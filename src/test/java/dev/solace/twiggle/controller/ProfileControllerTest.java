package dev.solace.twiggle.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.config.RateLimiterConfiguration;
import dev.solace.twiggle.dto.ProfileDTO;
import dev.solace.twiggle.service.ProfileService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfileController.class)
@Import({RateLimiterConfiguration.class, ProfileControllerTest.ProfileTestConfig.class})
class ProfileControllerTest {

    @TestConfiguration
    static class ProfileTestConfig {
        @Bean
        @Primary
        public ProfileService profileService() {
            return org.mockito.Mockito.mock(ProfileService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProfileDTO profile1;
    private ProfileDTO profile2;
    private UUID profile1Uuid;

    @BeforeEach
    void setUp() {
        profile1Uuid = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        profile1 = ProfileDTO.builder()
                .fullName("Garden User One")
                .avatarUrl("https://example.com/avatar1.png")
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .build();

        profile2 = ProfileDTO.builder()
                .fullName("Garden User Two")
                .avatarUrl("https://example.com/avatar2.png")
                .createdAt(now.minusHours(5))
                .updatedAt(now.minusHours(1))
                .build();
    }

    @Test
    void getAllProfiles_ShouldReturnListOfProfiles() throws Exception {
        // Arrange
        List<ProfileDTO> profileList = Arrays.asList(profile1, profile2);
        // Mock the service to return a Page object if the controller expects one
        // Assuming the controller method being tested is the paginated one
        // If testing /all endpoint, adjust mocking and assertions
        Page<ProfileDTO> profilePage = new org.springframework.data.domain.PageImpl<>(profileList);
        when(profileService.findAll(any(Pageable.class))).thenReturn(profilePage);

        // Act & Assert for the paginated endpoint
        mockMvc.perform(
                        get("/api/v1/profiles") // Assuming this is the paginated endpoint
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved profiles"))
                // Check the content array within the data (Page) object
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].fullName").value(profile1.getFullName()))
                .andExpect(jsonPath("$.data.content[1].fullName").value(profile2.getFullName()))
                // Optionally check other Page attributes like totalElements
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void getProfileById_WithValidId_ShouldReturnProfile() throws Exception {
        // Arrange
        when(profileService.findById(any(UUID.class))).thenReturn(Optional.of(profile1));

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/{id}", profile1Uuid).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved profile"))
                .andExpect(jsonPath("$.data.fullName").value(profile1.getFullName()))
                .andExpect(jsonPath("$.data.avatarUrl").value(profile1.getAvatarUrl()));
    }

    @Test
    void getProfileById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        UUID invalidUuid = UUID.randomUUID();
        when(profileService.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/{id}", invalidUuid).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Profile not found")));
    }

    @Test
    void createProfile_WithValidData_ShouldReturnCreatedProfile() throws Exception {
        // Arrange
        ProfileDTO requestDto = ProfileDTO.builder()
                .fullName(profile1.getFullName())
                .avatarUrl(profile1.getAvatarUrl())
                .build();

        ProfileDTO createdDto = ProfileDTO.builder()
                .fullName(profile1.getFullName())
                .avatarUrl(profile1.getAvatarUrl())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(profileService.create(any(ProfileDTO.class))).thenReturn(createdDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Profile created successfully"))
                .andExpect(jsonPath("$.data.fullName").value(createdDto.getFullName()))
                .andExpect(jsonPath("$.data.avatarUrl").value(createdDto.getAvatarUrl()));
    }

    @Test
    void updateProfile_WithValidData_ShouldReturnUpdatedProfile() throws Exception {
        // Arrange
        ProfileDTO requestDto = ProfileDTO.builder()
                .fullName("Updated Name")
                .avatarUrl("https://example.com/updated.png")
                .build();

        ProfileDTO updatedDto = ProfileDTO.builder()
                .fullName(requestDto.getFullName())
                .avatarUrl(requestDto.getAvatarUrl())
                .createdAt(profile1.getCreatedAt())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(profileService.update(any(UUID.class), any(ProfileDTO.class))).thenReturn(Optional.of(updatedDto));

        // Act & Assert
        mockMvc.perform(put("/api/v1/profiles/{id}", profile1Uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                .andExpect(jsonPath("$.data.fullName").value(updatedDto.getFullName()))
                .andExpect(jsonPath("$.data.avatarUrl").value(updatedDto.getAvatarUrl()));
    }

    @Test
    void updateProfile_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        UUID invalidUuid = UUID.randomUUID();
        ProfileDTO requestDto = ProfileDTO.builder()
                .fullName("Update Attempt")
                .avatarUrl("https://example.com/fail.png")
                .build();

        when(profileService.update(any(UUID.class), any(ProfileDTO.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/v1/profiles/{id}", invalidUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProfile_WithValidId_ShouldReturnSuccess() throws Exception {
        // Arrange
        when(profileService.findById(any(UUID.class))).thenReturn(Optional.of(profile1));
        doNothing().when(profileService).delete(any(UUID.class));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/profiles/{id}", profile1Uuid).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Profile deleted successfully"));
    }

    @Test
    void deleteProfile_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        UUID invalidUuid = UUID.randomUUID();
        when(profileService.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/profiles/{id}", invalidUuid).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Profile not found")));
    }

    @Test
    void searchProfiles_WithValidQuery_ShouldReturnMatchingProfiles() throws Exception {
        // Arrange
        String searchQuery = "Garden";
        List<ProfileDTO> profileList = Arrays.asList(profile1, profile2);
        Page<ProfileDTO> profilePage = new org.springframework.data.domain.PageImpl<>(profileList);
        when(profileService.searchProfiles(any(String.class), any(Pageable.class)))
                .thenReturn(profilePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/search")
                        .param("query", searchQuery)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt")
                        .param("direction", "DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully searched profiles"))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].fullName").value(profile1.getFullName()))
                .andExpect(jsonPath("$.data.content[1].fullName").value(profile2.getFullName()));
    }

    @Test
    void searchProfiles_WithEmptyQuery_ShouldReturnAllProfiles() throws Exception {
        // Arrange
        List<ProfileDTO> profileList = Arrays.asList(profile1, profile2);
        Page<ProfileDTO> profilePage = new org.springframework.data.domain.PageImpl<>(profileList);
        when(profileService.searchProfiles(any(String.class), any(Pageable.class)))
                .thenReturn(profilePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/search")
                        .param("query", "")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully searched profiles"))
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

    @Test
    void searchProfilesAdvanced_WithValidCriteria_ShouldReturnRelevantProfiles() throws Exception {
        // Arrange
        String fullName = "Garden";
        String query = "User";
        List<ProfileDTO> profileList = Arrays.asList(profile1, profile2);
        Page<ProfileDTO> profilePage = new org.springframework.data.domain.PageImpl<>(profileList);
        when(profileService.searchProfilesWithRelevance(any(String.class), any(String.class), any(Pageable.class)))
                .thenReturn(profilePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/search/advanced")
                        .param("fullName", fullName)
                        .param("query", query)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully searched profiles with advanced criteria"))
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

    @Test
    void getAllProfilesWithoutPagination_ShouldReturnAllProfiles() throws Exception {
        // Arrange
        List<ProfileDTO> profileList = Arrays.asList(profile1, profile2);
        when(profileService.findAll()).thenReturn(profileList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/all").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved all profiles"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].fullName").value(profile1.getFullName()))
                .andExpect(jsonPath("$.data[1].fullName").value(profile2.getFullName()));
    }

    @Test
    void getProfilesByName_WithValidName_ShouldReturnMatchingProfiles() throws Exception {
        // Arrange
        String fullName = "Garden";
        List<ProfileDTO> profileList = Arrays.asList(profile1, profile2);
        Page<ProfileDTO> profilePage = new org.springframework.data.domain.PageImpl<>(profileList);
        when(profileService.findByFullName(any(String.class), any(Pageable.class)))
                .thenReturn(profilePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/name/{fullName}", fullName)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt")
                        .param("direction", "DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved profiles by name"))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].fullName").value(profile1.getFullName()))
                .andExpect(jsonPath("$.data.content[1].fullName").value(profile2.getFullName()));
    }

    @Test
    void getProfilesByNameWithoutPagination_WithValidName_ShouldReturnAllMatchingProfiles() throws Exception {
        // Arrange
        String fullName = "Garden";
        List<ProfileDTO> profileList = Arrays.asList(profile1, profile2);
        when(profileService.findByFullName(any(String.class))).thenReturn(profileList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/name/{fullName}/all", fullName).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved profiles by name"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].fullName").value(profile1.getFullName()))
                .andExpect(jsonPath("$.data[1].fullName").value(profile2.getFullName()));
    }

    @Test
    void handleCustomException_ShouldReturnAppropriateErrorResponse() throws Exception {
        // Arrange
        UUID testUuid = UUID.randomUUID();
        when(profileService.findById(any(UUID.class)))
                .thenThrow(new IllegalStateException("Profile service is temporarily unavailable"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/{id}", testUuid).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to retrieve profile (Code: INTERNAL_ERROR)"));
    }

    @Test
    void getAllProfiles_WithInvalidSortDirection_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt")
                        .param("direction", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to retrieve profiles (Code: INTERNAL_ERROR)"));
    }

    @Test
    void getAllProfiles_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(profileService.findAll(any(Pageable.class))).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to retrieve profiles (Code: INTERNAL_ERROR)"));
    }

    @Test
    void searchProfiles_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(profileService.searchProfiles(any(String.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/search")
                        .param("query", "test")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to search profiles (Code: INTERNAL_ERROR)"));
    }

    @Test
    void searchProfilesAdvanced_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(profileService.searchProfilesWithRelevance(any(String.class), any(String.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/search/advanced")
                        .param("fullName", "test")
                        .param("query", "test")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message")
                        .value("Failed to perform advanced search on profiles (Code: INTERNAL_ERROR)"));
    }

    @Test
    void getAllProfilesWithoutPagination_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(profileService.findAll()).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/all").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to retrieve profiles (Code: INTERNAL_ERROR)"));
    }

    @Test
    void getProfilesByName_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(profileService.findByFullName(any(String.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/name/{fullName}", "test")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to retrieve profiles by name (Code: INTERNAL_ERROR)"));
    }

    @Test
    void getProfilesByNameWithoutPagination_WhenServiceThrowsException_ShouldReturnInternalServerError()
            throws Exception {
        // Arrange
        when(profileService.findByFullName(any(String.class))).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/name/{fullName}/all", "test").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to retrieve profiles by name (Code: INTERNAL_ERROR)"));
    }

    @Test
    void createProfile_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        ProfileDTO requestDto = ProfileDTO.builder()
                .fullName("Test User")
                .avatarUrl("https://example.com/test.png")
                .build();

        when(profileService.create(any(ProfileDTO.class))).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to create profile (Code: INTERNAL_ERROR)"));
    }

    @Test
    void updateProfile_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        ProfileDTO requestDto = ProfileDTO.builder()
                .fullName("Updated User")
                .avatarUrl("https://example.com/updated.png")
                .build();

        when(profileService.update(any(UUID.class), any(ProfileDTO.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/profiles/{id}", profile1Uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to update profile (Code: INTERNAL_ERROR)"));
    }

    @Test
    void deleteProfile_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(profileService.findById(any(UUID.class))).thenReturn(Optional.of(profile1));
        doThrow(new RuntimeException("Test exception")).when(profileService).delete(any(UUID.class));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/profiles/{id}", profile1Uuid).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to delete profile (Code: INTERNAL_ERROR)"));
    }

    @Test
    void createProfile_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        ProfileDTO.ProfileDTOBuilder builder = ProfileDTO.builder();
        builder.fullName("");
        builder.avatarUrl("https://example.com/test.png"); // Empty name should fail validation
        ProfileDTO requestDto = builder.build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void updateProfile_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        ProfileDTO requestDto = ProfileDTO.builder()
                .fullName("") // Empty name should fail validation
                .avatarUrl("https://example.com/test.png")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/profiles/{id}", profile1Uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }
}
