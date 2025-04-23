package dev.solace.twiggle.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom deserializer that can handle either a string or array for companion plants.
 * Converts both formats to a List of strings.
 */
@Slf4j
public class StringToListDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonToken token = parser.getCurrentToken();

        return switch (token) {
            case START_ARRAY -> {
                List<String> result = new ArrayList<>();
                JsonToken nextToken = parser.nextToken();
                while (nextToken != JsonToken.END_ARRAY) {
                    if (nextToken == JsonToken.VALUE_STRING) {
                        result.add(parser.getText());
                    } else {
                        result.add(parser.getValueAsString());
                    }
                    nextToken = parser.nextToken();
                }
                yield result;
            }
            case VALUE_STRING -> {
                String text = parser.getText();
                if (text == null || text.trim().isEmpty()) {
                    yield new ArrayList<>();
                }
                String[] items = text.split("[,;]");
                yield Arrays.stream(items)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }
            case VALUE_NULL -> getNullValue(ctxt);
            default -> {
                log.warn("Unexpected token type for companion_plants: {}", token);
                yield List.of(parser.getValueAsString());
            }
        };
    }

    @Override
    public List<String> getNullValue(DeserializationContext ctxt) {
        return new ArrayList<>();
    }
}
