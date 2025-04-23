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
public class PestDTO {
    private Long id;
    private String commonName;
    private String scientificName;
    private String description;
    private String damageSymptoms;
    private String lifeCycle;
    private String seasonActive;
    private String organicControl;
    private String chemicalControl;
    private String preventionTips;
    private String imageUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
