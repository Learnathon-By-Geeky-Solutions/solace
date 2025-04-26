package dev.solace.twiggle.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantDiseaseDTO {
    private Long id;
    private String commonName;
    private String scientificName;
    private String description;
    private String symptoms;
    private String favorableConditions;
    private String preventionTips;
    private String organicControl;
    private String chemicalControl;
    private String imageUrl;
    private String transmissionMethod;
    private String contagiousness;
    private String severityRating;
    private String timeToOnset;
    private String recoveryChances;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
