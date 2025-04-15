package dev.solace.twiggle.mapper;

import dev.solace.twiggle.dto.PlantReminderDTO;
import dev.solace.twiggle.model.PlantReminder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlantReminderMapper {

    /**
     * Converts a PlantReminder entity to a PlantReminderDTO.
     * ID is not mapped as it's auto-generated.
     *
     * @param plantReminder the entity to convert
     * @return the corresponding DTO
     */
    PlantReminderDTO toDto(PlantReminder plantReminder);

    /**
     * Converts a PlantReminderDTO to a PlantReminder entity.
     * ID will be auto-generated and not set from the DTO.
     *
     * @param plantReminderDTO the DTO to convert
     * @return the corresponding entity
     */
    @Mapping(target = "id", ignore = true)
    PlantReminder toEntity(PlantReminderDTO plantReminderDTO);
}
