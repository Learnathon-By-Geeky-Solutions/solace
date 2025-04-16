package dev.solace.twiggle.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public List<String> deserialize(JsonParser parser, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        
        JsonToken token = parser.getCurrentToken();
        
        if (token == JsonToken.START_ARRAY) {
            // If it's already an array, use default deserialization
            List<String> result = new ArrayList<>();
            
            // Move to the first element
            token = parser.nextToken();
            
            // Read each element until we reach the end of the array
            while (token != JsonToken.END_ARRAY) {
                if (token == JsonToken.VALUE_STRING) {
                    result.add(parser.getText());
                } else {
                    // Handle non-string values (could be numbers, booleans, etc.)
                    result.add(parser.getValueAsString());
                }
                token = parser.nextToken();
            }
            
            return result;
        } else if (token == JsonToken.VALUE_STRING) {
            // If it's a string, split by commas and/or semicolons
            String text = parser.getText();
            
            if (text == null || text.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            // Split by commas and semicolons, clean up whitespace
            String[] items = text.split("[,;]");
            return Arrays.stream(items)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        } else if (token == JsonToken.VALUE_NULL) {
            return new ArrayList<>();
        } else {
            // For any other token types, just convert to string
            log.warn("Unexpected token type for companion_plants: {}", token);
            return List.of(parser.getValueAsString());
        }
    }
} 