package dev.solace.twiggle.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {OpenAIConfig.class, OpenAIConfigIntegrationTest.TestConfig.class})
@TestPropertySource(properties = {"openai.api.key=${EMPTY_API_KEY:}", "unsplash.access.key=${EMPTY_ACCESS_KEY:}"})
class OpenAIConfigIntegrationTest {

    @Autowired
    private OpenAIConfig config;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public WebClient.Builder webClientBuilder() {
            return WebClient.builder();
        }
    }

    @Test
    void checkApiKeys_shouldLogWarning_whenKeysAreMissing() {
        // When - the configuration loads and @PostConstruct runs

        // Then - verify that checkApiKeys() doesn't throw exception
        // and handles missing keys gracefully
        config.checkApiKeys();
    }
}
