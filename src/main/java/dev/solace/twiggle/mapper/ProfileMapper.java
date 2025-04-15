package dev.solace.twiggle.mapper;

import dev.solace.twiggle.dto.ProfileDTO;
import dev.solace.twiggle.model.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileMapper {

    /**
     * Converts a Profile entity to a ProfileDTO.
     * ID is not mapped as it's auto-generated.
     *
     * @param profile the entity to convert
     * @return the corresponding DTO
     */
    ProfileDTO toDto(Profile profile);

    /**
     * Converts a ProfileDTO to a Profile entity.
     * ID will be auto-generated and not set from the DTO.
     *
     * @param profileDTO the DTO to convert
     * @return the corresponding entity
     */
    @Mapping(target = "id", ignore = true)
    Profile toEntity(ProfileDTO profileDTO);
}
