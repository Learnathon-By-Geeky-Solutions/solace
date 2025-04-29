package dev.solace.twiggle.service.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.recommendation.PlantRecommendation;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class UnsplashClientTest {

    @Mock
    private WebClient unsplashWebClient;

    @Mock
    private ObjectMapper objectMapper;

    private UnsplashClient unsplashClient;

    @BeforeEach
    void setUp() {
        unsplashClient = new UnsplashClient(unsplashWebClient, objectMapper);
    }

    @Test
    void fetchPlantImage_ShouldSetImageUrlWhenSuccessful() throws Exception {
        // Arrange
        PlantRecommendation plant = PlantRecommendation.builder()
                .name("Test Plant")
                .description("Test")
                .type("Test")
                .build();

        WebClient.RequestHeadersUriSpec requestSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        JsonNode rootNode = mock(JsonNode.class);
        JsonNode resultsNode = mock(JsonNode.class);
        JsonNode firstResult = mock(JsonNode.class);
        JsonNode urlsNode = mock(JsonNode.class);
        JsonNode smallUrlNode = mock(JsonNode.class);

        when(unsplashWebClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(any(Function.class))).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just("{\"results\":[{\"urls\":{\"small\":\"https://example.com/image.jpg\"}}]}"));

        when(objectMapper.readTree(anyString())).thenReturn(rootNode);
        when(rootNode.path("results")).thenReturn(resultsNode);
        when(resultsNode.size()).thenReturn(1);
        when(resultsNode.path(0)).thenReturn(firstResult);
        when(firstResult.path("urls")).thenReturn(urlsNode);
        when(urlsNode.path("small")).thenReturn(smallUrlNode);
        when(smallUrlNode.asText()).thenReturn("https://example.com/image.jpg");

        // Act
        unsplashClient.fetchPlantImage(plant);

        // Assert
        assertEquals("https://example.com/image.jpg", plant.getImageURL());
    }

    @Test
    void fetchPlantImage_ShouldHandleEmptyResults() throws Exception {
        // Arrange
        PlantRecommendation plant = PlantRecommendation.builder()
                .name("Test Plant")
                .description("Test")
                .type("Test")
                .build();

        WebClient.RequestHeadersUriSpec requestSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        JsonNode rootNode = mock(JsonNode.class);
        JsonNode resultsNode = mock(JsonNode.class);

        when(unsplashWebClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(any(Function.class))).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{\"results\":[]}"));

        when(objectMapper.readTree(anyString())).thenReturn(rootNode);
        when(rootNode.path("results")).thenReturn(resultsNode);
        when(resultsNode.size()).thenReturn(0);

        // Act
        unsplashClient.fetchPlantImage(plant);

        // Assert
        assertNull(plant.getImageURL());
    }

    @Test
    void fetchPlantImage_ShouldHandleExceptions() {
        // Arrange
        PlantRecommendation plant = PlantRecommendation.builder()
                .name("Test Plant")
                .description("Test")
                .type("Test")
                .build();

        when(unsplashWebClient.get()).thenThrow(new RuntimeException("API Error"));

        // Act
        unsplashClient.fetchPlantImage(plant);

        // Assert - should not throw exception and imageURL should remain null
        assertNull(plant.getImageURL());
    }
}
