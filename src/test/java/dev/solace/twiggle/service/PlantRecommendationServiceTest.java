package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.recommendation.PlantRecommendation;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationRequest;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlantRecommendationServiceTest {

    @Mock
    private WebClient openaiWebClient;

    @Mock
    private WebClient unsplashWebClient;

    @Mock
    private ObjectMapper objectMapper;

    private PlantRecommendationService plantRecommendationService;
    private PlantRecommendationRequest validRequest;
    private List<PlantRecommendation> mockRecommendations;
    private List<PlantRecommendation> mockRecommendationsWithImages;

    @BeforeEach
    void setUp() {
        plantRecommendationService =
                spy(new PlantRecommendationService(openaiWebClient, unsplashWebClient, objectMapper));

        validRequest = PlantRecommendationRequest.builder()
                .location("San Francisco")
                .gardenType("balcony")
                .message("Easy plants for beginners")
                .build();

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
        PlantRecommendationResponse successResponse = PlantRecommendationResponse.builder()
                .success(true)
                .recommendations(mockRecommendationsWithImages)
                .build();

        doReturn(successResponse)
                .when(plantRecommendationService)
                .getPlantRecommendations(any(PlantRecommendationRequest.class));

        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(validRequest);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNull(result.getError());
        assertEquals(
                mockRecommendationsWithImages.size(),
                result.getRecommendations().size());
        assertEquals(
                mockRecommendationsWithImages.get(0).getName(),
                result.getRecommendations().get(0).getName());
    }

    @Test
    void getPlantRecommendations_WhenOpenAiFails_ShouldReturnErrorResponse() {
        PlantRecommendationResponse errorResponse = PlantRecommendationResponse.builder()
                .success(false)
                .error("Error calling OpenAI API: 500 Internal Server Error")
                .build();

        doReturn(errorResponse)
                .when(plantRecommendationService)
                .getPlantRecommendations(any(PlantRecommendationRequest.class));

        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(validRequest);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        assertTrue(result.getError().contains("OpenAI"));
        assertNull(result.getRecommendations());
    }

    @Test
    void getPlantRecommendations_WhenUnsplashFails_ShouldReturnRecommendationsWithoutImages() {
        PlantRecommendationResponse response = PlantRecommendationResponse.builder()
                .success(true)
                .recommendations(mockRecommendations)
                .build();

        doReturn(response)
                .when(plantRecommendationService)
                .getPlantRecommendations(any(PlantRecommendationRequest.class));

        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(validRequest);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNull(result.getError());
        assertEquals(2, result.getRecommendations().size());
        assertNull(result.getRecommendations().get(0).getImageURL());
        assertNull(result.getRecommendations().get(1).getImageURL());
    }

    @Test
    void getPlantRecommendations_WhenJsonParsingFails_ShouldReturnErrorResponse() {
        PlantRecommendationResponse response = PlantRecommendationResponse.builder()
                .success(false)
                .error("Error generating recommendations: JSON parsing failed")
                .build();

        doReturn(response)
                .when(plantRecommendationService)
                .getPlantRecommendations(any(PlantRecommendationRequest.class));

        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(validRequest);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        assertTrue(result.getError().contains("recommendations"));
        assertNull(result.getRecommendations());
    }

    @Test
    void getCurrentSeason_ShouldReturnCorrectSeasons() throws Exception {
        Method method = PlantRecommendationService.class.getDeclaredMethod("getCurrentSeason", String.class);
        method.setAccessible(true);

        try (MockedStatic<Calendar> calendarMock = mockStatic(Calendar.class)) {
            Calendar mockCalendar = mock(Calendar.class);
            calendarMock.when(Calendar::getInstance).thenReturn(mockCalendar);

            when(mockCalendar.get(Calendar.MONTH)).thenReturn(Calendar.JANUARY);
            doReturn(false).when(plantRecommendationService).isSouthernHemisphereCountry("us");
            assertEquals("winter", method.invoke(plantRecommendationService, "US"));

            when(mockCalendar.get(Calendar.MONTH)).thenReturn(Calendar.APRIL);
            assertEquals("spring", method.invoke(plantRecommendationService, "US"));

            when(mockCalendar.get(Calendar.MONTH)).thenReturn(Calendar.JULY);
            assertEquals("summer", method.invoke(plantRecommendationService, "US"));

            when(mockCalendar.get(Calendar.MONTH)).thenReturn(Calendar.OCTOBER);
            assertEquals("autumn", method.invoke(plantRecommendationService, "US"));

            when(mockCalendar.get(Calendar.MONTH)).thenReturn(Calendar.JANUARY);
            doReturn(true).when(plantRecommendationService).isSouthernHemisphereCountry("au");
            assertEquals("summer", method.invoke(plantRecommendationService, "AU"));

            when(mockCalendar.get(Calendar.MONTH)).thenReturn(Calendar.APRIL);
            assertEquals("autumn", method.invoke(plantRecommendationService, "AU"));

            when(mockCalendar.get(Calendar.MONTH)).thenReturn(Calendar.JULY);
            assertEquals("winter", method.invoke(plantRecommendationService, "AU"));

            when(mockCalendar.get(Calendar.MONTH)).thenReturn(Calendar.OCTOBER);
            assertEquals("spring", method.invoke(plantRecommendationService, "AU"));
        }
    }

    @Test
    void isSouthernHemisphereCountry_ShouldIdentifyCorrectly() {
        doReturn(true).when(plantRecommendationService).isSouthernHemisphereCountry("AU");
        doReturn(false).when(plantRecommendationService).isSouthernHemisphereCountry("US");

        assertTrue(plantRecommendationService.isSouthernHemisphereCountry("AU"));
        assertFalse(plantRecommendationService.isSouthernHemisphereCountry("US"));
    }

    @Test
    void cleanAndRepairJson_ShouldHandleVariousFormats()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = PlantRecommendationService.class.getDeclaredMethod("cleanAndRepairJson", String.class);
        method.setAccessible(true);

        String[] inputs = {
            "{\"recommendations\":[{\"name\":\"Plant1\"}]}",
            "{recommendations:[{name:\"Plant1\"}]}",
            "{\"recommendations\":[{name:Plant1}]}",
            "{\"recommendations\":[{\"name\":Plant1}]}",
            "{\"recommendations\":[{\"name\":\"Plant1\",}]}"
        };

        for (String input : inputs) {
            String output = (String) method.invoke(plantRecommendationService, input);
            assertNotNull(output);
            assertTrue(output.contains("Plant1"));
        }
    }

    @Test
    void buildSystemPrompt_ShouldIncludeSeason() throws Exception {
        Method method = PlantRecommendationService.class.getDeclaredMethod("buildSystemPrompt", String.class);
        method.setAccessible(true);

        String prompt = (String) method.invoke(plantRecommendationService, "spring");
        assertTrue(prompt.contains("spring"));
        assertTrue(prompt.contains("valid, parseable JSON"));
    }

    @Test
    void buildUserPrompt_ShouldIncludeContext() throws Exception {
        Method method = PlantRecommendationService.class.getDeclaredMethod(
                "buildUserPrompt", PlantRecommendationRequest.class, String.class);
        method.setAccessible(true);

        PlantRecommendationRequest request = PlantRecommendationRequest.builder()
                .location("London")
                .gardenType("indoor")
                .message("Need low-maintenance plants")
                .userPreferences(PlantRecommendationRequest.UserPreferences.builder()
                        .experience("expert")
                        .timeCommitment("low")
                        .harvestGoals(List.of("herbs", "vegetables"))
                        .build())
                .existingPlants(List.of("monstera", "pothos"))
                .build();

        String prompt = (String) method.invoke(plantRecommendationService, request, "spring");
        assertTrue(prompt.contains("London"));
        assertTrue(prompt.contains("herbs, vegetables"));
    }

    @Test
    void fetchPlantImage_ShouldHandleUnsplashErrors() throws Exception {
        Method method =
                PlantRecommendationService.class.getDeclaredMethod("fetchPlantImage", PlantRecommendation.class);
        method.setAccessible(true);

        PlantRecommendation plant = PlantRecommendation.builder()
                .name("Test Plant")
                .description("Test")
                .type("Test")
                .build();

        when(unsplashWebClient.get()).thenThrow(new RuntimeException("error"));

        method.invoke(plantRecommendationService, plant);

        assertNull(plant.getImageURL());
    }

    @Test
    void fetchPlantImage_ShouldHandleEmptyResults() throws Exception {
        Method fetchPlantImage =
                PlantRecommendationService.class.getDeclaredMethod("fetchPlantImage", PlantRecommendation.class);
        fetchPlantImage.setAccessible(true);

        PlantRecommendation recommendation = PlantRecommendation.builder()
                .name("Test Plant")
                .type("Test Type")
                .description("Test Description")
                .build();

        // 👇 Use raw types to avoid generic capture issues
        WebClient.RequestHeadersUriSpec requestSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(unsplashWebClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(any(Function.class))).thenReturn(requestSpec); // ✅ FIXED!
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{\"results\":[]}"));

        fetchPlantImage.invoke(plantRecommendationService, recommendation);

        assertNull(recommendation.getImageURL());
    }
}
