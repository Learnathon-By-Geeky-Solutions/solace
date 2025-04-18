package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.recommendation.PlantRecommendation;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationRequest;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationResponse;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

// Use lenient stubbing for all tests to avoid UnnecessaryStubbingException
@ExtendWith(MockitoExtension.class)
class PlantRecommendationServiceTest {

    @Mock
    private WebClient openaiWebClient;

    @Mock
    private WebClient unsplashWebClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PlantRecommendationService plantRecommendationService;

    private PlantRecommendationRequest request;
    private String mockOpenAiResponseJson;
    private String mockUnsplashResponseJson;
    private String mockParsedRecommendationsJson;
    private List<PlantRecommendation> mockParsedRecommendations;

    // WebClient chain mocks
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    private WebClient.RequestBodySpec requestBodySpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.RequestHeadersSpec unsplashHeadersSpec;
    private WebClient.ResponseSpec unsplashResponseSpec;

    @BeforeEach
    void setUp() {
        // Set up mockito to be lenient for all tests
        lenient().when(mock(Object.class).toString()).thenCallRealMethod();

        // Initialize test data
        request = PlantRecommendationRequest.builder()
                .location("San Francisco")
                .gardenType("balcony")
                .message("Easy plants for beginners")
                .build();

        mockOpenAiResponseJson =
                "{\"choices\": [{\"message\": {\"content\": \"[{\\\"name\\\":\\\"Tomato\\\",\\\"type\\\":\\\"Vegetable\\\",\\\"description\\\":\\\"Easy\\\"},{\\\"name\\\":\\\"Basil\\\",\\\"type\\\":\\\"Herb\\\",\\\"description\\\":\\\"Companion\\\"}]\"}}]}";
        mockUnsplashResponseJson = "{\"results\": [{\"urls\": {\"small\": \"http://image.url/small.jpg\"}}]}";
        mockParsedRecommendationsJson =
                "[{\"name\":\"Tomato\",\"type\":\"Vegetable\",\"description\":\"Easy\"},{\"name\":\"Basil\",\"type\":\"Herb\",\"description\":\"Companion\"}]";

        mockParsedRecommendations = List.of(
                PlantRecommendation.builder()
                        .name("Tomato")
                        .type("Vegetable")
                        .description("Easy")
                        .build(),
                PlantRecommendation.builder()
                        .name("Basil")
                        .type("Herb")
                        .description("Companion")
                        .build());

        // Initialize WebClient chain mocks
        requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        requestBodySpec = mock(WebClient.RequestBodySpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);
        requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        unsplashHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        unsplashResponseSpec = mock(WebClient.ResponseSpec.class);
    }

    @Test
    void getPlantRecommendations_WithValidRequest_ShouldReturnRecommendations() throws JsonProcessingException {
        // Setup OpenAI chain
        when(openaiWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockOpenAiResponseJson));

        // Setup OpenAI response parsing
        JsonNode mockJsonNode = mock(JsonNode.class);
        JsonNode mockChoicesNode = mock(JsonNode.class);
        JsonNode mockFirstChoiceNode = mock(JsonNode.class);
        JsonNode mockMessageNode = mock(JsonNode.class);
        JsonNode mockContentNode = mock(JsonNode.class);

        when(objectMapper.readTree(mockOpenAiResponseJson)).thenReturn(mockJsonNode);
        when(mockJsonNode.path("choices")).thenReturn(mockChoicesNode);
        when(mockChoicesNode.path(0)).thenReturn(mockFirstChoiceNode);
        when(mockFirstChoiceNode.path("message")).thenReturn(mockMessageNode);
        when(mockMessageNode.path("content")).thenReturn(mockContentNode);
        when(mockContentNode.asText()).thenReturn(mockParsedRecommendationsJson);

        // Critical fix: Mock readValue for both the specific string and any string
        // This handles the cleanAndRepairJson method's behavior
        when(objectMapper.readValue(eq(mockParsedRecommendationsJson), any(TypeReference.class)))
                .thenReturn(mockParsedRecommendations);
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(mockParsedRecommendations);

        // Setup Unsplash chain
        when(unsplashWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(unsplashHeadersSpec);
        when(unsplashHeadersSpec.retrieve()).thenReturn(unsplashResponseSpec);
        when(unsplashResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockUnsplashResponseJson));

        // Setup Unsplash response parsing
        JsonNode mockUnsplashNode = mock(JsonNode.class);
        JsonNode mockResultsNode = mock(JsonNode.class);
        JsonNode mockFirstResultNode = mock(JsonNode.class);
        JsonNode mockUrlsNode = mock(JsonNode.class);
        JsonNode mockSmallUrlNode = mock(JsonNode.class);

        when(objectMapper.readTree(mockUnsplashResponseJson)).thenReturn(mockUnsplashNode);
        when(mockUnsplashNode.path("results")).thenReturn(mockResultsNode);
        when(mockResultsNode.size()).thenReturn(1);
        when(mockResultsNode.path(0)).thenReturn(mockFirstResultNode);
        when(mockFirstResultNode.path("urls")).thenReturn(mockUrlsNode);
        when(mockUrlsNode.path("small")).thenReturn(mockSmallUrlNode);
        when(mockSmallUrlNode.asText()).thenReturn("http://image.url/small.jpg");

        // Act
        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess(), "Expected success=true for valid request");
        assertNull(result.getError());
        assertNotNull(result.getRecommendations());
        assertEquals(2, result.getRecommendations().size());
        assertEquals(
                "http://image.url/small.jpg", result.getRecommendations().get(0).getImageURL());
        assertEquals(
                "http://image.url/small.jpg", result.getRecommendations().get(1).getImageURL());
    }

    @Test
    void getPlantRecommendations_WhenOpenAiFails_ShouldReturnErrorResponse() {
        // Setup OpenAI chain with failure
        when(openaiWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Create and configure the exception
        WebClientResponseException mockException = mock(WebClientResponseException.class);
        when(mockException.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(mockException.getResponseBodyAsString()).thenReturn("OpenAI Error");
        when(mockException.getMessage()).thenReturn("500 OpenAI Error");

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(mockException));

        // Act
        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(request);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        assertTrue(
                result.getError().contains("Error calling OpenAI API: 500 OpenAI Error"),
                "Actual: " + result.getError());
        assertNull(result.getRecommendations());
    }

    @Test
    void getPlantRecommendations_WhenUnsplashFails_ShouldReturnRecommendationsWithoutImages()
            throws JsonProcessingException {
        // Setup OpenAI chain
        when(openaiWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockOpenAiResponseJson));

        // Setup OpenAI response parsing
        JsonNode mockJsonNode = mock(JsonNode.class);
        JsonNode mockChoicesNode = mock(JsonNode.class);
        JsonNode mockFirstChoiceNode = mock(JsonNode.class);
        JsonNode mockMessageNode = mock(JsonNode.class);
        JsonNode mockContentNode = mock(JsonNode.class);

        when(objectMapper.readTree(mockOpenAiResponseJson)).thenReturn(mockJsonNode);
        when(mockJsonNode.path("choices")).thenReturn(mockChoicesNode);
        when(mockChoicesNode.path(0)).thenReturn(mockFirstChoiceNode);
        when(mockFirstChoiceNode.path("message")).thenReturn(mockMessageNode);
        when(mockMessageNode.path("content")).thenReturn(mockContentNode);
        when(mockContentNode.asText()).thenReturn(mockParsedRecommendationsJson);

        // Critical fix: Mock readValue for both the specific string and any string
        when(objectMapper.readValue(eq(mockParsedRecommendationsJson), any(TypeReference.class)))
                .thenReturn(mockParsedRecommendations);
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(mockParsedRecommendations);

        // Setup Unsplash chain with failure
        when(unsplashWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(unsplashHeadersSpec);
        when(unsplashHeadersSpec.retrieve()).thenReturn(unsplashResponseSpec);

        // Create and configure the exception
        WebClientResponseException mockException = mock(WebClientResponseException.class);
        when(unsplashResponseSpec.bodyToMono(String.class)).thenReturn(Mono.error(mockException));

        // Act
        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess(), "Expected success=true even if Unsplash fails");
        assertNull(result.getError());
        assertNotNull(result.getRecommendations());
        assertEquals(2, result.getRecommendations().size());
        assertNull(result.getRecommendations().get(0).getImageURL());
        assertNull(result.getRecommendations().get(1).getImageURL());
    }

    @Test
    void getPlantRecommendations_WhenJsonParsingFails_ShouldReturnErrorResponse() throws JsonProcessingException {
        // Setup OpenAI chain
        when(openaiWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockOpenAiResponseJson));

        // Setup OpenAI response parsing with failure
        JsonNode mockJsonNode = mock(JsonNode.class);
        JsonNode mockChoicesNode = mock(JsonNode.class);
        JsonNode mockFirstChoiceNode = mock(JsonNode.class);
        JsonNode mockMessageNode = mock(JsonNode.class);
        JsonNode mockContentNode = mock(JsonNode.class);

        when(objectMapper.readTree(mockOpenAiResponseJson)).thenReturn(mockJsonNode);
        when(mockJsonNode.path("choices")).thenReturn(mockChoicesNode);
        when(mockChoicesNode.path(0)).thenReturn(mockFirstChoiceNode);
        when(mockFirstChoiceNode.path("message")).thenReturn(mockMessageNode);
        when(mockMessageNode.path("content")).thenReturn(mockContentNode);
        when(mockContentNode.asText()).thenReturn(mockParsedRecommendationsJson);

        // Mock readTree to succeed but readValue to fail
        when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);

        // Critical fix: Throw exception for any string argument in readValue
        when(objectMapper.readValue(any(String.class), any(TypeReference.class)))
                .thenThrow(new JsonProcessingException("Parsing failed") {});

        // Act
        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(request);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        assertTrue(
                result.getError().contains("Error generating recommendations: Parsing failed"),
                "Actual: " + result.getError());
        assertNull(result.getRecommendations());
    }
}
