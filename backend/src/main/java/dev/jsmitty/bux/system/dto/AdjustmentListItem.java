package dev.jsmitty.bux.system.dto;

import java.time.LocalDateTime;

public record AdjustmentListItem(
        Long id,
        String studentId,
        String ninjaName,
        String adminUsername,
        Integer amount,
        String reason,
        LocalDateTime createdAt) {}
