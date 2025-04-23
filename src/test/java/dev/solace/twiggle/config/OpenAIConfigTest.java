package dev.solace.twiggle.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
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
@SpringBootTest(classes = {OpenAIConfig.class, OpenAIConfigTest.TestConfig.class})
@TestPropertySource(
        properties = {
            "openai.api.key=test-openai-key",
            "openai.api.url=https://test-openai-api.com",
            "unsplash.access.key=test-unsplash-key",
            "unsplash.api.url=https://test-unsplash-api.com"
        })
class OpenAIConfigTest {

    @Autowired
    private WebClient openaiWebClient;

    @Autowired
    private WebClient unsplashWebClient;

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
    void checkApiKeys_shouldLogInfo_whenKeysArePresent() throws Exception {
        // Given - Properties are already injected through @TestPropertySource

        // When
        config.checkApiKeys();

        // Then - We can verify that the methods don't throw exceptions
        // and that private fields are set correctly using reflection
        Field openaiApiKeyField = OpenAIConfig.class.getDeclaredField("openaiApiKey");
        openaiApiKeyField.setAccessible(true);
        String openaiKey = (String) openaiApiKeyField.get(config);
        assertThat(openaiKey).isEqualTo("test-openai-key");

        Field unsplashAccessKeyField = OpenAIConfig.class.getDeclaredField("unsplashAccessKey");
        unsplashAccessKeyField.setAccessible(true);
        String unsplashKey = (String) unsplashAccessKeyField.get(config);
        assertThat(unsplashKey).isEqualTo("test-unsplash-key");
    }

    @Test
    void openaiWebClient_shouldBeConfiguredCorrectly() {
        assertThat(openaiWebClient).isNotNull();
        // Note: Since WebClient is immutable, we can't easily verify headers and baseUrl
        // In integration tests, we'd make actual requests to verify configuration
    }

    @Test
    void unsplashWebClient_shouldBeConfiguredCorrectly() {
        assertThat(unsplashWebClient).isNotNull();
    }
}
