package dev.jsmitty.bux.system.service;

import dev.jsmitty.bux.system.domain.Ninja;
import dev.jsmitty.bux.system.domain.TxnType;
import dev.jsmitty.bux.system.dto.LeaderboardEntry;
import dev.jsmitty.bux.system.dto.LeaderboardResponse;
import dev.jsmitty.bux.system.repository.LedgerTxnRepository;
import dev.jsmitty.bux.system.repository.NinjaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardService {

  private final LedgerTxnRepository ledgerTxnRepository;
  private final NinjaRepository ninjaRepository;

  public LeaderboardService(
      LedgerTxnRepository ledgerTxnRepository, NinjaRepository ninjaRepository) {
    this.ledgerTxnRepository = ledgerTxnRepository;
    this.ninjaRepository = ninjaRepository;
  }

  public LeaderboardResponse getTopEarners(UUID facilityId, String period, int limit) {
    LocalDateTime since = calculateSinceDate(period);

    List<Object[]> results =
        ledgerTxnRepository.findTopEarners(facilityId, since, PageRequest.of(0, limit));

    List<LeaderboardEntry> entries = buildLeaderboardEntries(facilityId, results, true);

    return new LeaderboardResponse(period, entries);
  }

  public LeaderboardResponse getTopSpenders(UUID facilityId, String period, int limit) {
    LocalDateTime since = calculateSinceDate(period);

    List<Object[]> results =
        ledgerTxnRepository.findTopSpenders(
            facilityId, TxnType.PURCHASE, since, PageRequest.of(0, limit));

    List<LeaderboardEntry> entries = buildLeaderboardEntries(facilityId, results, false);

    return new LeaderboardResponse(period, entries);
  }

  private LocalDateTime calculateSinceDate(String period) {
    LocalDateTime now = LocalDateTime.now();
    return switch (period.toLowerCase()) {
      case "weekly" -> now.minusWeeks(1);
      case "monthly" -> now.minusMonths(1);
      case "yearly" -> now.minusYears(1);
      default -> now.minusMonths(1);
    };
  }

  private List<LeaderboardEntry> buildLeaderboardEntries(
      UUID facilityId, List<Object[]> results, boolean isEarned) {
    int rank = 1;
    List<LeaderboardEntry> entries = new java.util.ArrayList<>();

    for (Object[] row : results) {
      String studentId = (String) row[0];
      Long total = ((Number) row[1]).longValue();

      Ninja ninja =
          ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId).orElse(null);
      String name = ninja != null ? ninja.getFullName() : null;
      Integer balance = ninja != null ? ninja.getCurrentBalance() : 0;

      if (isEarned) {
        entries.add(LeaderboardEntry.earned(rank, studentId, name, total.intValue(), balance));
      } else {
        entries.add(LeaderboardEntry.spent(rank, studentId, name, total.intValue(), balance));
      }
      rank++;
    }

    return entries;
  }
}
