package dev.solace.twiggle.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import dev.solace.twiggle.dto.PestDTO;
import dev.solace.twiggle.service.PestService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(PestController.class)
class PestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PestService pestService;

    private PestDTO samplePest;

    @BeforeEach
    void setUp() {
        samplePest = PestDTO.builder()
                .id(1L)
                .commonName("Aphid")
                .scientificName("Aphidoidea")
                .description("Small sap-sucking insects")
                .damageSymptoms("Leaf curling")
                .lifeCycle("Rapid and recurring")
                .seasonActive("Spring")
                .organicControl("Neem oil")
                .chemicalControl("Malathion")
                .preventionTips("Use row covers")
                .imageUrl("http://example.com/aphid.jpg")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void testGetAllPests() throws Exception {
        when(pestService.findAll()).thenReturn(List.of(samplePest));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/pests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully retrieved all pests"))
                .andExpect(jsonPath("$.data[0].commonName").value("Aphid"));
    }

    @Test
    void testGetPestsByPlantLibraryId() throws Exception {
        UUID plantLibraryId = UUID.randomUUID();
        when(pestService.findByPlantLibraryId(plantLibraryId)).thenReturn(List.of(samplePest));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/pests/plant-library/" + plantLibraryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Successfully retrieved pests for plant library ID: " + plantLibraryId))
                .andExpect(jsonPath("$.data[0].scientificName").value("Aphidoidea"));
    }
}
