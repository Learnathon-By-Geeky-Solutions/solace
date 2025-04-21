package dev.solace.twiggle.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
@Getter
public class PlantApiConfig {

    @Value("${plants.api.key:}")
    private String apiKey;

    private static final String BASE_URL = "https://perenual.com/api/v2";

    // Instead of creating a new RestTemplate bean, inject the one from AppConfig
    private final RestTemplate restTemplate;

    // Constructor injection to get the RestTemplate from AppConfig
    public PlantApiConfig(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank() || "your_api_key_here".equals(apiKey)) {
            log.warn("Perenual API key is not configured properly. Plant API functionality will not work.");
        } else {
            log.info("Perenual Plant API configured successfully");
        }
    }

    public String getSpeciesListUrl() {
        return BASE_URL + "/species-list";
    }

    public String getSpeciesDetailsUrl(Long id) {
        return BASE_URL + "/species/details/" + id;
    }

    public String getDiseasePestListUrl() {
        return "https://perenual.com/api/pest-disease-list";
    }
}
