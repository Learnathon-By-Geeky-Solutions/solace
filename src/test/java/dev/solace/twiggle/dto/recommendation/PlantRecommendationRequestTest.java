package dev.solace.twiggle.dto.recommendation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class PlantRecommendationRequestTest {

    @Test
    void applyDefaultsIfNeeded_shouldSetDefaultsForNullFields() {
        PlantRecommendationRequest req = new PlantRecommendationRequest();
        req.applyDefaultsIfNeeded();
        assertEquals("Any", req.getGardenType());
        assertEquals("Any", req.getLocation());
        assertEquals("Recommend plants", req.getMessage());
        assertNotNull(req.getExistingPlants());
        assertTrue(req.getExistingPlants().isEmpty());
        assertNotNull(req.getUserPreferences());
        assertEquals("beginner", req.getUserPreferences().getExperience());
        assertEquals("moderate", req.getUserPreferences().getTimeCommitment());
        assertNotNull(req.getUserPreferences().getHarvestGoals());
        assertTrue(req.getUserPreferences().getHarvestGoals().isEmpty());
    }

    @Test
    void applyDefaultsIfNeeded_shouldNotOverrideNonBlankFields() {
        PlantRecommendationRequest.UserPreferences prefs = PlantRecommendationRequest.UserPreferences.builder()
                .experience("advanced")
                .harvestGoals(List.of("Fun"))
                .timeCommitment("high")
                .build();
        PlantRecommendationRequest req = PlantRecommendationRequest.builder()
                .gardenType("Outdoor")
                .location("Park")
                .message("Custom message")
                .existingPlants(List.of("Rose"))
                .userPreferences(prefs)
                .build();
        req.applyDefaultsIfNeeded();
        assertEquals("Outdoor", req.getGardenType());
        assertEquals("Park", req.getLocation());
        assertEquals("Custom message", req.getMessage());
        assertEquals(List.of("Rose"), req.getExistingPlants());
        assertEquals("advanced", req.getUserPreferences().getExperience());
        assertEquals("high", req.getUserPreferences().getTimeCommitment());
        assertEquals(List.of("Fun"), req.getUserPreferences().getHarvestGoals());
    }

    @Test
    void applyDefaultsIfNeeded_shouldDefaultBlankFields() {
        PlantRecommendationRequest.UserPreferences prefs = PlantRecommendationRequest.UserPreferences.builder()
                .experience("")
                .harvestGoals(null)
                .timeCommitment(null)
                .build();
        PlantRecommendationRequest req = PlantRecommendationRequest.builder()
                .gardenType("")
                .location(null)
                .message("   ")
                .existingPlants(null)
                .userPreferences(prefs)
                .build();
        req.applyDefaultsIfNeeded();
        assertEquals("Any", req.getGardenType());
        assertEquals("Any", req.getLocation());
        assertEquals("Recommend plants", req.getMessage());
        assertNotNull(req.getExistingPlants());
        assertTrue(req.getExistingPlants().isEmpty());
        assertEquals("beginner", req.getUserPreferences().getExperience());
        assertEquals("moderate", req.getUserPreferences().getTimeCommitment());
        assertNotNull(req.getUserPreferences().getHarvestGoals());
        assertTrue(req.getUserPreferences().getHarvestGoals().isEmpty());
    }

    @Test
    void defaultIfBlank_shouldReturnDefaultForNullOrBlank() throws Exception {
        PlantRecommendationRequest req = new PlantRecommendationRequest();
        var method = PlantRecommendationRequest.class.getDeclaredMethod("defaultIfBlank", String.class, String.class);
        method.setAccessible(true);
        assertEquals("default", method.invoke(req, null, "default"));
        assertEquals("default", method.invoke(req, "", "default"));
        assertEquals("default", method.invoke(req, "   ", "default"));
        assertEquals("value", method.invoke(req, "value", "default"));
    }

    @Test
    void createDefaultPreferences_shouldReturnDefaults() throws Exception {
        PlantRecommendationRequest req = new PlantRecommendationRequest();
        var method = PlantRecommendationRequest.class.getDeclaredMethod("createDefaultPreferences");
        method.setAccessible(true);
        PlantRecommendationRequest.UserPreferences prefs =
                (PlantRecommendationRequest.UserPreferences) method.invoke(req);
        assertEquals("beginner", prefs.getExperience());
        assertEquals("moderate", prefs.getTimeCommitment());
        assertNotNull(prefs.getHarvestGoals());
        assertTrue(prefs.getHarvestGoals().isEmpty());
    }

    @Test
    void applyDefaultsToUserPreferences_shouldSetDefaults() throws Exception {
        PlantRecommendationRequest req = new PlantRecommendationRequest();
        PlantRecommendationRequest.UserPreferences prefs = PlantRecommendationRequest.UserPreferences.builder()
                .experience(null)
                .harvestGoals(null)
                .timeCommitment("")
                .build();
        var method = PlantRecommendationRequest.class.getDeclaredMethod(
                "applyDefaultsToUserPreferences", PlantRecommendationRequest.UserPreferences.class);
        method.setAccessible(true);
        method.invoke(req, prefs);
        assertEquals("beginner", prefs.getExperience());
        assertEquals("moderate", prefs.getTimeCommitment());
        assertNotNull(prefs.getHarvestGoals());
        assertTrue(prefs.getHarvestGoals().isEmpty());
    }
}
