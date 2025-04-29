package dev.solace.twiggle.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.recommendation.PlantRecommendation;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JsonUtils {
    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    private static final String CONTENT = "content";

    private final ObjectMapper objectMapper;

    public JsonUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<PlantRecommendation> extractRecommendationsFromOpenAiResponse(String openAiResponse)
            throws JsonProcessingException {
        JsonNode responseNode = objectMapper.readTree(openAiResponse);
        String rawContent = responseNode
                .path("choices")
                .path(0)
                .path("message")
                .path(CONTENT)
                .asText()
                .trim();

        String validJson = cleanAndRepairJson(rawContent);
        return objectMapper.readValue(validJson, new TypeReference<>() {});
    }

    public String cleanAndRepairJson(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "[]";
        }

        // Extract JSON array if embedded in text
        if (!content.startsWith("[")) {
            content = extractJsonArray(content);
        }

        try {
            // Validate JSON
            objectMapper.readTree(content);
            return content;
        } catch (JsonProcessingException e) {
            return repairBrokenJson(content, e);
        }
    }

    private String extractJsonArray(String content) {
        Matcher matcher =
                Pattern.compile("\\[\\s*\\{.*?\\}\\s*\\]", Pattern.DOTALL).matcher(content);
        if (matcher.find()) {
            return matcher.group(0);
        } else if (content.startsWith("{") && content.endsWith("}")) {
            return "[" + content + "]";
        } else {
            return "[]"; // Return empty array if no valid JSON structure found
        }
    }

    private String repairBrokenJson(String content, JsonProcessingException error) {
        log.warn("Invalid JSON detected, attempting to fix: {}", error.getMessage());

        // Count braces and brackets
        int openBraces = (int) content.chars().filter(ch -> ch == '{').count();
        int closeBraces = (int) content.chars().filter(ch -> ch == '}').count();
        int openBrackets = (int) content.chars().filter(ch -> ch == '[').count();
        int closeBrackets = (int) content.chars().filter(ch -> ch == ']').count();

        // Safety check to prevent memory issues
        int bracesDiff = openBraces - closeBraces;
        int bracketsDiff = openBrackets - closeBrackets;

        // If we need to add an unreasonable number of braces/brackets,
        // it's likely not a valid JSON structure
        if (bracesDiff > 100 || bracketsDiff > 100) {
            log.warn(
                    "Too many unmatched braces/brackets ({} braces, {} brackets), returning empty array",
                    bracesDiff,
                    bracketsDiff);
            return "[]";
        }

        // Add missing closing brackets/braces
        StringBuilder sb = new StringBuilder(content);
        for (int i = 0; i < bracesDiff && i < 100; i++) {
            sb.append('}');
        }
        for (int i = 0; i < bracketsDiff && i < 100; i++) {
            sb.append(']');
        }

        try {
            // Validate the fixed JSON
            objectMapper.readTree(sb.toString());
            return sb.toString();
        } catch (JsonProcessingException ex) {
            log.warn("Could not fix JSON, returning empty array: {}", ex.getMessage());
            return "[]"; // Return empty array if fixing failed
        }
    }
}
