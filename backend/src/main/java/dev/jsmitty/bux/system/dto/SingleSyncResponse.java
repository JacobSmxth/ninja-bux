package dev.jsmitty.bux.system.dto;

import java.util.Map;

public record SingleSyncResponse(String studentId, boolean updated, Map<String, Object> changes) {}
