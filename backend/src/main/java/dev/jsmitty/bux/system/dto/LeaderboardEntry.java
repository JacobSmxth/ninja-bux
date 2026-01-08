package dev.jsmitty.bux.system.dto;

/**
 * Leaderboard entry representing either earned or spent totals.
 */
public record LeaderboardEntry(
        int rank,
        String studentId,
        String ninjaName,
        Integer pointsEarned,
        Integer pointsSpent,
        Integer currentBalance) {
    public static LeaderboardEntry earned(
            int rank, String studentId, String ninjaName, Integer earned, Integer balance) {
        return new LeaderboardEntry(rank, studentId, ninjaName, earned, null, balance);
    }

    public static LeaderboardEntry spent(
            int rank, String studentId, String ninjaName, Integer spent, Integer balance) {
        return new LeaderboardEntry(rank, studentId, ninjaName, null, spent, balance);
    }
}
