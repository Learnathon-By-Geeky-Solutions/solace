package dev.solace.twiggle.model;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

class AuthUserTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testRawAppMetaDataJsonMethods() {
        // Arrange
        AuthUser user = new AuthUser();
        ObjectNode jsonObject = mapper.createObjectNode();
        jsonObject.put("key", "value");
        jsonObject.put("number", 123);

        // Act - Test serialization
        user.setRawAppMetaDataFromJson(jsonObject);

        // Assert - Verify the string form contains our data
        String rawData = user.getRawAppMetaData();
        assertTrue(rawData.contains("\"key\":\"value\""));
        assertTrue(rawData.contains("\"number\":123"));

        // Act - Test deserialization
        JsonNode retrieved = user.getRawAppMetaDataAsJson();

        // Assert - Verify we can get back what we put in
        assertNotNull(retrieved);
        assertEquals("value", retrieved.get("key").asText());
        assertEquals(123, retrieved.get("number").asInt());
    }

    @Test
    void testRawUserMetaDataJsonMethods() {
        // Arrange
        AuthUser user = new AuthUser();
        ObjectNode jsonObject = mapper.createObjectNode();
        jsonObject.put("name", "John Doe");
        jsonObject.put("age", 30);

        // Act - Test serialization
        user.setRawUserMetaDataFromJson(jsonObject);

        // Assert - Verify the string form contains our data
        String rawData = user.getRawUserMetaData();
        assertTrue(rawData.contains("\"name\":\"John Doe\""));
        assertTrue(rawData.contains("\"age\":30"));

        // Act - Test deserialization
        JsonNode retrieved = user.getRawUserMetaDataAsJson();

        // Assert - Verify we can get back what we put in
        assertNotNull(retrieved);
        assertEquals("John Doe", retrieved.get("name").asText());
        assertEquals(30, retrieved.get("age").asInt());
    }

    @Test
    void testRawAppMetaDataWithInvalidJson() {
        // Arrange
        AuthUser user = new AuthUser();
        user.setRawAppMetaData("this is not valid json");

        // Act & Assert
        assertNull(user.getRawAppMetaDataAsJson());
    }

    @Test
    void testRawUserMetaDataWithInvalidJson() {
        // Arrange
        AuthUser user = new AuthUser();
        user.setRawUserMetaData("this is not valid json");

        // Act & Assert
        assertNull(user.getRawUserMetaDataAsJson());
    }

    @Test
    void testRawAppMetaDataWithNullInput() {
        // Arrange
        AuthUser user = new AuthUser();

        // Act - Test serialization with null
        user.setRawAppMetaDataFromJson(null);

        // Assert - When JSON node is null, ObjectMapper serializes it as "null" string
        assertEquals("null", user.getRawAppMetaData());

        // Act - Test deserialization with null
        user.setRawAppMetaData(null);

        // Assert
        assertNull(user.getRawAppMetaDataAsJson());
    }

    @Test
    void testRawUserMetaDataWithNullInput() {
        // Arrange
        AuthUser user = new AuthUser();

        // Act - Test serialization with null
        user.setRawUserMetaDataFromJson(null);

        // Assert - When JSON node is null, ObjectMapper serializes it as "null" string
        assertEquals("null", user.getRawUserMetaData());

        // Act - Test deserialization with null
        user.setRawUserMetaData(null);

        // Assert
        assertNull(user.getRawUserMetaDataAsJson());
    }
}
