package dev.solace.twiggle.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import dev.solace.twiggle.model.Plant;
import dev.solace.twiggle.model.PlantsLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PlantAddMapperTest {

    private PlantAddMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(PlantAddMapper.class);
    }

    @Test
    @DisplayName("toPlantEntity should map PlantsLibrary to Plant with correct field mappings")
    void toPlantEntity_ShouldMapPlantsLibraryToPlant() {
        // Given
        PlantsLibrary plantsLibrary = PlantsLibrary.builder()
                .commonName("Basil")
                .plantType("Herb")
                .shortDescription("A fragrant herb used in cooking")
                .wateringFrequency("Daily")
                .sunlightRequirement("Full Sun")
                .imageUrl("https://example.com/basil.jpg")
                .build();

        // When
        Plant plant = mapper.toPlantEntity(plantsLibrary);

        // Then
        assertThat(plant).isNotNull();
        assertThat(plant.getName()).isEqualTo("Basil");
        assertThat(plant.getType()).isEqualTo("Herb");
        assertThat(plant.getDescription()).isEqualTo("A fragrant herb used in cooking");
        assertThat(plant.getWateringFrequency()).isEqualTo("Daily");
        assertThat(plant.getSunlightRequirements()).isEqualTo("Full Sun");
        assertThat(plant.getImageUrl()).isEqualTo("https://example.com/basil.jpg");
        assertThat(plant.getId()).isNull();
        assertThat(plant.getGardenPlanId()).isNull();
        assertThat(plant.getPositionX()).isNull();
        assertThat(plant.getPositionY()).isNull();
        assertThat(plant.getCreatedAt()).isNull();
        assertThat(plant.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("toPlantEntity should handle null values in PlantsLibrary")
    void toPlantEntity_ShouldHandleNullValues() {
        // Given
        PlantsLibrary plantsLibrary = PlantsLibrary.builder().build();

        // When
        Plant plant = mapper.toPlantEntity(plantsLibrary);

        // Then
        assertThat(plant).isNotNull();
        assertThat(plant.getName()).isNull();
        assertThat(plant.getType()).isNull();
        assertThat(plant.getDescription()).isNull();
        assertThat(plant.getWateringFrequency()).isNull();
        assertThat(plant.getSunlightRequirements()).isNull();
        assertThat(plant.getImageUrl()).isNull();
        assertThat(plant.getId()).isNull();
        assertThat(plant.getGardenPlanId()).isNull();
        assertThat(plant.getPositionX()).isNull();
        assertThat(plant.getPositionY()).isNull();
        assertThat(plant.getCreatedAt()).isNull();
        assertThat(plant.getUpdatedAt()).isNull();
    }
}
