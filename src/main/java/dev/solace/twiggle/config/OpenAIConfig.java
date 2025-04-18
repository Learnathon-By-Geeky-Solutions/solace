package dev.solace.twiggle.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for OpenAI and Unsplash clients.
 */
@Configuration
@Slf4j
public class OpenAIConfig {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1}")
    private String openaiApiUrl;

    @Value("${unsplash.access.key:}")
    private String unsplashAccessKey;

    @Value("${unsplash.api.url:https://api.unsplash.com}")
    private String unsplashApiUrl;

    @PostConstruct
    public void checkApiKeys() {
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            log.warn("❌ OpenAI API key is MISSING or BLANK. Check environment or application properties.");
        } else {
            log.info("✅ OpenAI API key is LOADED. (Length: {})", openaiApiKey.length());
        }

        if (unsplashAccessKey == null || unsplashAccessKey.isBlank()) {
            log.warn("❌ Unsplash Access Key is MISSING or BLANK. Check environment or application properties.");
        } else {
            log.info("✅ Unsplash Access Key is LOADED. (Length: {})", unsplashAccessKey.length());
        }
    }

    @Bean
    public WebClient openaiWebClient() {
        return WebClient.builder()
                .baseUrl(openaiApiUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "Bearer " + openaiApiKey)
                .build();
    }

    @Bean
    public WebClient unsplashWebClient() {
        return WebClient.builder()
                .baseUrl(unsplashApiUrl)
                .defaultHeader("Authorization", "Client-ID " + unsplashAccessKey)
                .build();
    }
}
