package dev.solace.twiggle.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StringToListDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(List.class, new StringToListDeserializer());
        objectMapper.registerModule(module);
    }

    @Test
    void testDeserializeArray() throws JsonProcessingException {
        String json = "[\"Tomato\", \"Carrot\"]";
        List<String> result = objectMapper.readValue(json, List.class);
        assertEquals(List.of("Tomato", "Carrot"), result);
    }

    @Test
    void testDeserializeCommaSeparatedString() throws JsonProcessingException {
        String json = "\"Tomato, Carrot, Onion\"";
        List<String> result = objectMapper.readValue(json, List.class);
        assertEquals(List.of("Tomato", "Carrot", "Onion"), result);
    }

    @Test
    void testDeserializeSemicolonSeparatedString() throws JsonProcessingException {
        String json = "\"Tomato; Carrot; Onion\"";
        List<String> result = objectMapper.readValue(json, List.class);
        assertEquals(List.of("Tomato", "Carrot", "Onion"), result);
    }

    @Test
    void testDeserializeEmptyString() throws JsonProcessingException {
        String json = "\"\"";
        List<String> result = objectMapper.readValue(json, List.class);
        assertEquals(List.of(), result);
    }

    @Test
    void testDeserializeNull() throws JsonProcessingException {
        String json = "null";
        List<String> result = objectMapper.readValue(json, List.class);
        assertEquals(List.of(), result);
    }

    @Test
    void testDeserializeUnexpectedToken() throws JsonProcessingException {
        String json = "123";
        List<String> result = objectMapper.readValue(json, List.class);
        assertEquals(List.of("123"), result);
    }
}
