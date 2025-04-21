// src/test/java/dev/solace/twiggle/controller/PlantsLibraryControllerTest.java
package dev.solace.twiggle.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.PlantsLibraryDTO;
import dev.solace.twiggle.service.PlantsLibraryService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlantsLibraryController.class)
class PlantsLibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlantsLibraryService service;

    /* ---------- GET /api/plants-library ---------- */

    @Test
    void getAllPlants_returnsPagedResponse() throws Exception {
        PlantsLibraryDTO dto = PlantsLibraryDTO.builder()
                .id(UUID.randomUUID())
                .commonName("Snake Plant")
                .build();

        given(service.findAll(any(Pageable.class))).willReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/plants-library").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully retrieved plants"))
                .andExpect(jsonPath("$.data.content[0].commonName").value("Snake Plant"));
    }

    /* ---------- POST /api/plants-library ---------- */

    @Test
    void createPlant_validPayload_returnsCreatedDto() throws Exception {
        PlantsLibraryDTO request =
                PlantsLibraryDTO.builder().commonName("Snake Plant").build();
        PlantsLibraryDTO response = PlantsLibraryDTO.builder()
                .id(UUID.randomUUID())
                .commonName("Snake Plant")
                .build();

        given(service.create(any(PlantsLibraryDTO.class))).willReturn(response);

        mockMvc.perform(post("/api/plants-library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.data.commonName").value("Snake Plant"));
    }

    /* ---------- DELETE /api/plants-library/{id} ---------- */

    @Test
    void deletePlant_returnsSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        willDoNothing().given(service).delete(id);

        mockMvc.perform(delete("/api/plants-library/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Plant deleted successfully"));
    }
}
