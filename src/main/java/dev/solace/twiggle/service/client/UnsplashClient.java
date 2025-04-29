package dev.solace.twiggle.service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.recommendation.PlantRecommendation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class UnsplashClient {
    private static final Logger log = LoggerFactory.getLogger(UnsplashClient.class);

    private final WebClient unsplashWebClient;
    private final ObjectMapper objectMapper;

    public UnsplashClient(WebClient unsplashWebClient, ObjectMapper objectMapper) {
        this.unsplashWebClient = unsplashWebClient;
        this.objectMapper = objectMapper;
    }

    public void fetchPlantImage(PlantRecommendation recommendation) {
        try {
            String query = recommendation.getName() + " plant";
            String imageResponse = unsplashWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/photos")
                            .queryParam("query", query)
                            .queryParam("per_page", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (imageResponse != null) {
                JsonNode imageData = objectMapper.readTree(imageResponse);
                if (imageData.path("results").size() > 0) {
                    String imageUrl = imageData
                            .path("results")
                            .path(0)
                            .path("urls")
                            .path("small")
                            .asText();
                    recommendation.setImageURL(imageUrl);
                } else {
                    log.info("No image found for {}", recommendation.getName());
                }
            }
        } catch (Exception e) {
            log.error("Error fetching image for {}: {}", recommendation.getName(), e.getMessage(), e);
        }
    }
}
