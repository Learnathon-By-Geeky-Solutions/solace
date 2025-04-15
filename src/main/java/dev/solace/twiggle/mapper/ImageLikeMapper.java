package dev.solace.twiggle.mapper;

import dev.solace.twiggle.dto.ImageLikeDTO;
import dev.solace.twiggle.model.ImageLike;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ImageLikeMapper {

    /**
     * Converts an ImageLike entity to an ImageLikeDTO.
     * ID is not mapped as it's auto-generated.
     *
     * @param imageLike the entity to convert
     * @return the corresponding DTO
     */
    ImageLikeDTO toDto(ImageLike imageLike);

    /**
     * Converts an ImageLikeDTO to an ImageLike entity.
     * ID will be auto-generated and not set from the DTO.
     *
     * @param imageLikeDTO the DTO to convert
     * @return the corresponding entity
     */
    @Mapping(target = "id", ignore = true)
    ImageLike toEntity(ImageLikeDTO imageLikeDTO);
}
