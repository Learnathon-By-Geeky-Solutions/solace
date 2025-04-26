package dev.solace.twiggle.mapper;

import dev.solace.twiggle.model.Plant;
import dev.solace.twiggle.model.PlantsLibrary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between PlantsLibrary and Plant entities.
 */
@Mapper(componentModel = "spring")
public interface PlantAddMapper {

    /**
     * Maps a PlantsLibrary entity to a Plant entity.
     *
     * @param plantsLibrary the plant library entity
     * @return the plant entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "gardenPlanId", ignore = true)
    @Mapping(target = "name", source = "commonName")
    @Mapping(target = "type", source = "plantType")
    @Mapping(target = "description", source = "shortDescription")
    @Mapping(target = "wateringFrequency", source = "wateringFrequency")
    @Mapping(target = "sunlightRequirements", source = "sunlightRequirement")
    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "positionX", ignore = true)
    @Mapping(target = "positionY", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Plant toPlantEntity(PlantsLibrary plantsLibrary);
}
