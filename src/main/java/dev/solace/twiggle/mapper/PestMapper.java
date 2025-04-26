package dev.solace.twiggle.mapper;

import dev.solace.twiggle.dto.PestDTO;
import dev.solace.twiggle.model.Pest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PestMapper {
    PestDTO toDto(Pest pest);

    Pest toEntity(PestDTO pestDTO);
}
