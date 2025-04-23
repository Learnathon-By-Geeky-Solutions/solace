package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.PlantDiseaseDTO;
import dev.solace.twiggle.service.PlantDiseaseService;
import dev.solace.twiggle.util.ResponseUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plant-diseases")
@RequiredArgsConstructor
@Slf4j
@RateLimiter(name = "standard-api")
public class PlantDiseaseController {

    private final PlantDiseaseService plantDiseaseService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlantDiseaseDTO>>> getAllDiseases() {
        List<PlantDiseaseDTO> diseases = plantDiseaseService.findAll();
        return ResponseUtil.success("Successfully retrieved all diseases", diseases);
    }

    @GetMapping("/plant-library/{plantLibraryId}")
    public ResponseEntity<ApiResponse<List<PlantDiseaseDTO>>> getDiseasesByPlantLibraryId(
            @PathVariable UUID plantLibraryId) {
        List<PlantDiseaseDTO> diseases = plantDiseaseService.findByPlantLibraryId(plantLibraryId);
        return ResponseUtil.success(
                "Successfully retrieved diseases for plant library ID: " + plantLibraryId, diseases);
    }
}
