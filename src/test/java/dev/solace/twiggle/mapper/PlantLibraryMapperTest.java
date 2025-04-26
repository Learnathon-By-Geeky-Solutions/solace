package dev.solace.twiggle.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import dev.solace.twiggle.dto.PlantsLibraryDTO;
import dev.solace.twiggle.model.PlantsLibrary;
import io.hypersistence.utils.hibernate.type.range.Range;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PlantLibraryMapperTest {

    private PlantsLibraryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(PlantsLibraryMapper.class);
    }

    @Test
    void toDto_shouldMapEntityToDto() {
        // Given
        PlantsLibrary entity = new PlantsLibrary();
        entity.setTemperatureRange(Range.closed(BigDecimal.valueOf(10.0), BigDecimal.valueOf(30.0)));

        // When
        PlantsLibraryDTO dto = mapper.toDto(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getMinTemperature()).isEqualTo(10.0);
        assertThat(dto.getMaxTemperature()).isEqualTo(30.0);
    }

    @Test
    void toDto_shouldHandleNullTemperatureRange() {
        // Given
        PlantsLibrary entity = new PlantsLibrary();
        entity.setTemperatureRange(null);

        // When
        PlantsLibraryDTO dto = mapper.toDto(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getMinTemperature()).isNull();
        assertThat(dto.getMaxTemperature()).isNull();
    }

    @Test
    void toDto_shouldHandleInfiniteRanges() {
        // Given
        PlantsLibrary entity = new PlantsLibrary();
        entity.setTemperatureRange(Range.closedInfinite(BigDecimal.valueOf(10.0)));

        // When
        PlantsLibraryDTO dto = mapper.toDto(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getMinTemperature()).isEqualTo(10.0);
        assertThat(dto.getMaxTemperature()).isNull();
    }

    @Test
    void toEntity_shouldMapDtoToEntity() {
        // Given
        PlantsLibraryDTO dto = new PlantsLibraryDTO();
        dto.setMinTemperature(10.0);
        dto.setMaxTemperature(30.0);

        // When
        PlantsLibrary entity = mapper.toEntity(dto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getTemperatureRange()).isNotNull();
        assertThat(entity.getTemperatureRange().lower()).isEqualTo(BigDecimal.valueOf(10.0));
        assertThat(entity.getTemperatureRange().upper()).isEqualTo(BigDecimal.valueOf(30.0));
    }

    @Test
    void toEntity_shouldHandleNullTemperatures() {
        // Given
        PlantsLibraryDTO dto = new PlantsLibraryDTO();
        dto.setMinTemperature(null);
        dto.setMaxTemperature(null);

        // When
        PlantsLibrary entity = mapper.toEntity(dto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getTemperatureRange()).isNull();
    }

    @Test
    void toEntity_shouldHandleOnlyMinTemperature() {
        // Given
        PlantsLibraryDTO dto = new PlantsLibraryDTO();
        dto.setMinTemperature(10.0);
        dto.setMaxTemperature(null);

        // When
        PlantsLibrary entity = mapper.toEntity(dto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getTemperatureRange()).isNotNull();
        assertThat(entity.getTemperatureRange().lower()).isEqualTo(BigDecimal.valueOf(10.0));
        assertThat(entity.getTemperatureRange().hasUpperBound()).isFalse();
    }

    @Test
    void toEntity_shouldHandleOnlyMaxTemperature() {
        // Given
        PlantsLibraryDTO dto = new PlantsLibraryDTO();
        dto.setMinTemperature(null);
        dto.setMaxTemperature(30.0);

        // When
        PlantsLibrary entity = mapper.toEntity(dto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getTemperatureRange()).isNotNull();
        assertThat(entity.getTemperatureRange().hasLowerBound()).isFalse();
        assertThat(entity.getTemperatureRange().upper()).isEqualTo(BigDecimal.valueOf(30.0));
    }
}
