package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.config.TestSecurityConfig;
import dev.solace.twiggle.dto.PlantReminderDTO;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.PlantReminderService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(PlantReminderController.class)
@Import({PlantReminderControllerTest.PlantReminderTestConfig.class, TestSecurityConfig.class})
class PlantReminderControllerTest {

    @TestConfiguration
    static class PlantReminderTestConfig {
        @Bean
        @Primary
        public PlantReminderService plantReminderService() {
            return mock(PlantReminderService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlantReminderService plantReminderService;

    @Autowired
    private ObjectMapper objectMapper;

    private PlantReminderDTO reminderDTO;
    private UUID reminderId;
    private UUID plantId;
    private UUID gardenPlanId;

    private static final String GENERIC_ERROR_MESSAGE =
            "An unexpected error occurred. Please try again later or contact support if the problem persists.";

    @BeforeEach
    void setUp() {
        // Reset mock to clear any interactions
        reset(plantReminderService);

        reminderId = UUID.randomUUID();
        plantId = UUID.randomUUID();
        gardenPlanId = UUID.randomUUID();
        reminderDTO = PlantReminderDTO.builder()
                .plantId(plantId)
                .gardenPlanId(gardenPlanId)
                .reminderType("Watering")
                .reminderDate(LocalDate.now().plusDays(2))
                .notes("Water the basil plant")
                .isCompleted(false)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void testGetPlantReminderById() throws Exception {
        when(plantReminderService.findById(reminderId)).thenReturn(Optional.of(reminderDTO));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/" + reminderId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Successfully retrieved plant reminder"));
    }

    @Test
    void testGetPlantReminderByIdNotFound() throws Exception {
        when(plantReminderService.findById(reminderId)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/" + reminderId))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Plant reminder not found"));
    }

    @Test
    void testGetPlantReminderByIdError() throws Exception {
        when(plantReminderService.findById(reminderId))
                .thenThrow(
                        new CustomException("Test error", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/" + reminderId))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Test error"));
    }

    @Test
    void testCreatePlantReminder() throws Exception {
        when(plantReminderService.create(any(PlantReminderDTO.class))).thenReturn(reminderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/plant-reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reminderDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Plant reminder created successfully"));
    }

    @Test
    void testCreatePlantReminderError() throws Exception {
        when(plantReminderService.create(any(PlantReminderDTO.class))).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/plant-reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reminderDTO)))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(GENERIC_ERROR_MESSAGE));
    }

    @Test
    void testUpdatePlantReminder() throws Exception {
        when(plantReminderService.update(any(UUID.class), any(PlantReminderDTO.class)))
                .thenReturn(Optional.of(reminderDTO));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/plant-reminders/" + reminderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reminderDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Plant reminder updated successfully"));
    }

    @Test
    void testUpdatePlantReminderNotFound() throws Exception {
        when(plantReminderService.update(any(UUID.class), any(PlantReminderDTO.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/plant-reminders/" + reminderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reminderDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Plant reminder not found"));
    }

    @Test
    void testUpdatePlantReminderError() throws Exception {
        doThrow(new RuntimeException("Service error"))
                .when(plantReminderService)
                .update(eq(reminderId), any(PlantReminderDTO.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/plant-reminders/" + reminderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reminderDTO)))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(GENERIC_ERROR_MESSAGE));
    }

    @Test
    void testMarkReminderAsCompleted() throws Exception {
        when(plantReminderService.markAsCompleted(reminderId)).thenReturn(Optional.of(reminderDTO));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/plant-reminders/" + reminderId + "/complete"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Plant reminder marked as completed"));
    }

    @Test
    void testMarkReminderAsCompletedNotFound() throws Exception {
        when(plantReminderService.markAsCompleted(reminderId)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/plant-reminders/" + reminderId + "/complete"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Plant reminder not found"));
    }

    @Test
    void testMarkReminderAsCompletedError() throws Exception {
        when(plantReminderService.markAsCompleted(reminderId))
                .thenThrow(
                        new CustomException("Test error", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/plant-reminders/" + reminderId + "/complete"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Test error"));
    }

    @Test
    void testDeletePlantReminder() throws Exception {
        doNothing().when(plantReminderService).delete(reminderId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/plant-reminders/" + reminderId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Plant reminder deleted successfully"));
    }

    @Test
    void testDeletePlantReminderError() throws Exception {
        doThrow(new RuntimeException("Service error"))
                .when(plantReminderService)
                .delete(reminderId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/plant-reminders/" + reminderId))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(GENERIC_ERROR_MESSAGE));
    }

    @Test
    void testGetRemindersDueByDate() throws Exception {
        when(plantReminderService.findByReminderDateLessThanEqual(any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/due")
                        .param("date", LocalDate.now().toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Successfully retrieved due reminders"));
    }

    @Test
    void testGetRemindersDueByDateError() throws Exception {
        when(plantReminderService.findByReminderDateLessThanEqual(any(LocalDate.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/due")
                        .param("date", LocalDate.now().toString()))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(GENERIC_ERROR_MESSAGE));
    }

    @Test
    void testGetAllPlantReminders() throws Exception {
        Page<PlantReminderDTO> page = new PageImpl<>(Arrays.asList(reminderDTO));
        when(plantReminderService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "reminderDate")
                        .param("direction", "ASC"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Successfully retrieved plant reminders"));
    }

    @Test
    void testGetAllPlantRemindersError() throws Exception {
        when(plantReminderService.findAll(any(Pageable.class))).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "reminderDate")
                        .param("direction", "ASC"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(GENERIC_ERROR_MESSAGE));
    }

    @Test
    void testGetAllPlantRemindersWithoutPagination() throws Exception {
        List<PlantReminderDTO> reminders = Arrays.asList(reminderDTO);
        when(plantReminderService.findAll()).thenReturn(reminders);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/all"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Successfully retrieved all plant reminders"));
    }

    @Test
    void testGetAllPlantRemindersWithoutPaginationError() throws Exception {
        when(plantReminderService.findAll()).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/all"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(GENERIC_ERROR_MESSAGE));
    }

    @Test
    void testGetAllPlantRemindersWithInvalidSortDirection() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "reminderDate")
                        .param("direction", "INVALID"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Invalid sort direction. Must be either 'ASC' or 'DESC'"));
    }

    @Test
    void testGetRemindersByPlantId() throws Exception {
        Page<PlantReminderDTO> page = new PageImpl<>(Arrays.asList(reminderDTO));
        when(plantReminderService.findByPlantId(any(UUID.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/plant/" + plantId)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "reminderDate")
                        .param("direction", "ASC"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Successfully retrieved reminders for plant"));
    }

    @Test
    void testGetRemindersByPlantIdError() throws Exception {
        when(plantReminderService.findByPlantId(any(UUID.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/plant/" + plantId)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "reminderDate")
                        .param("direction", "ASC"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(GENERIC_ERROR_MESSAGE));
    }

    @Test
    void testGetRemindersByPlantIdWithInvalidSortDirection() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/plant/" + plantId)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "reminderDate")
                        .param("direction", "INVALID"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Invalid sort direction. Must be either 'ASC' or 'DESC'"));
    }

    @Test
    void testGetRemindersByGardenPlanId() throws Exception {
        Page<PlantReminderDTO> page = new PageImpl<>(Arrays.asList(reminderDTO));
        when(plantReminderService.findByGardenPlanId(any(UUID.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/garden-plan/" + gardenPlanId)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "reminderDate")
                        .param("direction", "ASC"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Successfully retrieved reminders for garden plan"));
    }

    @Test
    void testGetRemindersByGardenPlanIdError() throws Exception {
        when(plantReminderService.findByGardenPlanId(any(UUID.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/garden-plan/" + gardenPlanId)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "reminderDate")
                        .param("direction", "ASC"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(GENERIC_ERROR_MESSAGE));
    }

    @Test
    void testGetRemindersByGardenPlanIdWithInvalidSortDirection() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/garden-plan/" + gardenPlanId)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "reminderDate")
                        .param("direction", "INVALID"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Invalid sort direction. Must be either 'ASC' or 'DESC'"));
    }

    @Test
    void testGetIncompleteRemindersByPlantId() throws Exception {
        List<PlantReminderDTO> reminders = Arrays.asList(reminderDTO);
        when(plantReminderService.findByPlantIdAndIsCompleted(plantId, false)).thenReturn(reminders);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/plant/" + plantId + "/incomplete"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Successfully retrieved incomplete reminders for plant"));
    }

    @Test
    void testGetIncompleteRemindersByPlantIdError() throws Exception {
        when(plantReminderService.findByPlantIdAndIsCompleted(any(UUID.class), eq(false)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-reminders/plant/" + plantId + "/incomplete"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(GENERIC_ERROR_MESSAGE));
    }
}
