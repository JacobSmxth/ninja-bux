package dev.jsmitty.bux.system.controller;

import dev.jsmitty.bux.system.dto.LeaderboardResponse;
import dev.jsmitty.bux.system.service.LeaderboardService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facilities/{facilityId}/leaderboard")
public class LeaderboardController {

  private final LeaderboardService leaderboardService;
  private final FacilityAccessChecker accessChecker;

  public LeaderboardController(
      LeaderboardService leaderboardService, FacilityAccessChecker accessChecker) {
    this.leaderboardService = leaderboardService;
    this.accessChecker = accessChecker;
  }

  @GetMapping("/earned")
  public ResponseEntity<LeaderboardResponse> getTopEarners(
      @PathVariable UUID facilityId,
      @RequestParam(defaultValue = "monthly") String period,
      @RequestParam(defaultValue = "10") int limit) {
    accessChecker.checkFacilityAccess(facilityId);
    return ResponseEntity.ok(leaderboardService.getTopEarners(facilityId, period, limit));
  }

  @GetMapping("/spent")
  public ResponseEntity<LeaderboardResponse> getTopSpenders(
      @PathVariable UUID facilityId,
      @RequestParam(defaultValue = "monthly") String period,
      @RequestParam(defaultValue = "10") int limit) {
    accessChecker.checkFacilityAccess(facilityId);
    return ResponseEntity.ok(leaderboardService.getTopSpenders(facilityId, period, limit));
  }
}
