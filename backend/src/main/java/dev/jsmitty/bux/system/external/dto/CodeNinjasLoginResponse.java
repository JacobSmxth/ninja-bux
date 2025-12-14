package dev.jsmitty.bux.system.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CodeNinjasLoginResponse(
    @JsonProperty("token") String token, @JsonProperty("user") LoginUser user) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record LoginUser(
      @JsonProperty("firstName") String firstName,
      @JsonProperty("lastName") String lastName,
      @JsonProperty("facilityName") String facilityName) {}
}
