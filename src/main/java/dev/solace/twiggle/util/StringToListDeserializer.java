package dev.solace.twiggle.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom deserializer that can handle either a string or array for companion
 * plants.
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
                    } else if (nextToken == JsonToken.VALUE_NULL) {
                        result.add(null);
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
                        .collect(Collectors.toList());
            }
            case VALUE_NULL -> getNullValue(ctxt);
            case START_OBJECT -> {
                JsonNode node = parser.readValueAsTree();
                StringBuilder sb = new StringBuilder("{");
                node.fields().forEachRemaining(entry -> {
                    if (sb.length() > 1) {
                        sb.append(", ");
                    }
                    sb.append(entry.getKey())
                            .append("=")
                            .append(entry.getValue().asText());
                });
                sb.append("}");
                yield Collections.singletonList(sb.toString());
            }
            default -> {
                String value = parser.getValueAsString();
                yield Collections.singletonList(value);
            }
        };
    }

    @Override
    public List<String> getNullValue(DeserializationContext ctxt) {
        return new ArrayList<>();
    }
}
