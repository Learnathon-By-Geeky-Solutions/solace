package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.plant.*;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.PlantApiService;
import dev.solace.twiggle.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plants/external")
@RequiredArgsConstructor
@Slf4j
public class PlantApiController {

    private final PlantApiService plantApiService;

    /**
     * Get a list of plants from the external API.
     *
     * @param request Plant list request parameters
     * @return List of plants matching the criteria
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PlantListResponseDTO>> getPlants(@Valid PlantListRequestDTO request) {
        try {
            PlantListResponseDTO response = plantApiService.getPlantList(request);
            return ResponseUtil.success("Successfully retrieved plants from external API", response);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving plants from external API: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve plants from external API",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get detailed information about a specific plant.
     *
     * @param id ID of the plant to retrieve details for
     * @return Detailed plant information
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlantDetailsDTO>> getPlantDetails(@PathVariable Long id) {
        try {
            PlantDetailsDTO response = plantApiService.getPlantDetails(id);
            return ResponseUtil.success("Successfully retrieved plant details from external API", response);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving plant details from external API: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve plant details from external API",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get a list of plant diseases and pests from the external API.
     *
     * @param request Disease/pest list request parameters
     * @return List of diseases/pests matching the criteria
     */
    @GetMapping("/diseases-pests")
    public ResponseEntity<ApiResponse<DiseasePestListResponseDTO>> getDiseasesAndPests(
            @Valid DiseasePestListRequestDTO request) {
        try {
            DiseasePestListResponseDTO response = plantApiService.getDiseasePestList(request);
            return ResponseUtil.success("Successfully retrieved diseases and pests from external API", response);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving diseases and pests from external API: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve diseases and pests from external API",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }
}
