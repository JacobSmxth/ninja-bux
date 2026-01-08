package dev.jsmitty.bux.system.dto;

import java.util.List;

/**
 * Response wrapper for leaderboard results.
 */
public record LeaderboardResponse(String period, List<LeaderboardEntry> leaderboard) {}
