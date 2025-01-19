package dev.solace.twiggle.config;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@ActiveProfiles("dev")
class MongoDBConnectionTest {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoClient mongoClient;

    private MongoDatabase database;

    @BeforeEach
    void setUp() {
        database = mongoClient.getDatabase(mongoTemplate.getDb().getName());
    }

    @Test
    @DisplayName("Test MongoDB Connection")
    void testMongoDBConnection() {
        assertNotNull(mongoTemplate);
        assertNotNull(database);
    }

    @Test
    @DisplayName("Test MongoDB Insert Operation")
    void testMongoDBInsert() {
        String collectionName = "test_collection";
        Document testDoc = new Document("test_key", "test_value");

        // Insert
        database.getCollection(collectionName).insertOne(testDoc);

        // Verify insertion
        Document foundDoc = database.getCollection(collectionName)
                .find(new Document("test_key", "test_value"))
                .first();
        assertNotNull(foundDoc);
        assertEquals("test_value", foundDoc.getString("test_key"));

        // Cleanup
        database.getCollection(collectionName).drop();
    }

    @Test
    @DisplayName("Test MongoDB Update Operation")
    void testMongoDBUpdate() {
        String collectionName = "test_collection";
        Document testDoc = new Document("test_key", "initial_value");

        // Insert
        database.getCollection(collectionName).insertOne(testDoc);

        // Update
        database.getCollection(collectionName)
                .updateOne(
                        new Document("test_key", "initial_value"),
                        new Document("$set", new Document("test_key", "updated_value")));

        // Verify update
        Document updatedDoc = database.getCollection(collectionName)
                .find(new Document("test_key", "updated_value"))
                .first();
        assertNotNull(updatedDoc);
        assertEquals("updated_value", updatedDoc.getString("test_key"));

        // Cleanup
        database.getCollection(collectionName).drop();
    }

    @Test
    @DisplayName("Test MongoDB Delete Operation")
    void testMongoDBDelete() {
        String collectionName = "test_collection";
        Document testDoc = new Document("test_key", "test_value");

        // Insert
        database.getCollection(collectionName).insertOne(testDoc);

        // Delete
        database.getCollection(collectionName).deleteOne(new Document("test_key", "test_value"));

        // Verify deletion
        Document deletedDoc = database.getCollection(collectionName)
                .find(new Document("test_key", "test_value"))
                .first();
        assertNull(deletedDoc);

        // Cleanup
        database.getCollection(collectionName).drop();
    }
}
