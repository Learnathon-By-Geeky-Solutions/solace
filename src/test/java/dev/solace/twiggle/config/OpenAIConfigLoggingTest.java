package dev.solace.twiggle.config;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

class OpenAIConfigLoggingTest {

    private OpenAIConfig config;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        config = new OpenAIConfig();
        logger = (Logger) LoggerFactory.getLogger(OpenAIConfig.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void checkApiKeys_shouldLogWarning_whenOpenAIKeyIsMissing() {
        // Given
        ReflectionTestUtils.setField(config, "openaiApiKey", "");
        ReflectionTestUtils.setField(config, "unsplashAccessKey", "valid-key");

        // When
        config.checkApiKeys();

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList)
                .filteredOn(event -> event.getLevel() == Level.WARN)
                .extracting(ILoggingEvent::getMessage)
                .contains("❌ OpenAI API key is MISSING or BLANK. Check environment or application properties.");
    }

    @Test
    void checkApiKeys_shouldLogWarning_whenUnsplashKeyIsMissing() {
        // Given
        ReflectionTestUtils.setField(config, "openaiApiKey", "valid-key");
        ReflectionTestUtils.setField(config, "unsplashAccessKey", "");

        // When
        config.checkApiKeys();

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList)
                .filteredOn(event -> event.getLevel() == Level.WARN)
                .extracting(ILoggingEvent::getMessage)
                .contains("❌ Unsplash Access Key is MISSING or BLANK. Check environment or application properties.");
    }

    @Test
    void checkApiKeys_shouldLogInfo_whenBothKeysArePresent() {
        // Given: Use keys with the expected lengths
        ReflectionTestUtils.setField(config, "openaiApiKey", "123456789012345"); // Length: 15
        ReflectionTestUtils.setField(config, "unsplashAccessKey", "12345678901234567"); // Length: 17

        // When
        config.checkApiKeys();

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList)
                .filteredOn(event -> event.getLevel() == Level.INFO)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("✅ OpenAI API key is LOADED. (Length: 15)", "✅ Unsplash Access Key is LOADED. (Length: 17)");
    }

    @Test
    void checkApiKeys_shouldLogWarning_whenBothKeysAreMissing() {
        // Given
        ReflectionTestUtils.setField(config, "openaiApiKey", null);
        ReflectionTestUtils.setField(config, "unsplashAccessKey", "");

        // When
        config.checkApiKeys();

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).filteredOn(event -> event.getLevel() == Level.WARN).hasSize(2);
    }
}
