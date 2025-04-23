package dev.solace.twiggle.model;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import io.hypersistence.utils.hibernate.type.range.PostgreSQLRangeType;
import io.hypersistence.utils.hibernate.type.range.Range;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "plants_library")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantsLibrary {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    // Text fields
    @Column(name = "common_name")
    private String commonName;

    @Column(name = "other_name")
    private String otherName;

    @Column(name = "scientific_name")
    private String scientificName;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "origin")
    private String origin;

    @Column(name = "plant_type")
    private String plantType;

    @Column(name = "climate")
    private String climate;

    @Column(name = "life_cycle")
    private String lifeCycle;

    @Column(name = "watering_frequency")
    private String wateringFrequency;

    @Column(name = "soil_type")
    private String soilType;

    @Column(name = "size")
    private String size;

    @Column(name = "sunlight_requirement")
    private String sunlightRequirement;

    @Column(name = "growth_rate")
    private String growthRate;

    @Column(name = "ideal_place")
    private String idealPlace;

    @Column(name = "care_level")
    private String careLevel;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "best_planting_season")
    private String bestPlantingSeason;

    @Column(name = "gardening_tips")
    private String gardeningTips;

    @Column(name = "pruning_guide")
    private String pruningGuide;

    // Numeric fields
    @Column(name = "seed_depth")
    private Double seedDepth;

    @Column(name = "germination_time")
    private Double germinationTime;

    @Column(name = "time_to_harvest")
    private Double timeToHarvest;

    // Boolean fields
    @Column(name = "flower")
    private Boolean flower;

    @Column(name = "fruit")
    private Boolean fruit;

    @Column(name = "medicinal")
    private Boolean medicinal;

    // PostgreSQL numrange
    @Type(PostgreSQLRangeType.class)
    @Column(name = "temperature_range", columnDefinition = "numrange")
    private Range<BigDecimal> temperatureRange;

    // PostgreSQL text[] arrays
    @Type(ListArrayType.class)
    @Column(name = "common_pests", columnDefinition = "text[]")
    private List<String> commonPests;

    @Type(ListArrayType.class)
    @Column(name = "common_diseases", columnDefinition = "text[]")
    private List<String> commonDiseases;

    @Type(ListArrayType.class)
    @Column(name = "companion_plants", columnDefinition = "text[]")
    private List<String> companionPlants;

    @Type(ListArrayType.class)
    @Column(name = "avoid_planting_with", columnDefinition = "text[]")
    private List<String> avoidPlantingWith;

    @Type(ListArrayType.class)
    @Column(name = "pest_disease_prevention_tips", columnDefinition = "text[]")
    private List<String> pestDiseasePreventionTips;

    @Type(ListArrayType.class)
    @Column(name = "cool_facts", columnDefinition = "text[]")
    private List<String> coolFacts;

    @Type(ListArrayType.class)
    @Column(name = "edible_parts", columnDefinition = "text[]")
    private List<String> edibleParts;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
