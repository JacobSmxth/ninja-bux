package dev.jsmitty.bux.system.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cn")
public class CodeNinjasProxyController {

    private static final String CN_BASE_URL = "https://api.impact.codeninjas.com";

    private final RestTemplate restTemplate;

    public CodeNinjasProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/login")
    public ResponseEntity<?> proxyLogin(@RequestBody Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer null"); // matches observed curl
        headers.set(HttpHeaders.ORIGIN, "https://impact.codeninjas.com");
        headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (proxy)");
        headers.set(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.7");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        String url = CN_BASE_URL + "/center/api/login";

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            return copyResponse(response);
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @GetMapping("/activity/current")
    public ResponseEntity<?> proxyCurrentActivity(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
                    String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.ORIGIN, "https://impact.codeninjas.com");
        headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (proxy)");
        headers.set(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.7");
        if (authorization != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }

        HttpEntity<Void> request = new HttpEntity<>(headers);
        String url = CN_BASE_URL + "/center/api/common/activity/current";

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            return copyResponse(response);
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    private ResponseEntity<String> copyResponse(ResponseEntity<String> response) {
        HttpHeaders outgoing = new HttpHeaders();
        outgoing.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(response.getBody(), outgoing, response.getStatusCode());
    }
}
