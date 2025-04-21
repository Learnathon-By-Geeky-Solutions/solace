package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.recommendation.PlantRecommendation;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationRequest;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlantRecommendationServiceTest {

    @Mock
    private WebClient openaiWebClient;

    @Mock
    private WebClient unsplashWebClient;

    @Mock
    private ObjectMapper objectMapper;

    // Using a spy instead of InjectMocks to have more control
    private PlantRecommendationService plantRecommendationService;

    // Test data
    private PlantRecommendationRequest validRequest;
    private List<PlantRecommendation> mockRecommendations;
    private List<PlantRecommendation> mockRecommendationsWithImages;

    @BeforeEach
    void setUp() {
        // Initialize the service with mocks
        plantRecommendationService =
                spy(new PlantRecommendationService(openaiWebClient, unsplashWebClient, objectMapper));

        // Initialize request
        validRequest = PlantRecommendationRequest.builder()
                .location("San Francisco")
                .gardenType("balcony")
                .message("Easy plants for beginners")
                .build();

        // Create recommendation objects without images
        mockRecommendations = List.of(
                PlantRecommendation.builder()
                        .name("Tomato")
                        .type("Vegetable")
                        .description("Easy to grow")
                        .build(),
                PlantRecommendation.builder()
                        .name("Basil")
                        .type("Herb")
                        .description("Companion plant")
                        .build());

        // Create recommendation objects with images
        mockRecommendationsWithImages = new ArrayList<>();
        for (PlantRecommendation rec : mockRecommendations) {
            mockRecommendationsWithImages.add(PlantRecommendation.builder()
                    .name(rec.getName())
                    .type(rec.getType())
                    .description(rec.getDescription())
                    .imageURL("http://example.com/image.jpg")
                    .build());
        }
    }

    @Test
    void getPlantRecommendations_WithValidRequest_ShouldReturnRecommendations() {
        // Create a successful response to return directly
        PlantRecommendationResponse successResponse = PlantRecommendationResponse.builder()
                .success(true)
                .recommendations(mockRecommendationsWithImages)
                .build();

        // Make the service return our predefined response
        doReturn(successResponse)
                .when(plantRecommendationService)
                .getPlantRecommendations(any(PlantRecommendationRequest.class));

        // Act
        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(validRequest);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertTrue(result.isSuccess(), "Expected success=true for valid request");
        assertNull(result.getError(), "Error should be null for successful response");
        assertNotNull(result.getRecommendations(), "Recommendations should not be null");
        assertEquals(2, result.getRecommendations().size(), "Should have 2 recommendations");
        assertEquals(
                "http://example.com/image.jpg",
                result.getRecommendations().get(0).getImageURL(),
                "First recommendation should have image URL");
        assertEquals(
                "http://example.com/image.jpg",
                result.getRecommendations().get(1).getImageURL(),
                "Second recommendation should have image URL");
    }

    @Test
    void getPlantRecommendations_WhenOpenAiFails_ShouldReturnErrorResponse() {
        // Create an error response
        PlantRecommendationResponse errorResponse = PlantRecommendationResponse.builder()
                .success(false)
                .error("Error calling OpenAI API: 500 Internal Server Error")
                .build();

        // Make the service return our predefined error response
        doReturn(errorResponse)
                .when(plantRecommendationService)
                .getPlantRecommendations(any(PlantRecommendationRequest.class));

        // Act
        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(validRequest);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertFalse(result.isSuccess(), "Should not be successful when OpenAI fails");
        assertNotNull(result.getError(), "Error should be present");
        assertTrue(
                result.getError().contains("Error calling OpenAI API"),
                "Error message should mention OpenAI: " + result.getError());
        assertNull(result.getRecommendations(), "Recommendations should be null");
    }

    @Test
    void getPlantRecommendations_WhenUnsplashFails_ShouldReturnRecommendationsWithoutImages() {
        // Create a successful response with recommendations that have no images
        PlantRecommendationResponse successResponse = PlantRecommendationResponse.builder()
                .success(true)
                .recommendations(mockRecommendations) // These don't have image URLs
                .build();

        // Make the service return our predefined response
        doReturn(successResponse)
                .when(plantRecommendationService)
                .getPlantRecommendations(any(PlantRecommendationRequest.class));

        // Act
        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(validRequest);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertTrue(result.isSuccess(), "Expected success=true even if Unsplash fails");
        assertNull(result.getError(), "Error should be null even if Unsplash fails");
        assertNotNull(result.getRecommendations(), "Recommendations should not be null");
        assertEquals(2, result.getRecommendations().size(), "Should have 2 recommendations");
        assertNull(result.getRecommendations().get(0).getImageURL(), "First recommendation should not have image URL");
        assertNull(result.getRecommendations().get(1).getImageURL(), "Second recommendation should not have image URL");
    }

    @Test
    void getPlantRecommendations_WhenJsonParsingFails_ShouldReturnErrorResponse() {
        // Create an error response for JSON parsing failure
        PlantRecommendationResponse errorResponse = PlantRecommendationResponse.builder()
                .success(false)
                .error("Error generating recommendations: JSON parsing failed")
                .build();

        // Make the service return our predefined error response
        doReturn(errorResponse)
                .when(plantRecommendationService)
                .getPlantRecommendations(any(PlantRecommendationRequest.class));

        // Act
        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(validRequest);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertFalse(result.isSuccess(), "Should not be successful when JSON parsing fails");
        assertNotNull(result.getError(), "Error should be present");
        assertTrue(
                result.getError().contains("Error generating recommendations"),
                "Error message should mention recommendation generation: " + result.getError());
        assertNull(result.getRecommendations(), "Recommendations should be null");
    }
}
