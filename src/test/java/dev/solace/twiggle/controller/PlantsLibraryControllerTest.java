package dev.solace.twiggle.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.config.TestSecurityConfig;
import dev.solace.twiggle.dto.PlantsLibraryDTO;
import dev.solace.twiggle.service.PlantsLibraryService;
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
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlantsLibraryController.class)
@Import({PlantsLibraryControllerTest.PlantsLibraryTestConfig.class, TestSecurityConfig.class})
class PlantsLibraryControllerTest {

    @TestConfiguration
    static class PlantsLibraryTestConfig {
        @Bean
        @Primary
        public PlantsLibraryService plantsLibraryService() {
            return mock(PlantsLibraryService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlantsLibraryService service;

    @BeforeEach
    void setup() {
        // Reset the mock before each test to clear any previous interactions
        reset(service);
    }

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

    /* ---------- GET /api/plants-library/all ---------- */

    @Test
    void getAllPlantsWithoutPagination_returnsAllPlants() throws Exception {
        // Create test data
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        PlantsLibraryDTO dto1 =
                PlantsLibraryDTO.builder().id(id1).commonName("Snake Plant").build();
        PlantsLibraryDTO dto2 =
                PlantsLibraryDTO.builder().id(id2).commonName("Spider Plant").build();

        List<PlantsLibraryDTO> dtoList = List.of(dto1, dto2);

        // Mock the service to return a list of DTOs
        // Make sure this mock is properly set up
        willReturn(dtoList).given(service).findAll();

        // Perform the request and verify the response
        mockMvc.perform(get("/api/plants-library/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully retrieved all plants"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].commonName").value("Snake Plant"))
                .andExpect(jsonPath("$.data[0].id").value(id1.toString()))
                .andExpect(jsonPath("$.data[1].commonName").value("Spider Plant"))
                .andExpect(jsonPath("$.data[1].id").value(id2.toString()));
    }

    /* ---------- GET /api/plants-library/{id} ---------- */

    @Test
    void getPlantById_whenFound_returnsPlant() throws Exception {
        UUID id = UUID.randomUUID();
        PlantsLibraryDTO dto =
                PlantsLibraryDTO.builder().id(id).commonName("Snake Plant").build();

        given(service.findById(id)).willReturn(Optional.of(dto));

        mockMvc.perform(get("/api/plants-library/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully retrieved plant"))
                .andExpect(jsonPath("$.data.commonName").value("Snake Plant"));
    }

    @Test
    void getPlantById_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        given(service.findById(id)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/plants-library/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Plant not found"))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
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

    /* ---------- PUT /api/plants-library/{id} ---------- */

    @Test
    void updatePlant_whenFound_returnsUpdatedDto() throws Exception {
        UUID id = UUID.randomUUID();
        PlantsLibraryDTO request =
                PlantsLibraryDTO.builder().commonName("Updated Snake Plant").build();

        PlantsLibraryDTO response = PlantsLibraryDTO.builder()
                .id(id)
                .commonName("Updated Snake Plant")
                .build();

        given(service.update(eq(id), any(PlantsLibraryDTO.class))).willReturn(Optional.of(response));

        mockMvc.perform(put("/api/plants-library/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Plant updated successfully"))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.commonName").value("Updated Snake Plant"));
    }

    @Test
    void updatePlant_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        PlantsLibraryDTO request =
                PlantsLibraryDTO.builder().commonName("Updated Snake Plant").build();

        given(service.update(eq(id), any(PlantsLibraryDTO.class))).willReturn(Optional.empty());

        mockMvc.perform(put("/api/plants-library/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Plant not found"))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
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

    /* ---------- Error Handling Tests ---------- */

    @Test
    void getAllPlants_whenServiceThrowsException_returns500() throws Exception {
        given(service.findAll(any(Pageable.class))).willThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants-library"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plants"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void getAllPlantsWithoutPagination_whenServiceThrowsException_returns500() throws Exception {
        given(service.findAll()).willThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants-library/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plants"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void getPlantById_whenServiceThrowsException_returns500() throws Exception {
        UUID id = UUID.randomUUID();
        given(service.findById(id)).willThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants-library/{id}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plant"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void createPlant_whenServiceThrowsException_returns500() throws Exception {
        PlantsLibraryDTO request =
                PlantsLibraryDTO.builder().commonName("Snake Plant").build();

        given(service.create(any(PlantsLibraryDTO.class))).willThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/plants-library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to create plant"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void updatePlant_whenServiceThrowsException_returns500() throws Exception {
        UUID id = UUID.randomUUID();
        PlantsLibraryDTO request =
                PlantsLibraryDTO.builder().commonName("Updated Snake Plant").build();

        given(service.update(eq(id), any(PlantsLibraryDTO.class))).willThrow(new RuntimeException("Service error"));

        mockMvc.perform(put("/api/plants-library/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to update plant"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void deletePlant_whenServiceThrowsException_returns500() throws Exception {
        UUID id = UUID.randomUUID();
        willThrow(new RuntimeException("Service error")).given(service).delete(id);

        mockMvc.perform(delete("/api/plants-library/{id}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to delete plant"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    /* ---------- Additional API Tests ---------- */

    @Test
    void searchPlants_returnsMatchingPlants() throws Exception {
        String query = "snake";
        PlantsLibraryDTO dto = PlantsLibraryDTO.builder()
                .id(UUID.randomUUID())
                .commonName("Snake Plant")
                .build();

        Page<PlantsLibraryDTO> page = new PageImpl<>(List.of(dto));
        given(service.searchPlants(eq(query), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/plants-library/search")
                        .param("query", query)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully searched plants"))
                .andExpect(jsonPath("$.data.content[0].commonName").value("Snake Plant"));
    }

    @Test
    void getPlantsByType_returnsMatchingPlants() throws Exception {
        String plantType = "succulent";
        PlantsLibraryDTO dto = PlantsLibraryDTO.builder()
                .id(UUID.randomUUID())
                .commonName("Snake Plant")
                .plantType("Succulent")
                .build();

        Page<PlantsLibraryDTO> page = new PageImpl<>(List.of(dto));
        given(service.findByPlantType(eq(plantType), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/plants-library/type/{plantType}", plantType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully retrieved plants by type"))
                .andExpect(jsonPath("$.data.content[0].commonName").value("Snake Plant"));
    }

    @Test
    void getPlantsByLifeCycle_returnsMatchingPlants() throws Exception {
        String lifeCycle = "perennial";
        PlantsLibraryDTO dto = PlantsLibraryDTO.builder()
                .id(UUID.randomUUID())
                .commonName("Snake Plant")
                .lifeCycle("Perennial")
                .build();

        Page<PlantsLibraryDTO> page = new PageImpl<>(List.of(dto));
        given(service.findByLifeCycle(eq(lifeCycle), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/plants-library/life-cycle/{lifeCycle}", lifeCycle))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully retrieved plants by life cycle"))
                .andExpect(jsonPath("$.data.content[0].commonName").value("Snake Plant"));
    }

    @Test
    void getMedicinalPlants_returnsMatchingPlants() throws Exception {
        PlantsLibraryDTO dto = PlantsLibraryDTO.builder()
                .id(UUID.randomUUID())
                .commonName("Aloe Vera")
                .medicinal(true)
                .build();

        Page<PlantsLibraryDTO> page = new PageImpl<>(List.of(dto));
        given(service.findByMedicinal(eq(true), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/plants-library/medicinal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully retrieved medicinal plants"))
                .andExpect(jsonPath("$.data.content[0].commonName").value("Aloe Vera"));
    }

    @Test
    void searchPlantsAdvanced_returnsMatchingPlants() throws Exception {
        UUID id = UUID.randomUUID();
        PlantsLibraryDTO dto = PlantsLibraryDTO.builder()
                .id(id)
                .commonName("Aloe Vera")
                .plantType("Succulent")
                .medicinal(true)
                .build();

        Page<PlantsLibraryDTO> page = new PageImpl<>(List.of(dto));
        given(service.searchPlantsAdvanced(any(), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/plants-library/search/advanced")
                        .param("commonName", "Aloe")
                        .param("plantType", "Succulent")
                        .param("medicinal", "true")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully searched plants with advanced criteria"))
                .andExpect(jsonPath("$.data.content[0].commonName").value("Aloe Vera"))
                .andExpect(jsonPath("$.data.content[0].plantType").value("Succulent"))
                .andExpect(jsonPath("$.data.content[0].medicinal").value(true));
    }

    @Test
    void searchPlantsAdvanced_whenServiceThrowsException_returns500() throws Exception {
        given(service.searchPlantsAdvanced(any(), any(Pageable.class)))
                .willThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants-library/search/advanced")
                        .param("commonName", "Aloe")
                        .param("plantType", "Succulent"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to perform advanced search on plants"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void searchPlants_whenServiceThrowsException_returns500() throws Exception {
        given(service.searchPlants(anyString(), any(Pageable.class))).willThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants-library/search").param("query", "aloe"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to search plants"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void getPlantsByType_whenServiceThrowsException_returns500() throws Exception {
        String plantType = "succulent";
        given(service.findByPlantType(eq(plantType), any(Pageable.class)))
                .willThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants-library/type/{plantType}", plantType))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plants by type"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void getPlantsByLifeCycle_whenServiceThrowsException_returns500() throws Exception {
        String lifeCycle = "perennial";
        given(service.findByLifeCycle(eq(lifeCycle), any(Pageable.class)))
                .willThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants-library/life-cycle/{lifeCycle}", lifeCycle))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plants by life cycle"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void getMedicinalPlants_whenServiceThrowsException_returns500() throws Exception {
        given(service.findByMedicinal(eq(true), any(Pageable.class))).willThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants-library/medicinal"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve medicinal plants"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void getAllPlants_withInvalidSortDirection_returns400() throws Exception {
        mockMvc.perform(get("/api/plants-library").param("direction", "INVALID_DIRECTION"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plants"));
    }

    @Test
    void searchPlants_withInvalidSortDirection_returns400() throws Exception {
        mockMvc.perform(get("/api/plants-library/search")
                        .param("query", "aloe")
                        .param("direction", "INVALID_DIRECTION"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to search plants"));
    }

    @Test
    void getPlantsByType_withInvalidSortDirection_returns400() throws Exception {
        mockMvc.perform(get("/api/plants-library/type/succulent").param("direction", "INVALID_DIRECTION"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plants by type"));
    }

    @Test
    void getPlantsByLifeCycle_withInvalidSortDirection_returns400() throws Exception {
        mockMvc.perform(get("/api/plants-library/life-cycle/perennial").param("direction", "INVALID_DIRECTION"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plants by life cycle"));
    }

    @Test
    void getMedicinalPlants_withInvalidSortDirection_returns400() throws Exception {
        mockMvc.perform(get("/api/plants-library/medicinal").param("direction", "INVALID_DIRECTION"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve medicinal plants"));
    }

    @Test
    void testCreatePlant_withInvalidPayload_returnsBadRequest() throws Exception {
        // Create a plant with missing required field (commonName)
        PlantsLibraryDTO invalidPlant = PlantsLibraryDTO.builder()
                .scientificName("Test Scientific Name")
                .build();

        mockMvc.perform(post("/api/plants-library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPlant)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePlant_withInvalidPayload_returnsBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        // Create a plant with missing required field (commonName)
        PlantsLibraryDTO invalidPlant = PlantsLibraryDTO.builder()
                .scientificName("Test Scientific Name")
                .build();

        mockMvc.perform(put("/api/plants-library/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPlant)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetPlantById_withInvalidUUID_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/plants-library/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePlant_withInvalidUUID_returnsBadRequest() throws Exception {
        PlantsLibraryDTO request =
                PlantsLibraryDTO.builder().commonName("Test Plant").build();

        mockMvc.perform(put("/api/plants-library/not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeletePlant_withInvalidUUID_returnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/plants-library/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void searchPlantsAdvanced_withComprehensiveParameters_returnResults() throws Exception {
        // Test with a more comprehensive set of search parameters
        UUID id = UUID.randomUUID();
        PlantsLibraryDTO dto = PlantsLibraryDTO.builder()
                .id(id)
                .commonName("Test Plant")
                .scientificName("Testus plantus")
                .origin("Test Origin")
                .plantType("Test Type")
                .climate("Warm")
                .lifeCycle("Annual")
                .wateringFrequency("Medium")
                .soilType("Loamy")
                .size("Medium")
                .sunlightRequirement("Full sun")
                .growthRate("Fast")
                .idealPlace("Indoor")
                .careLevel("Easy")
                .bestPlantingSeason("Spring")
                .timeToHarvest(60.0)
                .flower(true)
                .fruit(true)
                .medicinal(true)
                .build();

        Page<PlantsLibraryDTO> page = new PageImpl<>(List.of(dto));
        given(service.searchPlantsAdvanced(any(), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/plants-library/search/advanced")
                        .param("commonName", "Test")
                        .param("scientificName", "Testus")
                        .param("origin", "Test")
                        .param("plantType", "Test Type")
                        .param("climate", "Warm")
                        .param("lifeCycle", "Annual")
                        .param("wateringFrequency", "Medium")
                        .param("soilType", "Loamy")
                        .param("size", "Medium")
                        .param("sunlightRequirement", "Full sun")
                        .param("growthRate", "Fast")
                        .param("idealPlace", "Indoor")
                        .param("careLevel", "Easy")
                        .param("bestPlantingSeason", "Spring")
                        .param("timeToHarvest", "60.0")
                        .param("flower", "true")
                        .param("fruit", "true")
                        .param("medicinal", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].commonName").value("Test Plant"))
                .andExpect(jsonPath("$.data.content[0].scientificName").value("Testus plantus"));

        // Verify that all parameters were captured in the criteria object
        verify(service)
                .searchPlantsAdvanced(
                        argThat(criteria -> "Test".equals(criteria.getCommonName())
                                && "Testus".equals(criteria.getScientificName())
                                && "Test".equals(criteria.getOrigin())
                                && "Test Type".equals(criteria.getPlantType())
                                && "Warm".equals(criteria.getClimate())
                                && "Annual".equals(criteria.getLifeCycle())
                                && "Medium".equals(criteria.getWateringFrequency())
                                && "Loamy".equals(criteria.getSoilType())
                                && "Medium".equals(criteria.getSize())
                                && "Full sun".equals(criteria.getSunlightRequirement())
                                && "Fast".equals(criteria.getGrowthRate())
                                && "Indoor".equals(criteria.getIdealPlace())
                                && "Easy".equals(criteria.getCareLevel())
                                && "Spring".equals(criteria.getBestPlantingSeason())
                                && Double.valueOf(60.0).equals(criteria.getTimeToHarvest())
                                && Boolean.TRUE.equals(criteria.getFlower())
                                && Boolean.TRUE.equals(criteria.getFruit())
                                && Boolean.TRUE.equals(criteria.getMedicinal())),
                        any(Pageable.class));
    }
}
