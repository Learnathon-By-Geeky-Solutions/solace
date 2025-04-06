package dev.solace.twiggle.model.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Plant entity that maps to the existing 'plants' table in Supabase.
 */
@Entity
@Table(name = "plants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plant {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "garden_plan_id", nullable = false, columnDefinition = "uuid")
    private UUID gardenPlanId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "watering_frequency")
    private String wateringFrequency;

    @Column(name = "sunlight_requirements")
    private String sunlightRequirements;

    @Column(name = "position_x")
    private Integer positionX;

    @Column(name = "position_y")
    private Integer positionY;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
