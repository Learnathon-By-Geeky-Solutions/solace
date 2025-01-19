package dev.solace.twiggle.config;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
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
    private static final String TEST_COLLECTION = "test_collection";

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0.5"));

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

    @AfterEach
    void tearDown() {
        // Clean up all test collections
        database.listCollectionNames().forEach(name -> {
            if (name.startsWith("test_")) {
                database.getCollection(name).drop();
            }
        });
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
        Document testDoc = new Document("test_key", "test_value");

        // Insert
        database.getCollection(TEST_COLLECTION).insertOne(testDoc);

        // Verify insertion
        Document foundDoc = database.getCollection(TEST_COLLECTION)
                .find(new Document("test_key", "test_value"))
                .first();
        assertNotNull(foundDoc);
        assertEquals("test_value", foundDoc.getString("test_key"));
    }

    @Test
    @DisplayName("Test MongoDB Insert With Duplicate ID")
    void testMongoDBInsertDuplicate() {
        Document doc = new Document("_id", "test_id").append("value", "test");

        // First insertion should succeed
        database.getCollection(TEST_COLLECTION).insertOne(doc);

        // Second insertion with same _id should throw MongoWriteException
        assertThrows(MongoWriteException.class, () -> {
            database.getCollection(TEST_COLLECTION).insertOne(doc);
        });
    }

    @Test
    @DisplayName("Test MongoDB Update Operation")
    void testMongoDBUpdate() {
        Document testDoc = new Document("test_key", "initial_value");

        // Insert
        database.getCollection(TEST_COLLECTION).insertOne(testDoc);

        // Update
        database.getCollection(TEST_COLLECTION)
                .updateOne(
                        new Document("test_key", "initial_value"),
                        new Document("$set", new Document("test_key", "updated_value")));

        // Verify update
        Document updatedDoc = database.getCollection(TEST_COLLECTION)
                .find(new Document("test_key", "updated_value"))
                .first();
        assertNotNull(updatedDoc);
        assertEquals("updated_value", updatedDoc.getString("test_key"));
    }

    @Test
    @DisplayName("Test MongoDB Update Non-Existent Document")
    void testMongoDBUpdateNonExistent() {
        // Attempt to update a non-existent document
        Document result = database.getCollection(TEST_COLLECTION)
                .findOneAndUpdate(
                        new Document("non_existent", "value"), new Document("$set", new Document("field", "value")));

        // Verify that no document was found and updated
        assertNull(result);
    }

    @Test
    @DisplayName("Test MongoDB Delete Operation")
    void testMongoDBDelete() {
        Document testDoc = new Document("test_key", "test_value");

        // Insert
        database.getCollection(TEST_COLLECTION).insertOne(testDoc);

        // Delete
        database.getCollection(TEST_COLLECTION).deleteOne(new Document("test_key", "test_value"));

        // Verify deletion
        Document deletedDoc = database.getCollection(TEST_COLLECTION)
                .find(new Document("test_key", "test_value"))
                .first();
        assertNull(deletedDoc);
    }

    @Test
    @DisplayName("Test MongoDB Delete Non-Existent Document")
    void testMongoDBDeleteNonExistent() {
        // Attempt to delete a non-existent document
        long deletedCount = database.getCollection(TEST_COLLECTION)
                .deleteOne(new Document("non_existent", "value"))
                .getDeletedCount();

        // Verify that no documents were deleted
        assertEquals(0, deletedCount);
    }
}
