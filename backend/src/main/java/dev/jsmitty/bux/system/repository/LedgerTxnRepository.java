package dev.jsmitty.bux.system.repository;

import dev.jsmitty.bux.system.domain.LedgerTxn;
import dev.jsmitty.bux.system.domain.TxnType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerTxnRepository extends JpaRepository<LedgerTxn, Long> {
  Page<LedgerTxn> findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
      UUID facilityId, String studentId, Pageable pageable);

  List<LedgerTxn> findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
      UUID facilityId, String studentId);

  @Query(
      "SELECT COALESCE(SUM(l.amount), 0) FROM LedgerTxn l WHERE l.facilityId = :facilityId AND"
          + " l.studentId = :studentId")
  Integer calculateBalance(
      @Param("facilityId") UUID facilityId, @Param("studentId") String studentId);

  @Query(
      "SELECT l.studentId, SUM(l.amount) as total FROM LedgerTxn l "
          + "WHERE l.facilityId = :facilityId AND l.amount > 0 AND l.createdAt >= :since "
          + "GROUP BY l.studentId ORDER BY total DESC")
  List<Object[]> findTopEarners(
      @Param("facilityId") UUID facilityId, @Param("since") LocalDateTime since, Pageable pageable);

  @Query(
      "SELECT l.studentId, ABS(SUM(l.amount)) as total FROM LedgerTxn l "
          + "WHERE l.facilityId = :facilityId AND l.createdAt >= :since AND "
          + "((l.type = :purchaseType AND l.amount < 0) "
          + "OR (l.type = :refundType AND l.amount > 0 AND l.description LIKE 'Refund:%')) "
          + "GROUP BY l.studentId ORDER BY total DESC")
  List<Object[]> findTopSpendersNetOfRefunds(
      @Param("facilityId") UUID facilityId,
      @Param("purchaseType") TxnType purchaseType,
      @Param("refundType") TxnType refundType,
      @Param("since") LocalDateTime since,
      Pageable pageable);
}
