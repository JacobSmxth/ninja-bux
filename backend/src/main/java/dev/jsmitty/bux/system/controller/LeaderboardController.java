package dev.jsmitty.bux.system.controller;

import dev.jsmitty.bux.system.dto.LeaderboardResponse;
import dev.jsmitty.bux.system.service.LeaderboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Leaderboard endpoints for earned/spent bux.
 *
 * <p>Public endpoints for displaying top earners and spenders by facility.
 */
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

    /** Top earners within a time window (public). */
    @GetMapping("/earned")
    public ResponseEntity<LeaderboardResponse> getTopEarners(
            @PathVariable UUID facilityId,
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(defaultValue = "10") int limit) {
        // Public endpoint - no auth check needed
        return ResponseEntity.ok(leaderboardService.getTopEarners(facilityId, period, limit));
    }

    /** Top spenders within a time window (public). */
    @GetMapping("/spent")
    public ResponseEntity<LeaderboardResponse> getTopSpenders(
            @PathVariable UUID facilityId,
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(defaultValue = "10") int limit) {
        // Public endpoint - no auth check needed
        return ResponseEntity.ok(leaderboardService.getTopSpenders(facilityId, period, limit));
    }
}
