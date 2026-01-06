package dev.jsmitty.bux.system.controller;

import dev.jsmitty.bux.system.external.CodeNinjasApiClient;
import dev.jsmitty.bux.system.external.dto.LevelStatusSummary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cn/level")
public class LevelStatusController {

    private final CodeNinjasApiClient codeNinjasApiClient;

    public LevelStatusController(CodeNinjasApiClient codeNinjasApiClient) {
        this.codeNinjasApiClient = codeNinjasApiClient;
    }

    @GetMapping("/statusinfo")
    public ResponseEntity<LevelStatusSummary> getLevelStatus(
            @RequestHeader("Authorization") String authorization,
            @RequestParam String programId,
            @RequestParam String courseId,
            @RequestParam(required = false) String levelId) {
        String token = authorization.replace("Bearer ", "").trim();
        return ResponseEntity.ok(
                codeNinjasApiClient.getLevelStatus(token, programId, courseId, levelId));
    }
}
