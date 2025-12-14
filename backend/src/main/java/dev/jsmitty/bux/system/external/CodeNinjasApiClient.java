package dev.jsmitty.bux.system.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jsmitty.bux.system.external.dto.CodeNinjasActivityResponse;
import dev.jsmitty.bux.system.external.dto.CodeNinjasActivityResult;
import dev.jsmitty.bux.system.external.dto.CodeNinjasLoginResponse;
import dev.jsmitty.bux.system.external.dto.CodeNinjasLoginResult;
import dev.jsmitty.bux.system.external.dto.LevelStatusSummary;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CodeNinjasApiClient {

  private static final String BASE_URL = "https://api.impact.codeninjas.com";
  private static final Logger log = LoggerFactory.getLogger(CodeNinjasApiClient.class);
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public CodeNinjasApiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
  }

  public CodeNinjasLoginResult login(String user, double latitude, double longitude) {
    HttpHeaders headers = defaultHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, "Bearer null");

    Map<String, Object> payload =
        Map.of("user", user, "latitude", latitude, "longitude", longitude);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
    String url = BASE_URL + "/center/api/login";

    try {
      ResponseEntity<String> response =
          restTemplate.exchange(url, HttpMethod.POST, request, String.class);
      CodeNinjasLoginResponse body = parseLoginResponse(response.getBody());
      if (body == null || body.token() == null || body.token().isBlank()) {
        throw new ResponseStatusException(
            HttpStatus.BAD_GATEWAY, "Code Ninjas login returned no token");
      }

      Map<String, Object> claims = decodeJwt(body.token());
      String studentId = stringClaim(claims, "oid");
      String facilityId = stringClaim(claims, "facilityid");
      Long exp = claims.get("exp") instanceof Number n ? n.longValue() : null;

      return new CodeNinjasLoginResult(
          body.token(),
          studentId,
          facilityId,
          body.user() != null ? body.user().firstName() : null,
          body.user() != null ? body.user().lastName() : null,
          body.user() != null ? body.user().facilityName() : null,
          exp);
    } catch (HttpStatusCodeException ex) {
      String body = ex.getResponseBodyAsString();
      log.warn("Code Ninjas login failed: status={} body={}", ex.getStatusCode(), body);
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Code Ninjas login failed: " + ex.getStatusCode() + " body=" + body,
          ex);
    }
  }

  public CodeNinjasActivityResult getCurrentActivity(String token) {
    HttpHeaders headers = defaultHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

    HttpEntity<Void> request = new HttpEntity<>(headers);
    String url = BASE_URL + "/center/api/common/activity/current";

    try {
      ResponseEntity<String> response =
          restTemplate.exchange(url, HttpMethod.GET, request, String.class);
      CodeNinjasActivityResponse body = parseActivityResponse(response.getBody());
      if (body == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_GATEWAY, "Code Ninjas activity returned no body");
      }
      return mapActivity(body);
    } catch (HttpStatusCodeException ex) {
      String body = ex.getResponseBodyAsString();
      log.warn("Code Ninjas activity failed: status={} body={}", ex.getStatusCode(), body);
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Code Ninjas activity failed: " + ex.getStatusCode() + " body=" + body,
          ex);
    }
  }

  public LevelStatusSummary getLevelStatus(
      String token, String programId, String courseId, String levelId) {
    HttpHeaders headers = defaultHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

    HttpEntity<Void> request = new HttpEntity<>(headers);
    String url =
        BASE_URL
            + "/center/api/level/statusinfo?programId="
            + programId
            + "&courseId="
            + courseId;
    if (levelId != null && !levelId.isBlank()) {
      url += "&levelId=" + levelId;
    }

    try {
      ResponseEntity<String> response =
          restTemplate.exchange(url, HttpMethod.GET, request, String.class);
      return summarizeLevelStatus(response.getBody(), programId, courseId, levelId);
    } catch (HttpStatusCodeException ex) {
      String body = ex.getResponseBodyAsString();
      log.warn("Code Ninjas level status failed: status={} body={} ", ex.getStatusCode(), body);
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Code Ninjas level status failed: " + ex.getStatusCode() + " body=" + body,
          ex);
    }
  }

  public String getCurrentGroup(String token, String programId, String courseId) {
    HttpHeaders headers = defaultHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

    HttpEntity<Void> request = new HttpEntity<>(headers);
    String url =
        BASE_URL
            + "/center/api/common/groups/currentGroup?programId="
            + programId
            + "&courseId="
            + courseId;

    try {
      ResponseEntity<String> response =
          restTemplate.exchange(url, HttpMethod.GET, request, String.class);
      return unwrapIfStringifiedJson(response.getBody());
    } catch (HttpStatusCodeException ex) {
      String body = ex.getResponseBodyAsString();
      log.warn("Code Ninjas currentGroup failed: status={} body={} ", ex.getStatusCode(), body);
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Code Ninjas currentGroup failed: " + ex.getStatusCode() + " body=" + body,
          ex);
    } catch (Exception ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "Failed to parse Code Ninjas currentGroup", ex);
    }
  }

  public String getNinjaInfo(String token, String courseId, String userId, Boolean getBeltChangeInfo) {
    HttpHeaders headers = defaultHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

    HttpEntity<Void> request = new HttpEntity<>(headers);
    String url =
        BASE_URL
            + "/center/api/common/ninjaInfo?courseId="
            + courseId
            + "&userId="
            + userId;
    if (getBeltChangeInfo != null) {
      url += "&getBeltChangeInfo=" + getBeltChangeInfo;
    }

    try {
      ResponseEntity<String> response =
          restTemplate.exchange(url, HttpMethod.GET, request, String.class);
      return unwrapIfStringifiedJson(response.getBody());
    } catch (HttpStatusCodeException ex) {
      String body = ex.getResponseBodyAsString();
      log.warn("Code Ninjas ninjaInfo failed: status={} body={} ", ex.getStatusCode(), body);
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Code Ninjas ninjaInfo failed: " + ex.getStatusCode() + " body=" + body,
          ex);
    } catch (Exception ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "Failed to parse Code Ninjas ninjaInfo", ex);
    }
  }

  private CodeNinjasLoginResponse parseLoginResponse(String rawBody) {
    if (rawBody == null || rawBody.isBlank()) {
      return null;
    }
    try {
      String body = unwrapIfStringifiedJson(rawBody);
      return objectMapper.readValue(body, CodeNinjasLoginResponse.class);
    } catch (Exception ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "Failed to parse Code Ninjas login response", ex);
    }
  }

  private CodeNinjasActivityResponse parseActivityResponse(String rawBody) {
    if (rawBody == null || rawBody.isBlank()) {
      return null;
    }
    try {
      String body = unwrapIfStringifiedJson(rawBody);
      return objectMapper.readValue(body, CodeNinjasActivityResponse.class);
    } catch (Exception ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "Failed to parse Code Ninjas activity response", ex);
    }
  }

  private String unwrapIfStringifiedJson(String raw) throws Exception {
    String trimmed = raw.trim();
    if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
        || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
      // JSON string containing escaped JSON content
      return objectMapper.readValue(trimmed, String.class);
    }
    return trimmed;
  }

  private LevelStatusSummary summarizeLevelStatus(
      String rawBody, String programId, String courseId, String levelId) {
    try {
      String body = unwrapIfStringifiedJson(rawBody);
      var root = objectMapper.readTree(body);

      int total = 0;
      int done = 0;
      String nextActivityId = null;
      String nextActivityType = null;
      Integer nextSequence = null;
      Integer levelSequence = null;

      if (root.isArray()) {
        for (var levelNode : root) {
          String currentLevelId = asText(levelNode, "id");
          Integer currentLevelSeq = asInt(levelNode, "sequence");
          if (levelId != null && currentLevelId != null && !levelId.equals(currentLevelId)) {
            continue;
          }
          if (levelSequence == null) {
            levelSequence = currentLevelSeq;
          }
          var activitiesNode = levelNode.get("activities");
          if (activitiesNode != null && activitiesNode.isArray()) {
            for (var activityNode : activitiesNode) {
              String activityId = asText(activityNode, "id");
              String activityType = asText(activityNode, "type");
              Integer activitySeq = asInt(activityNode, "sequence");
              var groupsNode = activityNode.get("groups");
              if (groupsNode != null && groupsNode.isArray()) {
                for (var groupNode : groupsNode) {
                  var subGroupsNode = groupNode.get("subGroups");
                  if (subGroupsNode != null && subGroupsNode.isArray()) {
                    for (var subNode : subGroupsNode) {
                      total++;
                      boolean status = subNode.has("status") && subNode.get("status").asBoolean();
                      if (status) {
                        done++;
                      } else if (nextActivityId == null) {
                        nextActivityId = activityId;
                        nextActivityType = activityType;
                        nextSequence = activitySeq;
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      int percent = total == 0 ? 0 : Math.round((done * 100f) / total);
      return new LevelStatusSummary(
          programId,
          courseId,
          levelId,
          levelSequence,
          total,
          done,
          percent,
          nextActivityId,
          nextActivityType,
          nextSequence);
    } catch (Exception ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "Failed to parse Code Ninjas level status", ex);
    }
  }

  private String asText(com.fasterxml.jackson.databind.JsonNode node, String field) {
    return node != null && node.has(field) && !node.get(field).isNull()
        ? node.get(field).asText()
        : null;
  }

  private Integer asInt(com.fasterxml.jackson.databind.JsonNode node, String field) {
    return node != null && node.has(field) && node.get(field).canConvertToInt()
        ? node.get(field).asInt()
        : null;
  }

  private HttpHeaders defaultHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set(HttpHeaders.ORIGIN, "https://impact.codeninjas.com");
    headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (bux-system)");
    headers.set(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.7");
    headers.set("DNT", "1");
    headers.set("Sec-Fetch-Dest", "empty");
    headers.set("Sec-Fetch-Mode", "cors");
    headers.set("Sec-Fetch-Site", "same-site");
    headers.set("Sec-CH-UA", "\"Chromium\";v=\"142\", \"Brave\";v=\"142\", \"Not_A Brand\";v=\"99\"");
    headers.set("Sec-CH-UA-Mobile", "?0");
    headers.set("Sec-CH-UA-Platform", "\"Linux\"");
    headers.set(HttpHeaders.REFERER, "https://impact.codeninjas.com/");
    return headers;
  }

  private Map<String, Object> decodeJwt(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
        throw new IllegalArgumentException("Invalid JWT");
      }
      byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
      return objectMapper.readValue(decoded, new TypeReference<>() {});
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to decode JWT", e);
    }
  }

  private String stringClaim(Map<String, Object> claims, String key) {
    Object val = claims.get(key);
    return val != null ? String.valueOf(val) : null;
  }

  private CodeNinjasActivityResult mapActivity(CodeNinjasActivityResponse body) {
    String activityId =
        body.relationShips() != null && body.relationShips().data() != null
            ? body.relationShips().data().activityId()
            : null;

    String programId =
        body.relationShips() != null && body.relationShips().data() != null
            ? body.relationShips().data().programId()
            : null;

    String courseId =
        body.relationShips() != null && body.relationShips().data() != null
            ? body.relationShips().data().courseId()
            : null;

    String studentId =
        body.relationShips() != null && body.relationShips().data() != null
            ? body.relationShips().data().studentId()
            : null;

    String courseName =
        body.relationShips() != null && body.relationShips().data() != null
            ? body.relationShips().data().courseName()
            : null;
    String levelId =
        body.relationShips() != null && body.relationShips().data() != null
            ? body.relationShips().data().levelId()
            : null;
    String levelName =
        body.relationShips() != null && body.relationShips().data() != null
            ? body.relationShips().data().levelName()
            : null;

    String groupId =
        body.relationShips() != null && body.relationShips().data() != null
            ? body.relationShips().data().groupId()
            : null;

    String subGroupId =
        body.relationShips() != null && body.relationShips().data() != null
            ? body.relationShips().data().subgroupId()
            : null;

    String activityName =
        body.relationShips() != null && body.relationShips().data() != null
            ? body.relationShips().data().activityName()
            : null;
    String activityType =
        body.relationShips() != null && body.relationShips().data() != null
            ? body.relationShips().data().activityType()
            : null;

    return new CodeNinjasActivityResult(
        body.id(),
        activityId != null ? activityId : body.id(),
        studentId,
        programId,
        courseId,
        courseName,
        levelName,
        levelId,
        null,
        activityName,
        activityType,
        groupId,
        subGroupId,
        body.createdDate(),
        body.lastModifiedDate());
  }
}
