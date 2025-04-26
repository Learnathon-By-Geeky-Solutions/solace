package dev.solace.twiggle.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "pests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "common_name", nullable = false)
    private String commonName;

    @Column(name = "scientific_name")
    private String scientificName;

    @Column(name = "description")
    private String description;

    @Column(name = "damage_symptoms")
    private String damageSymptoms;

    @Column(name = "life_cycle")
    private String lifeCycle;

    @Column(name = "season_active")
    private String seasonActive;

    @Column(name = "organic_control")
    private String organicControl;

    @Column(name = "chemical_control")
    private String chemicalControl;

    @Column(name = "prevention_tips")
    private String preventionTips;

    @Column(name = "image_url")
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
