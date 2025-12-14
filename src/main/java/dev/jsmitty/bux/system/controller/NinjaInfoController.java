package dev.jsmitty.bux.system.controller;

import dev.jsmitty.bux.system.external.CodeNinjasApiClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cn/ninjaInfo")
public class NinjaInfoController {

  private final CodeNinjasApiClient codeNinjasApiClient;

  public NinjaInfoController(CodeNinjasApiClient codeNinjasApiClient) {
    this.codeNinjasApiClient = codeNinjasApiClient;
  }

  @GetMapping
  public ResponseEntity<String> getNinjaInfo(
      @RequestHeader("Authorization") String authorization,
      @RequestParam String courseId,
      @RequestParam String userId,
      @RequestParam(required = false) Boolean getBeltChangeInfo) {
    String token = authorization.replace("Bearer ", "").trim();
    return ResponseEntity.ok(
        codeNinjasApiClient.getNinjaInfo(token, courseId, userId, getBeltChangeInfo));
  }
}
