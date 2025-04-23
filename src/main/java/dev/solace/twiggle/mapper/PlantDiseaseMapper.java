package dev.solace.twiggle.mapper;

import dev.solace.twiggle.dto.PlantDiseaseDTO;
import dev.solace.twiggle.model.PlantDisease;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlantDiseaseMapper {
    PlantDiseaseDTO toDto(PlantDisease disease);

    PlantDisease toEntity(PlantDiseaseDTO dto);
}
