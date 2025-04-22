package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.ActivityDTO;
import dev.solace.twiggle.service.ActivityService;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ActivityController.class)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityService activityService;

    @Autowired
    private ObjectMapper objectMapper;

    private ActivityDTO dto;

    @BeforeEach
    void setUp() {
        dto = ActivityDTO.builder()
                .userId(UUID.randomUUID())
                .gardenPlanId(UUID.randomUUID())
                .activityType("WATERING")
                .description("Watered plants")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void testGetAllActivities() throws Exception {
        Page<ActivityDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(activityService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/activities?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void testGetById_found() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(activityService.findById(id)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/activities/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activityType").value("WATERING"));
    }

    @Test
    void testGetById_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(activityService.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/activities/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void testCreateActivity() throws Exception {
        Mockito.when(activityService.create(any())).thenReturn(dto);

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk()) // updated from isCreated()
                .andExpect(jsonPath("$.data.activityType").value("WATERING"))
                .andExpect(jsonPath("$.message").value("Activity created successfully")); // updated message
    }

    @Test
    void testUpdateActivity_found() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(activityService.update(eq(id), any())).thenReturn(Optional.of(dto));

        mockMvc.perform(put("/api/activities/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activityType").value("WATERING"));
    }

    @Test
    void testUpdateActivity_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(activityService.update(eq(id), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/activities/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteActivity() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/activities/{id}", id))
                .andExpect(status().isOk()) // updated from isNoContent()
                .andExpect(jsonPath("$.message").value("Activity deleted successfully")); // updated message
    }
}
