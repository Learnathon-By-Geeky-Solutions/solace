package dev.solace.twiggle.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

/**
 * GardenImage entity that maps to the existing 'garden_images' table in Supabase.
 */
@Entity
@Table(name = "garden_images")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GardenImage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "garden_plan_id", columnDefinition = "uuid")
    private UUID gardenPlanId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "title")
    private String title;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
