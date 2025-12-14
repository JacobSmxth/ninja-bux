package dev.jsmitty.bux.system.controller;

import dev.jsmitty.bux.system.external.CodeNinjasApiClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cn/groups")
public class GroupController {

  private final CodeNinjasApiClient codeNinjasApiClient;

  public GroupController(CodeNinjasApiClient codeNinjasApiClient) {
    this.codeNinjasApiClient = codeNinjasApiClient;
  }

  @GetMapping("/current")
  public ResponseEntity<String> getCurrentGroup(
      @RequestHeader("Authorization") String authorization,
      @RequestParam String programId,
      @RequestParam String courseId) {
    String token = authorization.replace("Bearer ", "").trim();
    return ResponseEntity.ok(codeNinjasApiClient.getCurrentGroup(token, programId, courseId));
  }
}
