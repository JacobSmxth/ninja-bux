package dev.jsmitty.bux.system.service;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jsmitty.bux.system.domain.Facility;
import dev.jsmitty.bux.system.domain.Ninja;
import dev.jsmitty.bux.system.domain.TxnType;
import dev.jsmitty.bux.system.dto.LocalSyncRequest;
import dev.jsmitty.bux.system.repository.FacilityRepository;
import dev.jsmitty.bux.system.repository.LedgerTxnRepository;
import dev.jsmitty.bux.system.repository.NinjaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Integration tests for NinjaService concurrency safety.
 * Verifies that the pessimistic locking prevents double-awarding
 * when multiple sync requests arrive concurrently for the same ninja.
 */
@SpringBootTest
@ActiveProfiles("test")
class NinjaServiceConcurrencyTest {

    @Autowired
    private NinjaService ninjaService;

    @Autowired
    private NinjaRepository ninjaRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private LedgerTxnRepository ledgerTxnRepository;

    private UUID facilityId;
    private String studentId;

    @BeforeEach
    void setup() {
        // Clean up in correct order to respect foreign key constraints
        ledgerTxnRepository.deleteAll();
        ninjaRepository.deleteAll();
        facilityRepository.deleteAll();

        facilityId = UUID.randomUUID();
        studentId = "concurrent-test-" + System.nanoTime();

        // Create facility
        Facility facility = new Facility(facilityId, "Test Facility");
        facilityRepository.save(facility);

        // Create ninja at activity sequence 5
        Ninja ninja = new Ninja(studentId, facilityId);
        ninja.setFirstName("Test");
        ninja.setLastName("Ninja");
        ninja.setCourseName("White Belt");
        ninja.setLastActivitySequence(5);
        ninja.setLevelSequence(1);
        ninjaRepository.save(ninja);
    }

    /**
     * Tests that concurrent sync requests for the same ninja do not result in
     * double-awarding of activity rewards.
     *
     * Scenario:
     * - Ninja starts at activity sequence 5
     * - 5 concurrent requests all try to sync with activity sequence 8
     * - Expected: Only ONE reward of 3 bux (3 activities * 1 White Belt multiplier)
     * - Without locking: Would see 15 bux (5 requests * 3 bux each)
     */
    @Test
    void concurrentSyncs_shouldNotDoubleAward() throws Exception {
        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        // All threads will try to sync with activitySequence = 8 (delta of 3)
        LocalSyncRequest request =
                new LocalSyncRequest(
                        "Test",
                        "Ninja",
                        "White Belt",
                        null,
                        1,
                        "act-1",
                        8, // Activity sequence 8 (was 5, so delta = 3)
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        for (int i = 0; i < threadCount; i++) {
            Future<?> future =
                    executor.submit(
                            () -> {
                                try {
                                    startLatch.await(); // Wait for all threads to be ready
                                    ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);
                                } catch (Exception e) {
                                    // Log but don't fail - some threads may get lock timeout
                                    System.err.println(
                                            "Thread exception (expected for concurrent access): "
                                                    + e.getMessage());
                                } finally {
                                    doneLatch.countDown();
                                }
                            });
            futures.add(future);
        }

        // Release all threads at once
        startLatch.countDown();

        // Wait for all threads to complete
        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue().withFailMessage("Threads did not complete in time");

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Verify: Calculate total balance from ledger
        Integer balance = ledgerTxnRepository.calculateBalance(facilityId, studentId);

        // Expected: 3 activities * 1 (White Belt multiplier) = 3 bux
        // If double-award happened, we'd see 6, 9, 12, or 15 bux
        assertThat(balance)
                .isEqualTo(3)
                .withFailMessage(
                        "Expected balance of 3 bux (single award), but got %d (possible double-award)",
                        balance);

        // Also verify ninja's lastActivitySequence is correctly updated
        Ninja updated =
                ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId).orElseThrow();
        assertThat(updated.getLastActivitySequence()).isEqualTo(8);

        // Verify only ONE activity reward transaction was created
        long activityRewardCount =
                ledgerTxnRepository
                        .findByFacilityIdAndStudentIdOrderByCreatedAtDesc(facilityId, studentId)
                        .stream()
                        .filter(txn -> txn.getType() == TxnType.ACTIVITY_REWARD)
                        .count();
        assertThat(activityRewardCount)
                .isEqualTo(1)
                .withFailMessage(
                        "Expected 1 activity reward transaction, but found %d", activityRewardCount);
    }

    /**
     * Tests that concurrent level progression syncs also don't double-award.
     */
    @Test
    void concurrentLevelProgressionSyncs_shouldNotDoubleAward() throws Exception {
        // Update ninja to have a starting level sequence
        Ninja ninja =
                ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId).orElseThrow();
        ninja.setLevelSequence(2);
        ninja.setLastActivitySequence(null); // Clear activity sequence to isolate level test
        ninjaRepository.save(ninja);

        int threadCount = 3;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // All threads will try to sync with levelSequence = 5 (delta of 3)
        LocalSyncRequest request =
                new LocalSyncRequest(
                        "Test",
                        "Ninja",
                        "White Belt",
                        null,
                        5, // Level sequence 5 (was 2, so delta = 3)
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(
                    () -> {
                        try {
                            startLatch.await();
                            ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);
                        } catch (Exception e) {
                            System.err.println("Thread exception: " + e.getMessage());
                        } finally {
                            doneLatch.countDown();
                        }
                    });
        }

        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Verify balance: 3 levels * 10 (White Belt progression multiplier) = 30 bux
        Integer balance = ledgerTxnRepository.calculateBalance(facilityId, studentId);
        assertThat(balance)
                .isEqualTo(30)
                .withFailMessage(
                        "Expected balance of 30 bux (single level award), but got %d", balance);

        // Verify ninja's level sequence
        Ninja updated =
                ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId).orElseThrow();
        assertThat(updated.getLevelSequence()).isEqualTo(5);
    }

    /**
     * Tests that the second sync with the same data doesn't re-award.
     * This tests idempotency of the sync operation.
     */
    @Test
    void duplicateSyncs_shouldNotReaward() {
        LocalSyncRequest request =
                new LocalSyncRequest(
                        "Test",
                        "Ninja",
                        "White Belt",
                        null,
                        1,
                        "act-1",
                        8,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        // First sync should award
        ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);
        Integer balanceAfterFirst = ledgerTxnRepository.calculateBalance(facilityId, studentId);
        assertThat(balanceAfterFirst).isEqualTo(3);

        // Second sync with same data should NOT award again
        ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);
        Integer balanceAfterSecond = ledgerTxnRepository.calculateBalance(facilityId, studentId);
        assertThat(balanceAfterSecond)
                .isEqualTo(3)
                .withFailMessage("Duplicate sync caused re-award!");

        // Third sync should also not award
        ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);
        Integer balanceAfterThird = ledgerTxnRepository.calculateBalance(facilityId, studentId);
        assertThat(balanceAfterThird).isEqualTo(3);
    }
}
