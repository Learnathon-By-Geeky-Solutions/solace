package dev.solace.twiggle.mapper;

import dev.solace.twiggle.dto.PlantsLibraryDTO;
import dev.solace.twiggle.model.PlantsLibrary;
import io.hypersistence.utils.hibernate.type.range.Range;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlantsLibraryMapper {

    /**
     * Converts a PlantsLibrary entity to a PlantsLibraryDTO.
     * Special handling for temperature range.
     *
     * @param plantsLibrary the entity to convert
     * @return the corresponding DTO
     */
    @Mapping(target = "minTemperature", source = "temperatureRange", qualifiedByName = "getMinTemperature")
    @Mapping(target = "maxTemperature", source = "temperatureRange", qualifiedByName = "getMaxTemperature")
    PlantsLibraryDTO toDto(PlantsLibrary plantsLibrary);

    /**
     * Converts a PlantsLibraryDTO to a PlantsLibrary entity.
     * Special handling for temperature range.
     *
     * @param plantsLibraryDTO the DTO to convert
     * @return the corresponding entity
     */
    @Mapping(target = "temperatureRange", source = ".", qualifiedByName = "createTemperatureRange")
    PlantsLibrary toEntity(PlantsLibraryDTO plantsLibraryDTO);

    /**
     * Extracts the lower bound value from a temperature range.
     */
    @Named("getMinTemperature")
    default Double getMinTemperature(Range<BigDecimal> temperatureRange) {
        if (temperatureRange != null && temperatureRange.hasLowerBound()) {
            BigDecimal lower = temperatureRange.lower();
            return lower.doubleValue();
        }
        return null;
    }

    /**
     * Extracts the upper bound value from a temperature range.
     */
    @Named("getMaxTemperature")
    default Double getMaxTemperature(Range<BigDecimal> temperatureRange) {
        if (temperatureRange != null && temperatureRange.hasUpperBound()) {
            BigDecimal upper = temperatureRange.upper();
            return upper.doubleValue();
        }
        return null;
    }

    /**
     * Creates a temperature range from min and max values.
     */
    @Named("createTemperatureRange")
    default Range<BigDecimal> createTemperatureRange(PlantsLibraryDTO dto) {
        if (dto.getMinTemperature() != null && dto.getMaxTemperature() != null) {
            return Range.closed(
                    BigDecimal.valueOf(dto.getMinTemperature()), BigDecimal.valueOf(dto.getMaxTemperature()));
        } else if (dto.getMinTemperature() != null) {
            return Range.closedInfinite(BigDecimal.valueOf(dto.getMinTemperature()));
        } else if (dto.getMaxTemperature() != null) {
            return Range.infiniteClosed(BigDecimal.valueOf(dto.getMaxTemperature()));
        }
        return null;
    }
}
