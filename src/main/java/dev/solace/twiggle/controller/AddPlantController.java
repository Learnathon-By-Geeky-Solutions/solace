package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.AddPlantDTO;
import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.PlantDTO;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.AddPlantService;
import dev.solace.twiggle.util.ResponseUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for plants.
 */
@RestController
@RequestMapping("/api/plants")
@RequiredArgsConstructor
@Slf4j
@RateLimiter(name = "standard-api")
public class AddPlantController {
    private final AddPlantService addPlantService;

    @PostMapping("/from-library")
    public ResponseEntity<ApiResponse<PlantDTO>> addPlant(@Valid @RequestBody AddPlantDTO addPlantDTO) {
        try {
            PlantDTO createdPlant = addPlantService.addFromLibrary(addPlantDTO);
            return ResponseUtil.success("Plant added successfully", createdPlant);
        } catch (Exception e) {
            log.error("Error adding plant: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to add plant", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }
}
