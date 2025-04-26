package dev.solace.twiggle.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "plant_diseases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantDisease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "common_name", nullable = false)
    private String commonName;

    @Column(name = "scientific_name")
    private String scientificName;

    @Column(name = "description")
    private String description;

    @Column(name = "symptoms")
    private String symptoms;

    @Column(name = "favorable_conditions")
    private String favorableConditions;

    @Column(name = "prevention_tips")
    private String preventionTips;

    @Column(name = "organic_control")
    private String organicControl;

    @Column(name = "chemical_control")
    private String chemicalControl;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "transmission_method")
    private String transmissionMethod;

    @Column(name = "contagiousness")
    private String contagiousness;

    @Column(name = "severity_rating")
    private String severityRating;

    @Column(name = "time_to_onset")
    private String timeToOnset;

    @Column(name = "recovery_chances")
    private String recoveryChances;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
