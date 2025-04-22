package dev.solace.twiggle.service;

import dev.solace.twiggle.config.PlantApiConfig;
import dev.solace.twiggle.dto.plant.*;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlantApiService {

    private final PlantApiConfig plantApiConfig;
    private final RestTemplate restTemplate;

    // List of trusted domains for external API calls
    private static final List<String> TRUSTED_DOMAINS = Arrays.asList("perenual.com");

    /**
     * Get a list of plants based on various filter criteria.
     *
     * @param request Plant list request parameters
     * @return PlantListResponseDTO containing the filtered plants
     */
    public PlantListResponseDTO getPlantList(PlantListRequestDTO request) {
        try {
            validateApiKey();

            String baseUrl = plantApiConfig.getSpeciesListUrl();
            validateTrustedDomain(baseUrl);

            UriComponentsBuilder builder =
                    UriComponentsBuilder.fromUriString(baseUrl).queryParam("key", plantApiConfig.getApiKey());

            addQueryParameters(builder, request);

            String url = builder.build().toUriString();
            log.debug("Calling Perenual API: {}", url);

            ResponseEntity<PlantListResponseDTO> response = restTemplate.getForEntity(url, PlantListResponseDTO.class);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Error calling Perenual Plant API: {}", e.getMessage());
            throw new CustomException(
                    "Error fetching plant data from external API: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.EXTERNAL_API_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error calling Perenual Plant API: {}", e.getMessage(), e);
            throw new CustomException(
                    "Unexpected error fetching plant data", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Add query parameters to the URI builder based on the request parameters.
     *
     * @param builder The URI builder to add parameters to
     * @param request The request containing filter parameters
     */
    private void addQueryParameters(UriComponentsBuilder builder, PlantListRequestDTO request) {
        validateRequestParameters(request);
        addBasicParameters(builder, request);
        addBooleanParameters(builder, request);
        addStringParameters(builder, request);
    }

    /**
     * Validate that the request parameters meet security requirements
     *
     * @param request The request to validate
     */
    private void validateRequestParameters(PlantListRequestDTO request) {
        if (request.getPage() != null && (request.getPage() < 1)) {
            throw new CustomException("Invalid page parameter", HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
        }

        if (request.getOrder() != null && !Arrays.asList("asc", "desc").contains(request.getOrder())) {
            throw new CustomException("Invalid order parameter", HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
        }

        if (request.getCycle() != null
                && !Arrays.asList("perennial", "annual", "biennial", "biannual").contains(request.getCycle())) {
            throw new CustomException("Invalid cycle parameter", HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
        }

        if (request.getWatering() != null
                && !Arrays.asList("frequent", "average", "minimum", "none").contains(request.getWatering())) {
            throw new CustomException("Invalid watering parameter", HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
        }

        if (request.getSunlight() != null
                && !Arrays.asList("full_shade", "part_shade", "sun-part_shade", "full_sun")
                        .contains(request.getSunlight())) {
            throw new CustomException("Invalid sunlight parameter", HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
        }

        if (request.getHardiness() != null && (request.getHardiness() < 1 || request.getHardiness() > 13)) {
            throw new CustomException(
                    "Invalid hardiness parameter", HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
        }

        if (request.getQ() != null) {
            // Validate search query to prevent injection attacks
            validateSearchQuery(request.getQ());
        }
    }

    /**
     * Validate that a URL is from a trusted domain
     *
     * @param url The URL to validate
     */
    private void validateTrustedDomain(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();

            if (host == null || !TRUSTED_DOMAINS.stream().anyMatch(domain -> host.endsWith(domain))) {
                throw new CustomException(
                        "Untrusted domain for external API",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorCode.EXTERNAL_API_ERROR);
            }
        } catch (URISyntaxException e) {
            throw new CustomException(
                    "Invalid URL format for external API",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    /**
     * Validate search query to prevent injection attacks
     *
     * @param query The search query to validate
     */
    private void validateSearchQuery(String query) {
        // Check for potentially dangerous characters or patterns
        if (query.contains("<")
                || query.contains(">")
                || query.contains("\"")
                || query.contains("'")
                || query.contains(";")
                || query.contains("--")) {
            throw new CustomException(
                    "Invalid characters in search query", HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
        }
    }

    private void addBasicParameters(UriComponentsBuilder builder, PlantListRequestDTO request) {
        if (request.getPage() != null) {
            builder.queryParam("page", request.getPage());
        }
        if (request.getOrder() != null) {
            builder.queryParam("order", request.getOrder());
        }
        if (request.getCycle() != null) {
            builder.queryParam("cycle", request.getCycle());
        }
        if (request.getWatering() != null) {
            builder.queryParam("watering", request.getWatering());
        }
        if (request.getSunlight() != null) {
            builder.queryParam("sunlight", request.getSunlight());
        }
        if (request.getHardiness() != null) {
            builder.queryParam("hardiness", request.getHardiness());
        }
    }

    private void addBooleanParameters(UriComponentsBuilder builder, PlantListRequestDTO request) {
        if (request.getEdible() != null) {
            builder.queryParam("edible", request.getEdible() ? 1 : 0);
        }
        if (request.getPoisonous() != null) {
            builder.queryParam("poisonous", request.getPoisonous() ? 1 : 0);
        }
        if (request.getIndoor() != null) {
            builder.queryParam("indoor", request.getIndoor() ? 1 : 0);
        }
    }

    private void addStringParameters(UriComponentsBuilder builder, PlantListRequestDTO request) {
        if (request.getQ() != null && !request.getQ().isBlank()) {
            builder.queryParam("q", request.getQ());
        }
    }

    /**
     * Get detailed information about a specific plant.
     *
     * @param id ID of the plant to get details for
     * @return PlantDetailsDTO containing the plant details
     */
    public PlantDetailsDTO getPlantDetails(Long id) {
        try {
            validateApiKey();
            validatePlantId(id);

            String url = buildPlantDetailsUrl(id);
            log.debug("Calling Perenual API for plant details: {}", url);

            // Validate the URL before making the request
            validateTrustedDomain(url);

            ResponseEntity<PlantDetailsDTO> response = restTemplate.getForEntity(url, PlantDetailsDTO.class);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            handleApiError("Perenual Plant Details API", e);
            return null; // This line will never be reached due to the exception
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            handleUnexpectedError("Perenual Plant Details API", e);
            return null; // This line will never be reached due to the exception
        }
    }

    /**
     * Get a list of plant diseases and pests based on filter criteria.
     *
     * @param request Disease/pest list request parameters
     * @return DiseasePestListResponseDTO containing the filtered diseases/pests
     */
    public DiseasePestListResponseDTO getDiseasePestList(DiseasePestListRequestDTO request) {
        try {
            validateApiKey();

            String baseUrl = plantApiConfig.getDiseasePestListUrl();
            validateTrustedDomain(baseUrl);

            UriComponentsBuilder builder =
                    UriComponentsBuilder.fromUriString(baseUrl).queryParam("key", plantApiConfig.getApiKey());

            addDiseasePestQueryParameters(builder, request);

            String url = builder.build().toUriString();
            log.debug("Calling Perenual Disease/Pest API: {}", url);

            ResponseEntity<DiseasePestListResponseDTO> response =
                    restTemplate.getForEntity(url, DiseasePestListResponseDTO.class);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            handleApiError("Perenual Disease/Pest API", e);
            return null; // This line will never be reached due to the exception
        } catch (Exception e) {
            handleUnexpectedError("Perenual Disease/Pest API", e);
            return null; // This line will never be reached due to the exception
        }
    }

    private void validateApiKey() {
        if (plantApiConfig.getApiKey() == null
                || plantApiConfig.getApiKey().isBlank()
                || "your_api_key_here".equals(plantApiConfig.getApiKey())) {
            throw new CustomException(
                    "Perenual API key is not configured properly",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.CONFIGURATION_ERROR);
        }
    }

    private void validatePlantId(Long id) {
        if (id == null) {
            throw new CustomException("Plant ID is required", HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
        }
    }

    private String buildPlantDetailsUrl(Long id) {
        String url = plantApiConfig.getSpeciesDetailsUrl(id) + "?key=" + plantApiConfig.getApiKey();
        log.info("Constructed URL: {}", url);
        return url;
    }

    private void addDiseasePestQueryParameters(UriComponentsBuilder builder, DiseasePestListRequestDTO request) {
        // Validate disease/pest request parameters
        if (request.getQ() != null) {
            validateSearchQuery(request.getQ());
        }

        if (request.getId() != null) {
            builder.queryParam("id", request.getId());
        }

        if (request.getPage() != null) {
            builder.queryParam("page", request.getPage());
        }

        if (request.getQ() != null && !request.getQ().isBlank()) {
            builder.queryParam("q", request.getQ());
        }
    }

    private void handleApiError(String apiName, HttpClientErrorException e) {
        log.error("Error calling {}: {}", apiName, e.getMessage());
        throw new CustomException(
                "Error fetching data from external API: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.EXTERNAL_API_ERROR);
    }

    private void handleUnexpectedError(String apiName, Exception e) {
        log.error("Unexpected error calling {}: {}", apiName, e.getMessage(), e);
        throw new CustomException(
                "Unexpected error fetching data", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
    }
}
