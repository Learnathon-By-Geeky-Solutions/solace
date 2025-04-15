// package dev.solace.twiggle.service;
//
// import dev.solace.twiggle.model.postgres.Plant;
// import dev.solace.twiggle.repository.postgres.PlantRepository;
// import java.time.OffsetDateTime;
// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
//
/// **
// * Service class for managing plants.
// */
// @Service
// @RequiredArgsConstructor
// @Slf4j
// public class PlantService {
//
//    private final PlantRepository plantRepository;
//
//    /**
//     * Find all plants.
//     *
//     * @return list of all plants
//     */
//    public List<Plant> findAll() {
//        return plantRepository.findAll();
//    }
//
//    /**
//     * Find plant by ID.
//     *
//     * @param id the plant ID
//     * @return optional containing the plant if found
//     */
//    public Optional<Plant> findById(UUID id) {
//        return plantRepository.findById(id);
//    }
//
//    /**
//     * Find plants by garden plan ID.
//     *
//     * @param gardenPlanId the garden plan ID
//     * @return list of plants in the garden plan
//     */
//    public List<Plant> findByGardenPlanId(UUID gardenPlanId) {
//        return plantRepository.findByGardenPlanId(gardenPlanId);
//    }
//
//    /**
//     * Find plants by type.
//     *
//     * @param type the plant type
//     * @return list of plants of the specified type
//     */
//    public List<Plant> findByType(String type) {
//        return plantRepository.findByType(type);
//    }
//
//    /**
//     * Create a new plant.
//     *
//     * @param plant the plant to create
//     * @return the created plant
//     */
//    @Transactional
//    public Plant create(Plant plant) {
//        OffsetDateTime now = OffsetDateTime.now();
//        plant.setCreatedAt(now);
//        plant.setUpdatedAt(now);
//        return plantRepository.save(plant);
//    }
//
//    /**
//     * Update an existing plant.
//     *
//     * @param id the plant ID
//     * @param plant the updated plant details
//     * @return the updated plant
//     */
//    @Transactional
//    public Optional<Plant> update(UUID id, Plant plant) {
//        return plantRepository.findById(id).map(existingPlant -> {
//            existingPlant.setName(plant.getName());
//            existingPlant.setType(plant.getType());
//            existingPlant.setDescription(plant.getDescription());
//            existingPlant.setWateringFrequency(plant.getWateringFrequency());
//            existingPlant.setSunlightRequirements(plant.getSunlightRequirements());
//            existingPlant.setPositionX(plant.getPositionX());
//            existingPlant.setPositionY(plant.getPositionY());
//            existingPlant.setImageUrl(plant.getImageUrl());
//            existingPlant.setUpdatedAt(OffsetDateTime.now());
//            return plantRepository.save(existingPlant);
//        });
//    }
//
//    /**
//     * Delete a plant by ID.
//     *
//     * @param id the plant ID
//     */
//    @Transactional
//    public void delete(UUID id) {
//        plantRepository.deleteById(id);
//    }
// }
