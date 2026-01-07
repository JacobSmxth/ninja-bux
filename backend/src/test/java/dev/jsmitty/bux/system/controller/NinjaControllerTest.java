package dev.jsmitty.bux.system.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jsmitty.bux.system.dto.*;
import dev.jsmitty.bux.system.service.LedgerService;
import dev.jsmitty.bux.system.service.NinjaService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller tests for NinjaController.
 * Tests HTTP endpoints, request/response mapping, and Pageable binding.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NinjaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NinjaService ninjaService;

    @MockitoBean
    private LedgerService ledgerService;

    private static final UUID FACILITY_ID = UUID.randomUUID();
    private static final String STUDENT_ID = "student-123";

    // ==================== GET /ninjas Tests ====================

    @Test
    void getNinjas_returnsOkWithPageableDefaults() throws Exception {
        NinjaListResponse response = new NinjaListResponse(List.of(), 0);
        when(ninjaService.getNinjas(eq(FACILITY_ID), any(Pageable.class))).thenReturn(response);

        mockMvc.perform(get("/api/facilities/{facilityId}/ninjas", FACILITY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ninjas").isArray())
                .andExpect(jsonPath("$.totalCount").value(0));
    }

    @Test
    void getNinjas_acceptsPageableParameters() throws Exception {
        NinjaListResponse response = new NinjaListResponse(List.of(), 0);
        when(ninjaService.getNinjas(eq(FACILITY_ID), any(Pageable.class))).thenReturn(response);

        mockMvc.perform(
                        get("/api/facilities/{facilityId}/ninjas", FACILITY_ID)
                                .param("page", "2")
                                .param("size", "25")
                                .param("sort", "lastName,desc"))
                .andExpect(status().isOk());
    }

    // ==================== GET /ninjas/{studentId} Tests ====================

    @Test
    void getNinja_returnsOkWhenFound() throws Exception {
        NinjaResponse ninja =
                new NinjaResponse(
                        1L,
                        STUDENT_ID,
                        "John",
                        "Doe",
                        "White Belt",
                        "Level 1",
                        1,
                        "act-1",
                        5,
                        "group-1",
                        "sub-1",
                        10,
                        20,
                        100,
                        LocalDateTime.now());
        when(ninjaService.getNinja(FACILITY_ID, STUDENT_ID)).thenReturn(Optional.of(ninja));

        mockMvc.perform(
                        get("/api/facilities/{facilityId}/ninjas/{studentId}", FACILITY_ID, STUDENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(STUDENT_ID))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getNinja_returns404WhenNotFound() throws Exception {
        when(ninjaService.getNinja(FACILITY_ID, STUDENT_ID)).thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/facilities/{facilityId}/ninjas/{studentId}", FACILITY_ID, STUDENT_ID))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /ninjas/{studentId}/sync-local Tests ====================

    @Test
    void syncSingleNinjaLocal_returnsOkOnSuccess() throws Exception {
        LocalSyncRequest request =
                new LocalSyncRequest(
                        "John",
                        "Doe",
                        "White Belt",
                        null,
                        1,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        SyncChanges changes = SyncChanges.builder().updated(true).build();
        SingleSyncResponse response = new SingleSyncResponse(STUDENT_ID, true, changes);

        when(ninjaService.syncSingleNinjaLocal(eq(FACILITY_ID), eq(STUDENT_ID), any()))
                .thenReturn(response);

        mockMvc.perform(
                        post(
                                        "/api/facilities/{facilityId}/ninjas/{studentId}/sync-local",
                                        FACILITY_ID,
                                        STUDENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(STUDENT_ID))
                .andExpect(jsonPath("$.updated").value(true));
    }

    @Test
    void syncSingleNinjaLocal_returns400OnValidationError() throws Exception {
        LocalSyncRequest request =
                new LocalSyncRequest(
                        "John",
                        "Doe",
                        "White Belt",
                        null,
                        1,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        when(ninjaService.syncSingleNinjaLocal(eq(FACILITY_ID), eq(STUDENT_ID), any()))
                .thenThrow(new IllegalArgumentException("studentId must not be blank"));

        mockMvc.perform(
                        post(
                                        "/api/facilities/{facilityId}/ninjas/{studentId}/sync-local",
                                        FACILITY_ID,
                                        STUDENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("studentId must not be blank"));
    }

    // ==================== POST /ninjas/{studentId}/sync Tests ====================

    @Test
    void syncSingleNinja_returns400OnInvalidRequest() throws Exception {
        // Missing required fields
        String invalidJson = "{}";

        mockMvc.perform(
                        post(
                                        "/api/facilities/{facilityId}/ninjas/{studentId}/sync",
                                        FACILITY_ID,
                                        STUDENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void syncSingleNinja_returns400OnBlankUser() throws Exception {
        CodeNinjasLoginRequest request = new CodeNinjasLoginRequest("", 0.0, 0.0);

        mockMvc.perform(
                        post(
                                        "/api/facilities/{facilityId}/ninjas/{studentId}/sync",
                                        FACILITY_ID,
                                        STUDENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET /ninjas/{studentId}/ledger Tests ====================

    @Test
    void getLedger_returnsOkWithPageable() throws Exception {
        LedgerResponse response = new LedgerResponse(List.of(), 100);
        when(ledgerService.getLedger(eq(FACILITY_ID), eq(STUDENT_ID), any(Pageable.class)))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/facilities/{facilityId}/ninjas/{studentId}/ledger",
                                FACILITY_ID,
                                STUDENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(100))
                .andExpect(jsonPath("$.transactions").isArray());
    }

    @Test
    void getLedger_acceptsPageableParameters() throws Exception {
        LedgerResponse response = new LedgerResponse(List.of(), 50);
        when(ledgerService.getLedger(eq(FACILITY_ID), eq(STUDENT_ID), any(Pageable.class)))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                        "/api/facilities/{facilityId}/ninjas/{studentId}/ledger",
                                        FACILITY_ID,
                                        STUDENT_ID)
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk());
    }

    // ==================== Error Handling Tests ====================

    @Test
    void exceptionHandler_handlesNullPointerException() throws Exception {
        LocalSyncRequest request =
                new LocalSyncRequest(
                        "John",
                        "Doe",
                        "White Belt",
                        null,
                        1,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        when(ninjaService.syncSingleNinjaLocal(eq(FACILITY_ID), eq(STUDENT_ID), any()))
                .thenThrow(new NullPointerException("facilityId must not be null"));

        mockMvc.perform(
                        post(
                                        "/api/facilities/{facilityId}/ninjas/{studentId}/sync-local",
                                        FACILITY_ID,
                                        STUDENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("facilityId must not be null"));
    }
}
