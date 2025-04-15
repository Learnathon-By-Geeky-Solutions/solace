package dev.solace.twiggle.mapper;

import dev.solace.twiggle.dto.ActivityDTO;
import dev.solace.twiggle.model.Activity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ActivityMapper {

    /**
     * Converts an Activity entity to an ActivityDTO.
     * ID is not mapped as it's auto-generated.
     *
     * @param activity the entity to convert
     * @return the corresponding DTO
     */
    ActivityDTO toDto(Activity activity);

    /**
     * Converts an ActivityDTO to an Activity entity.
     * ID will be auto-generated and not set from the DTO.
     *
     * @param activityDTO the DTO to convert
     * @return the corresponding entity
     */
    @Mapping(target = "id", ignore = true)
    Activity toEntity(ActivityDTO activityDTO);
}
