package dev.jsmitty.bux.system.dto;

import dev.jsmitty.bux.system.external.dto.CodeNinjasActivityResult;
import dev.jsmitty.bux.system.external.dto.LevelStatusSummary;

/**
 * Typed DTO for sync operation results, replacing the untyped Map&lt;String, Object&gt;.
 * Contains all possible fields from both local and remote sync operations.
 *
 * <p>Built by {@link dev.jsmitty.bux.system.service.NinjaService}.
 */
public record SyncChanges(
        NinjaResponse ninja,
        Integer initialBalance,
        Integer stepReward,
        Integer stepsAwarded,
        Integer levelProgressionReward,
        Integer levelDifference,
        Integer oldLevelSequence,
        Integer newLevelSequence,
        boolean updated,
        // Remote-sync specific fields (null for local sync)
        String token,
        CodeNinjasActivityResult activity,
        LevelStatusSummary levelStatus) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private NinjaResponse ninja;
        private Integer initialBalance;
        private Integer stepReward;
        private Integer stepsAwarded;
        private Integer levelProgressionReward;
        private Integer levelDifference;
        private Integer oldLevelSequence;
        private Integer newLevelSequence;
        private boolean updated;
        private String token;
        private CodeNinjasActivityResult activity;
        private LevelStatusSummary levelStatus;

        public Builder ninja(NinjaResponse ninja) {
            this.ninja = ninja;
            return this;
        }

        public Builder initialBalance(Integer initialBalance) {
            this.initialBalance = initialBalance;
            return this;
        }

        public Builder stepReward(Integer stepReward) {
            this.stepReward = stepReward;
            return this;
        }

        public Builder stepsAwarded(Integer stepsAwarded) {
            this.stepsAwarded = stepsAwarded;
            return this;
        }

        public Builder levelProgressionReward(Integer levelProgressionReward) {
            this.levelProgressionReward = levelProgressionReward;
            return this;
        }

        public Builder levelDifference(Integer levelDifference) {
            this.levelDifference = levelDifference;
            return this;
        }

        public Builder oldLevelSequence(Integer oldLevelSequence) {
            this.oldLevelSequence = oldLevelSequence;
            return this;
        }

        public Builder newLevelSequence(Integer newLevelSequence) {
            this.newLevelSequence = newLevelSequence;
            return this;
        }

        public Builder updated(boolean updated) {
            this.updated = updated;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder activity(CodeNinjasActivityResult activity) {
            this.activity = activity;
            return this;
        }

        public Builder levelStatus(LevelStatusSummary levelStatus) {
            this.levelStatus = levelStatus;
            return this;
        }

        public SyncChanges build() {
            return new SyncChanges(
                    ninja,
                    initialBalance,
                    stepReward,
                    stepsAwarded,
                    levelProgressionReward,
                    levelDifference,
                    oldLevelSequence,
                    newLevelSequence,
                    updated,
                    token,
                    activity,
                    levelStatus);
        }
    }
}
