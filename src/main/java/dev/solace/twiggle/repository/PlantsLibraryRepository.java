package dev.solace.twiggle.repository;

import dev.solace.twiggle.model.PlantsLibrary;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing PlantsLibrary entities.
 */
@Repository
public interface PlantsLibraryRepository
        extends JpaRepository<PlantsLibrary, UUID>, JpaSpecificationExecutor<PlantsLibrary> {

    /**
     * Find plants by plant type with pagination.
     *
     * @param plantType The plant type to search for (case-insensitive, partial match)
     * @param pageable Pagination and sorting information
     * @return Page of plants matching the plant type
     */
    Page<PlantsLibrary> findByPlantTypeContainingIgnoreCase(String plantType, Pageable pageable);

    /**
     * Find plants by life cycle with pagination.
     *
     * @param lifeCycle The life cycle to search for (case-insensitive, partial match)
     * @param pageable Pagination and sorting information
     * @return Page of plants matching the life cycle
     */
    Page<PlantsLibrary> findByLifeCycleContainingIgnoreCase(String lifeCycle, Pageable pageable);

    /**
     * Find plants by medicinal property with pagination.
     *
     * @param medicinal The medicinal property to filter by
     * @param pageable Pagination and sorting information
     * @return Page of plants matching the medicinal property
     */
    Page<PlantsLibrary> findByMedicinal(Boolean medicinal, Pageable pageable);

    /**
     * Find plants by common name with pagination.
     *
     * @param commonName The common name to search for (case-insensitive, partial match)
     * @param pageable Pagination and sorting information
     * @return Page of plants matching the common name
     */
    Page<PlantsLibrary> findByCommonNameContainingIgnoreCase(String commonName, Pageable pageable);

    /**
     * Find plants by scientific name with pagination.
     *
     * @param scientificName The scientific name to search for (case-insensitive, partial match)
     * @param pageable Pagination and sorting information
     * @return Page of plants matching the scientific name
     */
    Page<PlantsLibrary> findByScientificNameContainingIgnoreCase(String scientificName, Pageable pageable);

    /**
     * Find plants by origin with pagination.
     *
     * @param origin The origin to search for (case-insensitive, partial match)
     * @param pageable Pagination and sorting information
     * @return Page of plants matching the origin
     */
    Page<PlantsLibrary> findByOriginContainingIgnoreCase(String origin, Pageable pageable);

    /**
     * Find plants by climate with pagination.
     *
     * @param climate The climate to search for (case-insensitive, partial match)
     * @param pageable Pagination and sorting information
     * @return Page of plants matching the climate
     */
    Page<PlantsLibrary> findByClimateContainingIgnoreCase(String climate, Pageable pageable);

    /**
     * Find plants by watering frequency with pagination.
     *
     * @param wateringFrequency The watering frequency to search for (case-insensitive, partial match)
     * @param pageable Pagination and sorting information
     * @return Page of plants matching the watering frequency
     */
    Page<PlantsLibrary> findByWateringFrequencyContainingIgnoreCase(String wateringFrequency, Pageable pageable);

    /**
     * Find plants by soil type with pagination.
     *
     * @param soilType The soil type to search for (case-insensitive, partial match)
     * @param pageable Pagination and sorting information
     * @return Page of plants matching the soil type
     */
    Page<PlantsLibrary> findBySoilTypeContainingIgnoreCase(String soilType, Pageable pageable);

    /**
     * Find plants by sunlight requirement with pagination.
     *
     * @param sunlightRequirement The sunlight requirement to search for (case-insensitive, partial match)
     * @param pageable Pagination and sorting information
     * @return Page of plants matching the sunlight requirement
     */
    Page<PlantsLibrary> findBySunlightRequirementContainingIgnoreCase(String sunlightRequirement, Pageable pageable);

    /**
     * Find plants by growth rate with pagination.
     *
     * @param growthRate The growth rate to search for (case-insensitive, partial match)
     * @param pageable Pagination and sorting information
     * @return Page of plants matching the growth rate
     */
    Page<PlantsLibrary> findByGrowthRateContainingIgnoreCase(String growthRate, Pageable pageable);

    /**
     * Find plants by care level with pagination.
     *
     * @param careLevel The care level to search for (case-insensitive, partial match)
     * @param pageable Pagination and sorting information
     * @return Page of plants matching the care level
     */
    Page<PlantsLibrary> findByCareLevelContainingIgnoreCase(String careLevel, Pageable pageable);

    /**
     * Find plants by flower property with pagination.
     *
     * @param flower The flower property to filter by
     * @param pageable Pagination and sorting information
     * @return Page of plants matching the flower property
     */
    Page<PlantsLibrary> findByFlower(Boolean flower, Pageable pageable);

    /**
     * Find plants by fruit property with pagination.
     *
     * @param fruit The fruit property to filter by
     * @param pageable Pagination and sorting information
     * @return Page of plants matching the fruit property
     */
    Page<PlantsLibrary> findByFruit(Boolean fruit, Pageable pageable);
}
