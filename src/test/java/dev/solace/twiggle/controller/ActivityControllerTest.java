package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(ActivityController.class)
@Import(ActivityControllerTest.ActivityTestConfig.class)
class ActivityControllerTest {

    @TestConfiguration
    static class ActivityTestConfig {
        @Bean
        @Primary
        public ActivityService activityService() {
            return Mockito.mock(ActivityService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
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

        MockHttpServletRequestBuilder request =
                get("/api/activities").param("page", "0").param("size", "10");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void testGetAllActivitiesWithoutPagination() throws Exception {
        List<ActivityDTO> activities = List.of(dto);
        Mockito.when(activityService.findAll()).thenReturn(activities);

        MockHttpServletRequestBuilder request = get("/api/activities/all");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.message").value("Successfully retrieved all activities"));
    }

    @Test
    void testGetById_found() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(activityService.findById(id)).thenReturn(Optional.of(dto));

        MockHttpServletRequestBuilder request = get("/api/activities/{id}", id);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activityType").value("WATERING"));
    }

    @Test
    void testGetById_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(activityService.findById(id)).thenReturn(Optional.empty());

        MockHttpServletRequestBuilder request = get("/api/activities/{id}", id);

        mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    void testGetActivitiesByUserId() throws Exception {
        UUID userId = UUID.randomUUID();
        Page<ActivityDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(activityService.findByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(page);

        MockHttpServletRequestBuilder request =
                get("/api/activities/user/{userId}", userId).param("page", "0").param("size", "10");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.message").value("Successfully retrieved activities for user"));
    }

    @Test
    void testGetActivitiesByGardenPlanId() throws Exception {
        UUID gardenPlanId = UUID.randomUUID();
        Page<ActivityDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(activityService.findByGardenPlanId(eq(gardenPlanId), any(Pageable.class)))
                .thenReturn(page);

        MockHttpServletRequestBuilder request = get("/api/activities/garden-plan/{gardenPlanId}", gardenPlanId)
                .param("page", "0")
                .param("size", "10");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.message").value("Successfully retrieved activities for garden plan"));
    }

    @Test
    void testGetActivitiesByUserIdAndType() throws Exception {
        UUID userId = UUID.randomUUID();
        String activityType = "WATERING";
        Page<ActivityDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(activityService.findByUserIdAndActivityType(eq(userId), eq(activityType), any(Pageable.class)))
                .thenReturn(page);

        MockHttpServletRequestBuilder request = get(
                        "/api/activities/user/{userId}/type/{activityType}", userId, activityType)
                .param("page", "0")
                .param("size", "10");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.message").value("Successfully retrieved activities for user and type"));
    }

    @Test
    void testCreateActivity() throws Exception {
        Mockito.when(activityService.create(any())).thenReturn(dto);

        MockHttpServletRequestBuilder request = post("/api/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activityType").value("WATERING"))
                .andExpect(jsonPath("$.message").value("Activity created successfully"));
    }

    @Test
    void testUpdateActivity_found() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(activityService.update(eq(id), any())).thenReturn(Optional.of(dto));

        MockHttpServletRequestBuilder request = put("/api/activities/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activityType").value("WATERING"));
    }

    @Test
    void testUpdateActivity_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(activityService.update(eq(id), any())).thenReturn(Optional.empty());

        MockHttpServletRequestBuilder request = put("/api/activities/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    void testDeleteActivity() throws Exception {
        UUID id = UUID.randomUUID();

        MockHttpServletRequestBuilder request = delete("/api/activities/{id}", id);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Activity deleted successfully"));
    }
}
