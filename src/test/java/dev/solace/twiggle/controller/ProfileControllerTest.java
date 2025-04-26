package dev.solace.twiggle.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
                .avatarUrl("http://example.com/avatar1.png")
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .build();

        profile2 = ProfileDTO.builder()
                .fullName("Garden User Two")
                .avatarUrl("http://example.com/avatar2.png")
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
        when(profileService.findById(profile1Uuid)).thenReturn(Optional.of(profile1));

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
        when(profileService.findById(invalidUuid)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/profiles/{id}", invalidUuid).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
                .avatarUrl("http://example.com/updated.png")
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
                .avatarUrl("http://example.com/fail.png")
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
        when(profileService.findById(profile1Uuid)).thenReturn(Optional.of(profile1));
        doNothing().when(profileService).delete(profile1Uuid);

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
        when(profileService.findById(invalidUuid)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/profiles/{id}", invalidUuid).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
