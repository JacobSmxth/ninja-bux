package dev.jsmitty.bux.system.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Raw login response payload from Code Ninjas.
 *
 * <p>Parsed by {@link dev.jsmitty.bux.system.external.CodeNinjasApiClient}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CodeNinjasLoginResponse(
        @JsonProperty("token") String token, @JsonProperty("user") LoginUser user) {

    /** User details nested in the login response. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LoginUser(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("facilityName") String facilityName) {}
}
