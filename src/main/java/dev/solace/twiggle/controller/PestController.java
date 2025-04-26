package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.PestDTO;
import dev.solace.twiggle.service.PestService;
import dev.solace.twiggle.util.ResponseUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pests")
@RequiredArgsConstructor
@Slf4j
@RateLimiter(name = "standard-api")
public class PestController {

    private final PestService pestService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PestDTO>>> getAllPests() {
        List<PestDTO> pests = pestService.findAll();
        return ResponseUtil.success("Successfully retrieved all pests", pests);
    }

    @GetMapping("/plant-library/{plantLibraryId}")
    public ResponseEntity<ApiResponse<List<PestDTO>>> getPestsByPlantLibraryId(@PathVariable UUID plantLibraryId) {
        List<PestDTO> pests = pestService.findByPlantLibraryId(plantLibraryId);
        return ResponseUtil.success("Successfully retrieved pests for plant library ID: " + plantLibraryId, pests);
    }
}
