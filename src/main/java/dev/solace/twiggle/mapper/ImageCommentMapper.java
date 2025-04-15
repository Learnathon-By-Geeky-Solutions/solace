package dev.solace.twiggle.mapper;

import dev.solace.twiggle.dto.ImageCommentDTO;
import dev.solace.twiggle.model.ImageComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ImageCommentMapper {

    /**
     * Converts an ImageComment entity to an ImageCommentDTO.
     * ID is not mapped as it's auto-generated.
     *
     * @param imageComment the entity to convert
     * @return the corresponding DTO
     */
    ImageCommentDTO toDto(ImageComment imageComment);

    /**
     * Converts an ImageCommentDTO to an ImageComment entity.
     * ID will be auto-generated and not set from the DTO.
     *
     * @param imageCommentDTO the DTO to convert
     * @return the corresponding entity
     */
    @Mapping(target = "id", ignore = true)
    ImageComment toEntity(ImageCommentDTO imageCommentDTO);
}
