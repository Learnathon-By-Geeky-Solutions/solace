package dev.solace.twiggle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for OpenAI client.
 */
@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key:}")
    private String openaiApiKey;
    
    @Value("${openai.api.url:https://api.openai.com/v1}")
    private String openaiApiUrl;
    
    @Value("${unsplash.access.key:}")
    private String unsplashAccessKey;
    
    @Value("${unsplash.api.url:https://api.unsplash.com}")
    private String unsplashApiUrl;

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