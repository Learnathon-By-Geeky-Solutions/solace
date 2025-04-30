package dev.solace.twiggle.service.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.recommendation.PlantRecommendation;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JsonUtilsTest {

    @Mock
    private ObjectMapper objectMapper;

    private JsonUtils jsonUtils;

    @BeforeEach
    void setUp() {
        jsonUtils = new JsonUtils(objectMapper);
    }

    @Test
    void extractRecommendationsFromOpenAiResponse_ShouldParseValidJson() throws JsonProcessingException {
        // Arrange
        String validJson =
                "{\"choices\":[{\"message\":{\"content\":\"[{\\\"name\\\":\\\"Tomato\\\",\\\"type\\\":\\\"Vegetable\\\",\\\"description\\\":\\\"Easy to grow\\\"}]\"}}]}";

        JsonNode rootNode = mock(JsonNode.class);
        JsonNode choicesNode = mock(JsonNode.class);
        JsonNode firstChoice = mock(JsonNode.class);
        JsonNode messageNode = mock(JsonNode.class);
        JsonNode contentNode = mock(JsonNode.class);

        when(objectMapper.readTree(validJson)).thenReturn(rootNode);
        when(rootNode.path("choices")).thenReturn(choicesNode);
        when(choicesNode.path(0)).thenReturn(firstChoice);
        when(firstChoice.path("message")).thenReturn(messageNode);
        when(messageNode.path("content")).thenReturn(contentNode);
        when(contentNode.asText())
                .thenReturn("[{\"name\":\"Tomato\",\"type\":\"Vegetable\",\"description\":\"Easy to grow\"}]");

        List<PlantRecommendation> expectedRecommendations = List.of(PlantRecommendation.builder()
                .name("Tomato")
                .type("Vegetable")
                .description("Easy to grow")
                .build());

        when(objectMapper.readValue(
                        eq("[{\"name\":\"Tomato\",\"type\":\"Vegetable\",\"description\":\"Easy to grow\"}]"),
                        any(TypeReference.class)))
                .thenReturn(expectedRecommendations);

        // Act
        List<PlantRecommendation> result = jsonUtils.extractRecommendationsFromOpenAiResponse(validJson);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tomato", result.get(0).getName());
    }

    @Test
    void cleanAndRepairJson_ShouldHandleNullContent() {
        assertEquals("[]", jsonUtils.cleanAndRepairJson(null));
    }

    @Test
    void cleanAndRepairJson_ShouldHandleEmptyContent() {
        assertEquals("[]", jsonUtils.cleanAndRepairJson(""));
        assertEquals("[]", jsonUtils.cleanAndRepairJson("   "));
    }

    @Test
    void cleanAndRepairJson_ShouldExtractJsonArrayFromText() throws JsonProcessingException {
        // Arrange
        String textWithJson = "Here is some text with an embedded JSON array: [{\"name\":\"Tomato\"}]";

        when(objectMapper.readTree("[{\"name\":\"Tomato\"}]")).thenReturn(mock(JsonNode.class));

        // Act
        String result = jsonUtils.cleanAndRepairJson(textWithJson);

        // Assert
        assertEquals("[{\"name\":\"Tomato\"}]", result);
    }

    @Test
    void cleanAndRepairJson_ShouldWrapSingleObjectInArray() throws JsonProcessingException {
        // Arrange
        String singleObject = "{\"name\":\"Tomato\"}";
        String expectedWrapped = "[{\"name\":\"Tomato\"}]";

        // Only stub the initial validation failure
        when(objectMapper.readTree(singleObject)).thenThrow(JsonProcessingException.class);

        // Specifically mock the wrapped response
        when(objectMapper.readTree(expectedWrapped)).thenReturn(mock(JsonNode.class));

        // Act
        String result = jsonUtils.cleanAndRepairJson(singleObject);

        // Assert
        assertTrue(result.startsWith("[{"));
        assertTrue(result.contains("Tomato"));
    }

    @Test
    void cleanAndRepairJson_ShouldRepairBrokenJson() throws JsonProcessingException {
        // Arrange
        String brokenJson = "[{\"name\":\"Tomato\",\"type\":\"Vegetable\",";

        // First validation fails
        when(objectMapper.readTree(brokenJson)).thenThrow(JsonProcessingException.class);
        // After repair, validation succeeds
        when(objectMapper.readTree("[{\"name\":\"Tomato\",\"type\":\"Vegetable\",}]"))
                .thenReturn(mock(JsonNode.class));

        // Act
        String result = jsonUtils.cleanAndRepairJson(brokenJson);

        // Assert
        assertTrue(result.contains("Tomato"));
        assertTrue(result.contains("Vegetable"));
    }

    @Test
    void cleanAndRepairJson_ShouldReturnEmptyArrayWhenRepairFails() throws JsonProcessingException {
        // Arrange
        String invalidJson = "{invalid}";

        // First validation fails
        when(objectMapper.readTree(invalidJson)).thenThrow(JsonProcessingException.class);

        // The key issue is that during the repair process, it's trying to check if [invalidJson] is valid
        // We need to make this validation fail too
        when(objectMapper.readTree("[{invalid}]")).thenThrow(JsonProcessingException.class);

        // And also mock the repaired version with added braces
        when(objectMapper.readTree("{invalid}}")).thenThrow(JsonProcessingException.class);
        when(objectMapper.readTree("[{invalid}}]")).thenThrow(JsonProcessingException.class);

        // Act
        String result = jsonUtils.cleanAndRepairJson(invalidJson);

        // Assert
        assertEquals("[]", result);
    }

    @Test
    void cleanAndRepairJson_ShouldReturnEmptyArrayWhenTooManyUnmatchedBraces() throws JsonProcessingException {
        // Arrange
        String lotsOfOpenBraces = "{".repeat(101); // 101 open braces

        // Mock initial validation to fail
        when(objectMapper.readTree(lotsOfOpenBraces)).thenThrow(JsonProcessingException.class);
        // Note: We don't need to mock the repair attempt's validation, as the code should return "[]" before that

        // Act
        String result = jsonUtils.cleanAndRepairJson(lotsOfOpenBraces);

        // Assert
        assertEquals("[]", result);
    }

    @Test
    void cleanAndRepairJson_ShouldReturnEmptyArrayWhenTooManyUnmatchedBrackets() throws JsonProcessingException {
        // Arrange
        String lotsOfOpenBrackets = "[".repeat(101); // 101 open brackets

        // Mock initial validation to fail
        when(objectMapper.readTree(lotsOfOpenBrackets)).thenThrow(JsonProcessingException.class);

        // Act
        String result = jsonUtils.cleanAndRepairJson(lotsOfOpenBrackets);

        // Assert
        assertEquals("[]", result);
    }
}
