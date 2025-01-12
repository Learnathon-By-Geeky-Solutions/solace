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

@SpringBootTest
@ActiveProfiles("dev")
public class MongoDBConnectionTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoClient mongoClient;

    private MongoDatabase database;

    /**
     * Prepares the test environment by initializing the MongoDB database instance.
     *
     * This method is executed before each test method and retrieves the database
     * associated with the configured MongoTemplate. It ensures that the database
     * is ready for subsequent test operations.
     *
     * @see MongoTemplate
     * @see MongoClient
     */
    @BeforeEach
    void setUp() {
        database = mongoClient.getDatabase(mongoTemplate.getDb().getName());
    }

    /**
     * Verifies the successful establishment of a MongoDB connection.
     *
     * This test method checks that:
     * 1. The MongoTemplate is properly initialized and not null
     * 2. The MongoDB database instance is not null
     * 3. The database name is non-empty
     *
     * @throws AssertionError if any of the connection validation checks fail
     */
    @Test
    @DisplayName("Test MongoDB Connection")
    void testMongoDBConnection() {
        assertNotNull(mongoTemplate);
        assertNotNull(database);
        assertTrue(mongoTemplate.getDb().getName().length() > 0);
    }

    /**
     * Tests the write and read operations for a MongoDB database collection.
     *
     * This test method verifies the ability to insert a document into a collection
     * and subsequently retrieve it using a matching query. It demonstrates basic
     * CRUD (Create and Read) functionality of MongoDB operations.
     *
     * The test performs the following steps:
     * 1. Creates a test collection named "test_collection"
     * 2. Inserts a document with a key-value pair
     * 3. Retrieves the document using the inserted key
     * 4. Asserts that the document exists and has the correct value
     * 5. Drops the test collection to clean up resources
     *
     * @throws AssertionError if the document cannot be inserted or retrieved
     */
    @Test
    @DisplayName("Test MongoDB Write and Read Operations")
    void testMongoDBWriteAndRead() {
        // Create a test collection
        String collectionName = "test_collection";
        Document testDoc = new Document("test_key", "test_value");

        // Write operation
        database.getCollection(collectionName).insertOne(testDoc);

        // Read operation
        Document foundDoc = database.getCollection(collectionName)
                .find(new Document("test_key", "test_value"))
                .first();

        assertNotNull(foundDoc);
        assertEquals("test_value", foundDoc.getString("test_key"));

        // Clean up
        database.getCollection(collectionName).drop();
    }

    /**
     * Tests the update operation in a MongoDB database.
     *
     * This test method verifies the ability to update a document in a MongoDB collection.
     * It performs the following steps:
     * 1. Creates a test collection
     * 2. Inserts an initial document with a specific value
     * 3. Updates the document's value
     * 4. Verifies that the document has been successfully updated
     * 5. Drops the test collection to clean up
     *
     * @throws AssertionError if the update operation fails or the updated document cannot be found
     */
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

        // Clean up
        database.getCollection(collectionName).drop();
    }

    /**
     * Tests the delete operation in a MongoDB database.
     *
     * This test method verifies the ability to delete a document from a MongoDB collection.
     * It performs the following steps:
     * 1. Creates a test collection
     * 2. Inserts a test document
     * 3. Deletes the document using a matching filter
     * 4. Confirms that the document has been successfully deleted
     * 5. Drops the test collection to clean up
     *
     * @throws AssertionError if the document is not successfully deleted or cannot be verified
     */
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

        // Clean up
        database.getCollection(collectionName).drop();
    }
}
