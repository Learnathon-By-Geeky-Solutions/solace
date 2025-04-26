package dev.solace.twiggle.service;

import static org.springframework.data.jpa.domain.Specification.where;

import dev.solace.twiggle.dto.PlantsLibraryDTO;
import dev.solace.twiggle.dto.PlantsLibrarySearchCriteria;
import dev.solace.twiggle.mapper.PlantsLibraryMapper;
import dev.solace.twiggle.model.PlantsLibrary;
import dev.solace.twiggle.repository.PlantsLibraryRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service class for managing plants library.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlantsLibraryService {

    private final PlantsLibraryRepository plantsLibraryRepository;
    private final PlantsLibraryMapper plantsLibraryMapper;

    /**
     * Find all plants with pagination and sorting.
     *
     * @param pageable pagination and sorting parameters
     * @return page of plant library DTOs
     */
    public Page<PlantsLibraryDTO> findAll(Pageable pageable) {
        return plantsLibraryRepository.findAll(pageable).map(plantsLibraryMapper::toDto);
    }

    /**
     * Find all plants without pagination.
     *
     * @return list of all plant library DTOs
     */
    public List<PlantsLibraryDTO> findAll() {
        return plantsLibraryRepository.findAll().stream()
                .map(plantsLibraryMapper::toDto)
                .toList();
    }

    /**
     * Find plant by ID.
     *
     * @param id the plant ID
     * @return optional containing the plant library DTO if found
     */
    public Optional<PlantsLibraryDTO> findById(UUID id) {
        return plantsLibraryRepository.findById(id).map(plantsLibraryMapper::toDto);
    }

    /**
     * Find plants by type with pagination and sorting.
     *
     * @param plantType the plant type
     * @param pageable  pagination and sorting parameters
     * @return page of plant library DTOs
     */
    public Page<PlantsLibraryDTO> findByPlantType(String plantType, Pageable pageable) {
        return plantsLibraryRepository
                .findByPlantTypeContainingIgnoreCase(plantType, pageable)
                .map(plantsLibraryMapper::toDto);
    }

    /**
     * Find plants by life cycle with pagination and sorting.
     *
     * @param lifeCycle the life cycle
     * @param pageable  pagination and sorting parameters
     * @return page of plant library DTOs
     */
    public Page<PlantsLibraryDTO> findByLifeCycle(String lifeCycle, Pageable pageable) {
        return plantsLibraryRepository
                .findByLifeCycleContainingIgnoreCase(lifeCycle, pageable)
                .map(plantsLibraryMapper::toDto);
    }

    /**
     * Find plants by medicinal property with pagination and sorting.
     *
     * @param medicinal the medicinal property
     * @param pageable  pagination and sorting parameters
     * @return page of plant library DTOs
     */
    public Page<PlantsLibraryDTO> findByMedicinal(Boolean medicinal, Pageable pageable) {
        return plantsLibraryRepository.findByMedicinal(medicinal, pageable).map(plantsLibraryMapper::toDto);
    }

    /**
     * Search plants by query with pagination and sorting.
     *
     * @param query    the search query
     * @param pageable pagination and sorting parameters
     * @return page of matching plant library DTOs
     */
    public Page<PlantsLibraryDTO> searchPlants(String query, Pageable pageable) {
        Specification<PlantsLibrary> spec = null;

        if (StringUtils.hasText(query)) {
            spec = where(likeIgnoreCase("commonName", query))
                    .or(likeIgnoreCase("otherName", query))
                    .or(likeIgnoreCase("scientificName", query))
                    .or(likeIgnoreCase("shortDescription", query))
                    .or(likeIgnoreCase("origin", query))
                    .or(likeIgnoreCase("plantType", query))
                    .or(likeIgnoreCase("climate", query))
                    .or(likeIgnoreCase("lifeCycle", query))
                    .or(likeIgnoreCase("wateringFrequency", query))
                    .or(likeIgnoreCase("soilType", query))
                    .or(likeIgnoreCase("sunlightRequirement", query))
                    .or(likeIgnoreCase("idealPlace", query))
                    .or(likeIgnoreCase("careLevel", query));
        }

        return plantsLibraryRepository.findAll(spec, pageable).map(plantsLibraryMapper::toDto);
    }

    /**
     * Advanced search for plants with specific criteria.
     *
     * @param criteria the search criteria
     * @param pageable pagination parameters
     * @return page of matching plant library DTOs
     */
    public Page<PlantsLibraryDTO> searchPlantsAdvanced(PlantsLibrarySearchCriteria criteria, Pageable pageable) {
        Specification<PlantsLibrary> spec = buildSearchSpecification(criteria);
        return plantsLibraryRepository.findAll(spec, pageable).map(plantsLibraryMapper::toDto);
    }

    /**
     * Build search specification from criteria.
     *
     * @param criteria the search criteria
     * @return the specification
     */
    private Specification<PlantsLibrary> buildSearchSpecification(PlantsLibrarySearchCriteria criteria) {
        Specification<PlantsLibrary> spec = Specification.where(null);

        spec = addStringCriteria(spec, criteria);
        spec = addNumericCriteria(spec, criteria);
        spec = addBooleanCriteria(spec, criteria);

        return spec;
    }

    /**
     * Add string-based criteria to specification.
     */
    private Specification<PlantsLibrary> addStringCriteria(
            Specification<PlantsLibrary> spec, PlantsLibrarySearchCriteria criteria) {
        if (StringUtils.hasText(criteria.getCommonName())) {
            spec = spec.and(likeIgnoreCase("commonName", criteria.getCommonName()));
        }
        if (StringUtils.hasText(criteria.getOtherName())) {
            spec = spec.and(likeIgnoreCase("otherName", criteria.getOtherName()));
        }
        if (StringUtils.hasText(criteria.getScientificName())) {
            spec = spec.and(likeIgnoreCase("scientificName", criteria.getScientificName()));
        }
        if (StringUtils.hasText(criteria.getOrigin())) {
            spec = spec.and(likeIgnoreCase("origin", criteria.getOrigin()));
        }
        if (StringUtils.hasText(criteria.getPlantType())) {
            spec = spec.and(likeIgnoreCase("plantType", criteria.getPlantType()));
        }
        if (StringUtils.hasText(criteria.getClimate())) {
            spec = spec.and(likeIgnoreCase("climate", criteria.getClimate()));
        }
        if (StringUtils.hasText(criteria.getLifeCycle())) {
            spec = spec.and(likeIgnoreCase("lifeCycle", criteria.getLifeCycle()));
        }
        if (StringUtils.hasText(criteria.getWateringFrequency())) {
            spec = spec.and(likeIgnoreCase("wateringFrequency", criteria.getWateringFrequency()));
        }
        if (StringUtils.hasText(criteria.getSoilType())) {
            spec = spec.and(likeIgnoreCase("soilType", criteria.getSoilType()));
        }
        if (StringUtils.hasText(criteria.getSize())) {
            spec = spec.and(likeIgnoreCase("size", criteria.getSize()));
        }
        if (StringUtils.hasText(criteria.getSunlightRequirement())) {
            spec = spec.and(likeIgnoreCase("sunlightRequirement", criteria.getSunlightRequirement()));
        }
        if (StringUtils.hasText(criteria.getGrowthRate())) {
            spec = spec.and(likeIgnoreCase("growthRate", criteria.getGrowthRate()));
        }
        if (StringUtils.hasText(criteria.getIdealPlace())) {
            spec = spec.and(likeIgnoreCase("idealPlace", criteria.getIdealPlace()));
        }
        if (StringUtils.hasText(criteria.getCareLevel())) {
            spec = spec.and(likeIgnoreCase("careLevel", criteria.getCareLevel()));
        }
        if (StringUtils.hasText(criteria.getBestPlantingSeason())) {
            spec = spec.and(likeIgnoreCase("bestPlantingSeason", criteria.getBestPlantingSeason()));
        }
        return spec;
    }

    /**
     * Add numeric criteria to specification.
     */
    private Specification<PlantsLibrary> addNumericCriteria(
            Specification<PlantsLibrary> spec, PlantsLibrarySearchCriteria criteria) {
        if (criteria.getTimeToHarvest() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("timeToHarvest"), criteria.getTimeToHarvest()));
        }
        return spec;
    }

    /**
     * Add boolean criteria to specification.
     */
    private Specification<PlantsLibrary> addBooleanCriteria(
            Specification<PlantsLibrary> spec, PlantsLibrarySearchCriteria criteria) {
        if (criteria.getFlower() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("flower"), criteria.getFlower()));
        }
        if (criteria.getFruit() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("fruit"), criteria.getFruit()));
        }
        if (criteria.getMedicinal() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("medicinal"), criteria.getMedicinal()));
        }
        return spec;
    }

    /**
     * Create a new plant.
     *
     * @param plantsLibraryDTO the plant library DTO to create
     * @return the created plant library DTO
     */
    @Transactional
    public PlantsLibraryDTO create(PlantsLibraryDTO plantsLibraryDTO) {
        OffsetDateTime now = OffsetDateTime.now();
        plantsLibraryDTO.setCreatedAt(now);
        plantsLibraryDTO.setUpdatedAt(now);

        PlantsLibrary plantsLibrary = plantsLibraryMapper.toEntity(plantsLibraryDTO);
        PlantsLibrary savedPlantsLibrary = plantsLibraryRepository.save(plantsLibrary);

        return plantsLibraryMapper.toDto(savedPlantsLibrary);
    }

    /**
     * Update an existing plant.
     *
     * @param id               the plant ID
     * @param plantsLibraryDTO the updated plant details
     * @return the updated plant library DTO if found
     */
    @Transactional
    public Optional<PlantsLibraryDTO> update(UUID id, PlantsLibraryDTO plantsLibraryDTO) {
        return plantsLibraryRepository.findById(id).map(existingPlant -> {
            // Update text fields
            existingPlant.setCommonName(plantsLibraryDTO.getCommonName());
            existingPlant.setOtherName(plantsLibraryDTO.getOtherName());
            existingPlant.setScientificName(plantsLibraryDTO.getScientificName());
            existingPlant.setShortDescription(plantsLibraryDTO.getShortDescription());
            existingPlant.setOrigin(plantsLibraryDTO.getOrigin());
            existingPlant.setPlantType(plantsLibraryDTO.getPlantType());
            existingPlant.setClimate(plantsLibraryDTO.getClimate());
            existingPlant.setLifeCycle(plantsLibraryDTO.getLifeCycle());
            existingPlant.setWateringFrequency(plantsLibraryDTO.getWateringFrequency());
            existingPlant.setSoilType(plantsLibraryDTO.getSoilType());
            existingPlant.setSize(plantsLibraryDTO.getSize());
            existingPlant.setSunlightRequirement(plantsLibraryDTO.getSunlightRequirement());
            existingPlant.setGrowthRate(plantsLibraryDTO.getGrowthRate());
            existingPlant.setIdealPlace(plantsLibraryDTO.getIdealPlace());
            existingPlant.setCareLevel(plantsLibraryDTO.getCareLevel());
            existingPlant.setImageUrl(plantsLibraryDTO.getImageUrl());
            existingPlant.setBestPlantingSeason(plantsLibraryDTO.getBestPlantingSeason());
            existingPlant.setGardeningTips(plantsLibraryDTO.getGardeningTips());
            existingPlant.setPruningGuide(plantsLibraryDTO.getPruningGuide());

            // Update numeric fields
            existingPlant.setSeedDepth(plantsLibraryDTO.getSeedDepth());
            existingPlant.setGerminationTime(plantsLibraryDTO.getGerminationTime());
            existingPlant.setTimeToHarvest(plantsLibraryDTO.getTimeToHarvest());

            // Update boolean fields
            existingPlant.setFlower(plantsLibraryDTO.getFlower());
            existingPlant.setFruit(plantsLibraryDTO.getFruit());
            existingPlant.setMedicinal(plantsLibraryDTO.getMedicinal());

            // Update temperature range through mapper
            existingPlant.setTemperatureRange(plantsLibraryMapper.createTemperatureRange(plantsLibraryDTO));

            // Update lists
            existingPlant.setCommonPests(plantsLibraryDTO.getCommonPests());
            existingPlant.setCommonDiseases(plantsLibraryDTO.getCommonDiseases());
            existingPlant.setCompanionPlants(plantsLibraryDTO.getCompanionPlants());
            existingPlant.setAvoidPlantingWith(plantsLibraryDTO.getAvoidPlantingWith());
            existingPlant.setPestDiseasePreventionTips(plantsLibraryDTO.getPestDiseasePreventionTips());
            existingPlant.setCoolFacts(plantsLibraryDTO.getCoolFacts());
            existingPlant.setEdibleParts(plantsLibraryDTO.getEdibleParts());

            existingPlant.setUpdatedAt(OffsetDateTime.now());

            // Save and convert back to DTO
            return plantsLibraryMapper.toDto(plantsLibraryRepository.save(existingPlant));
        });
    }

    /**
     * Delete a plant by ID.
     *
     * @param id the plant ID
     */
    @Transactional
    public void delete(UUID id) {
        plantsLibraryRepository.deleteById(id);
    }

    /**
     * Create a like specification for case-insensitive string comparison.
     *
     * @param field the field name
     * @param value the search value
     * @return the specification
     */
    private Specification<PlantsLibrary> likeIgnoreCase(String field, String value) {
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }
}
