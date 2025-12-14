package dev.jsmitty.bux.system.dto;

import java.util.List;

public record LeaderboardResponse(String period, List<LeaderboardEntry> leaderboard) {}
