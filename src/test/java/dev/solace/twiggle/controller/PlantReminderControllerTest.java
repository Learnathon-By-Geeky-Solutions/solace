package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.PlantReminderDTO;
import dev.solace.twiggle.service.PlantReminderService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(PlantReminderController.class)
public class PlantReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlantReminderService plantReminderService;

    @Autowired
    private ObjectMapper objectMapper;

    private PlantReminderDTO reminderDTO;
    private UUID reminderId;

    @BeforeEach
    void setUp() {
        reminderId = UUID.randomUUID();
        reminderDTO = PlantReminderDTO.builder()
                .plantId(UUID.randomUUID())
                .gardenPlanId(UUID.randomUUID())
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
    void testCreatePlantReminder() throws Exception {
        when(plantReminderService.create(any(PlantReminderDTO.class))).thenReturn(reminderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/plant-reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reminderDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Plant reminder created successfully"));
    }

    @Test
    void testUpdatePlantReminder() throws Exception {
        when(plantReminderService.update(eq(reminderId), any(PlantReminderDTO.class)))
                .thenReturn(Optional.of(reminderDTO));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/plant-reminders/" + reminderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reminderDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Plant reminder updated successfully"));
    }

    @Test
    void testMarkReminderAsCompleted() throws Exception {
        when(plantReminderService.markAsCompleted(reminderId)).thenReturn(Optional.of(reminderDTO));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/plant-reminders/" + reminderId + "/complete"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Plant reminder marked as completed"));
    }

    @Test
    void testDeletePlantReminder() throws Exception {
        doNothing().when(plantReminderService).delete(reminderId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/plant-reminders/" + reminderId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Plant reminder deleted successfully"));
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
}
