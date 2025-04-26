package dev.solace.twiggle.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlantTest {

    private Plant plant;
    private final UUID testId = UUID.randomUUID();
    private final UUID testGardenPlanId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        plant = new Plant();
        plant.setId(testId);
        plant.setGardenPlanId(testGardenPlanId);
        plant.setName("Test Plant");
        plant.setType("Vegetable");
        plant.setDescription("A test plant description");
        plant.setWateringFrequency("Daily");
        plant.setSunlightRequirements("Full Sun");
        plant.setPositionX(10);
        plant.setPositionY(20);
        plant.setImageUrl("http://example.com/image.jpg");
    }

    @Test
    void testBasicGettersAndSetters() {
        assertEquals(testId, plant.getId());
        assertEquals(testGardenPlanId, plant.getGardenPlanId());
        assertEquals("Test Plant", plant.getName());
        assertEquals("Vegetable", plant.getType());
        assertEquals("A test plant description", plant.getDescription());
        assertEquals("Daily", plant.getWateringFrequency());
        assertEquals("Full Sun", plant.getSunlightRequirements());
        assertEquals(10, plant.getPositionX());
        assertEquals(20, plant.getPositionY());
        assertEquals("http://example.com/image.jpg", plant.getImageUrl());
    }

    @Test
    void testNoArgsConstructor() {
        Plant emptyPlant = new Plant();
        assertNotNull(emptyPlant);
        assertNull(emptyPlant.getId());
        assertNull(emptyPlant.getName());
        assertNull(emptyPlant.getType());
    }

    @Test
    void testAllArgsConstructor() {
        Plant constructedPlant = new Plant(
                testId,
                testGardenPlanId,
                "Test Plant",
                "Vegetable",
                "Description",
                "Weekly",
                "Partial Shade",
                15,
                25,
                "http://example.com/image2.jpg",
                OffsetDateTime.now(),
                OffsetDateTime.now());

        assertNotNull(constructedPlant);
        assertEquals(testId, constructedPlant.getId());
        assertEquals(testGardenPlanId, constructedPlant.getGardenPlanId());
        assertEquals("Test Plant", constructedPlant.getName());
        assertEquals("Vegetable", constructedPlant.getType());
        assertEquals("Description", constructedPlant.getDescription());
        assertEquals("Weekly", constructedPlant.getWateringFrequency());
        assertEquals("Partial Shade", constructedPlant.getSunlightRequirements());
        assertEquals(15, constructedPlant.getPositionX());
        assertEquals(25, constructedPlant.getPositionY());
        assertEquals("http://example.com/image2.jpg", constructedPlant.getImageUrl());
    }

    @Test
    void testOnCreate() {
        plant.onCreate();
        assertNotNull(plant.getCreatedAt());
        assertNotNull(plant.getUpdatedAt());
        assertEquals(plant.getCreatedAt(), plant.getUpdatedAt());
    }

    @Test
    void testOnUpdate() {
        // Set initial timestamps
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        plant.setCreatedAt(now);
        plant.setUpdatedAt(now);

        // Trigger update with a known later time
        OffsetDateTime laterTime = now.plusSeconds(1);
        plant.setUpdatedAt(laterTime);
        plant.onUpdate();

        // Verify timestamps
        assertEquals(now, plant.getCreatedAt(), "Created timestamp should not change");
        assertTrue(plant.getUpdatedAt().isAfter(now), "Updated timestamp should be after initial time");
    }

    @Test
    void testEqualsAndHashCode() {
        Plant plant1 = new Plant();
        plant1.setId(testId);

        Plant plant2 = new Plant();
        plant2.setId(testId);

        Plant plant3 = new Plant();
        plant3.setId(UUID.randomUUID());

        assertEquals(plant1, plant2, "Plants with same ID should be equal");
        assertNotEquals(plant1, plant3, "Plants with different IDs should not be equal");
        assertEquals(plant1.hashCode(), plant2.hashCode(), "Hash codes should be equal for equal plants");
    }
}
