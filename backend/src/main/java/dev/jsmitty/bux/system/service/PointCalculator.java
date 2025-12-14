package dev.jsmitty.bux.system.service;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PointCalculator {

  private record BeltConfig(int base, int perLevel, int multiplier) {}

  private static final Map<String, BeltConfig> BELT_CONFIGS =
      Map.of(
          "White Belt", new BeltConfig(0, 10, 1),
          "Yellow Belt", new BeltConfig(80, 8, 2),
          "Orange Belt", new BeltConfig(144, 6, 3),
          "Green Belt", new BeltConfig(192, 4, 4),
          "Blue Belt", new BeltConfig(224, 2, 5),
          "Purple Belt", new BeltConfig(240, 2, 6),
          "Brown Belt", new BeltConfig(256, 2, 7),
          "Red Belt", new BeltConfig(272, 2, 8),
          "Black Belt", new BeltConfig(288, 2, 9));

  public int calculateInitialBalance(String courseName, String levelName) {
    BeltConfig config = BELT_CONFIGS.getOrDefault(courseName, new BeltConfig(0, 10, 1));
    int levelNumber = parseLevelNumber(levelName);
    return config.base() + (levelNumber * config.perLevel());
  }

  public int calculateActivityReward(String courseName, String activityType) {
    if (activityType == null) {
      return 0;
    }

    if ("solve".equalsIgnoreCase(activityType)) {
      return 1;
    }

    if ("build".equalsIgnoreCase(activityType)
        || "code-adventure".equalsIgnoreCase(activityType)
        || "quest".equalsIgnoreCase(activityType)) {
      BeltConfig config = BELT_CONFIGS.getOrDefault(courseName, new BeltConfig(0, 10, 1));
      return config.multiplier();
    }

    return 1;
  }

  public int getBeltMultiplier(String courseName) {
    BeltConfig config = BELT_CONFIGS.getOrDefault(courseName, new BeltConfig(0, 10, 1));
    return config.multiplier();
  }

  private int parseLevelNumber(String levelName) {
    if (levelName == null || levelName.isEmpty()) {
      return 0;
    }

    try {
      String[] parts = levelName.split("\\s+");
      for (String part : parts) {
        try {
          return Integer.parseInt(part);
        } catch (NumberFormatException ignored) {
        }
      }
    } catch (Exception ignored) {
    }

    return 0;
  }
}
