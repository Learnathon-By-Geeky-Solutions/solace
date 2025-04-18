package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.WeatherDTO;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.WeatherService;
import dev.solace.twiggle.util.ResponseUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for weather data.
 */
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Slf4j
@RateLimiter(name = "standard-api")
@Validated
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * Get current weather for a location.
     *
     * @param location the location to get weather for
     * @return the current weather data
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<@Valid WeatherDTO>> getCurrentWeather(
            @RequestParam @NotBlank(message = "Location cannot be blank") String location) {
        try {
            log.info("Getting current weather for location: {}", location);
            WeatherDTO weather = weatherService.getCurrentWeather(location);
            return ResponseUtil.success("Successfully retrieved current weather", weather);
        } catch (Exception e) {
            log.error("Error retrieving current weather for location {}: {}", location, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve current weather", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get current weather for coordinates.
     *
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @return the current weather data
     */
    @GetMapping("/current/coordinates")
    public ResponseEntity<ApiResponse<@Valid WeatherDTO>> getCurrentWeatherByCoordinates(
            @RequestParam
                    @Min(value = -90, message = "Latitude must be between -90 and 90")
                    @Max(value = 90, message = "Latitude must be between -90 and 90")
                    double latitude,
            @RequestParam
                    @Min(value = -180, message = "Longitude must be between -180 and 180")
                    @Max(value = 180, message = "Longitude must be between -180 and 180")
                    double longitude) {
        try {
            log.info("Getting current weather for coordinates: lat={}, lon={}", latitude, longitude);
            WeatherDTO weather = weatherService.getCurrentWeatherByCoordinates(latitude, longitude);
            return ResponseUtil.success("Successfully retrieved current weather", weather);
        } catch (Exception e) {
            log.error(
                    "Error retrieving current weather for coordinates lat={}, lon={}: {}",
                    latitude,
                    longitude,
                    e.getMessage(),
                    e);
            throw new CustomException(
                    "Failed to retrieve current weather", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get weather forecast for a location.
     *
     * @param location the location to get forecast for
     * @param days number of days to forecast (1-7)
     * @return the weather forecast data
     */
    @GetMapping("/forecast")
    public ResponseEntity<ApiResponse<@Valid WeatherDTO>> getWeatherForecast(
            @RequestParam @NotBlank(message = "Location cannot be blank") String location,
            @RequestParam(defaultValue = "3")
                    @Min(value = 1, message = "Days must be between 1 and 7")
                    @Max(value = 7, message = "Days must be between 1 and 7")
                    int days) {
        try {
            log.info("Getting weather forecast for location: {} for {} days", location, days);
            WeatherDTO forecast = weatherService.getWeatherForecast(location, days);
            return ResponseUtil.success("Successfully retrieved weather forecast", forecast);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving weather forecast for location {}: {}", location, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve weather forecast", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get weather forecast for coordinates.
     *
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @param days number of days to forecast (1-7)
     * @return the weather forecast data
     */
    @GetMapping("/forecast/coordinates")
    public ResponseEntity<ApiResponse<@Valid WeatherDTO>> getWeatherForecastByCoordinates(
            @RequestParam
                    @Min(value = -90, message = "Latitude must be between -90 and 90")
                    @Max(value = 90, message = "Latitude must be between -90 and 90")
                    double latitude,
            @RequestParam
                    @Min(value = -180, message = "Longitude must be between -180 and 180")
                    @Max(value = 180, message = "Longitude must be between -180 and 180")
                    double longitude,
            @RequestParam(defaultValue = "3")
                    @Min(value = 1, message = "Days must be between 1 and 7")
                    @Max(value = 7, message = "Days must be between 1 and 7")
                    int days) {
        try {
            log.info("Getting weather forecast for coordinates: lat={}, lon={} for {} days", latitude, longitude, days);
            WeatherDTO forecast = weatherService.getWeatherForecastByCoordinates(latitude, longitude, days);
            return ResponseUtil.success("Successfully retrieved weather forecast", forecast);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    "Error retrieving weather forecast for coordinates lat={}, lon={}: {}",
                    latitude,
                    longitude,
                    e.getMessage(),
                    e);
            throw new CustomException(
                    "Failed to retrieve weather forecast", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get garden-specific weather information and advice.
     *
     * @param location the location of the garden
     * @param gardenPlanId optional garden plan ID for plant-specific advice
     * @return weather data with gardening-specific information
     */
    @GetMapping("/garden")
    public ResponseEntity<ApiResponse<@Valid WeatherDTO>> getGardenWeather(
            @RequestParam @NotBlank(message = "Location cannot be blank") String location,
            @RequestParam(required = false) String gardenPlanId) {
        try {
            log.info(
                    "Getting garden weather for location: {}, garden plan ID: {}",
                    location,
                    gardenPlanId != null ? gardenPlanId : "not provided");

            WeatherDTO gardenWeather = weatherService.getGardenWeather(location, Optional.ofNullable(gardenPlanId));
            return ResponseUtil.success("Successfully retrieved garden weather information", gardenWeather);
        } catch (Exception e) {
            log.error("Error retrieving garden weather for location {}: {}", location, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve garden weather information",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get garden-specific weather information and advice for coordinates.
     *
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @param gardenPlanId optional garden plan ID for plant-specific advice
     * @return weather data with gardening-specific information
     */
    @GetMapping("/garden/coordinates")
    public ResponseEntity<ApiResponse<@Valid WeatherDTO>> getGardenWeatherByCoordinates(
            @RequestParam
                    @Min(value = -90, message = "Latitude must be between -90 and 90")
                    @Max(value = 90, message = "Latitude must be between -90 and 90")
                    double latitude,
            @RequestParam
                    @Min(value = -180, message = "Longitude must be between -180 and 180")
                    @Max(value = 180, message = "Longitude must be between -180 and 180")
                    double longitude,
            @RequestParam(required = false) String gardenPlanId) {
        try {
            log.info(
                    "Getting garden weather for coordinates: lat={}, lon={}, garden plan ID: {}",
                    latitude,
                    longitude,
                    gardenPlanId != null ? gardenPlanId : "not provided");

            WeatherDTO gardenWeather = weatherService.getGardenWeatherByCoordinates(
                    latitude, longitude, Optional.ofNullable(gardenPlanId));
            return ResponseUtil.success("Successfully retrieved garden weather information", gardenWeather);
        } catch (Exception e) {
            log.error(
                    "Error retrieving garden weather for coordinates lat={}, lon={}: {}",
                    latitude,
                    longitude,
                    e.getMessage(),
                    e);
            throw new CustomException(
                    "Failed to retrieve garden weather information",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get weather hazards that could potentially affect plants.
     *
     * @param location the location to check for hazards
     * @return weather data focusing on plant hazards
     */
    @GetMapping("/hazards")
    public ResponseEntity<ApiResponse<@Valid WeatherDTO>> getWeatherHazards(
            @RequestParam @NotBlank(message = "Location cannot be blank") String location) {
        try {
            log.info("Getting weather hazards for location: {}", location);

            // For hazards, we'll use the garden weather endpoint since it includes plant hazards
            WeatherDTO hazardData = weatherService.getGardenWeather(location, Optional.empty());
            return ResponseUtil.success("Successfully retrieved weather hazards", hazardData);
        } catch (Exception e) {
            log.error("Error retrieving weather hazards for location {}: {}", location, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve weather hazards", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get weather hazards that could potentially affect plants for coordinates.
     *
     * @param latitude the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @return weather data focusing on plant hazards
     */
    @GetMapping("/hazards/coordinates")
    public ResponseEntity<ApiResponse<@Valid WeatherDTO>> getWeatherHazardsByCoordinates(
            @RequestParam
                    @Min(value = -90, message = "Latitude must be between -90 and 90")
                    @Max(value = 90, message = "Latitude must be between -90 and 90")
                    double latitude,
            @RequestParam
                    @Min(value = -180, message = "Longitude must be between -180 and 180")
                    @Max(value = 180, message = "Longitude must be between -180 and 180")
                    double longitude) {
        try {
            log.info("Getting weather hazards for coordinates: lat={}, lon={}", latitude, longitude);

            // For hazards, we'll use the garden weather endpoint since it includes plant hazards
            WeatherDTO hazardData = weatherService.getGardenWeatherByCoordinates(latitude, longitude, Optional.empty());
            return ResponseUtil.success("Successfully retrieved weather hazards", hazardData);
        } catch (Exception e) {
            log.error(
                    "Error retrieving weather hazards for coordinates lat={}, lon={}: {}",
                    latitude,
                    longitude,
                    e.getMessage(),
                    e);
            throw new CustomException(
                    "Failed to retrieve weather hazards", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }
}
