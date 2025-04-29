package dev.solace.twiggle.service.impl;

import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Facade for the World Weather Online API client.
 * This class handles all the API calls and error handling.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorldWeatherOnlineFacade {

    private static final String FORMAT_PATTERN = "%f,%f";
    private final WorldWeatherOnlineApiClient weatherApiClient;

    /**
     * Fetch weather data for a location with specified number of days.
     *
     * @param location The location string
     * @param days Number of days for forecast
     * @return The raw JSON response from the API
     */
    public String fetch(String location, int days) {
        log.info("Fetching weather for location: {} for {} days", location, days);
        if (days == 1) {
            return executeApiCall(
                    () -> weatherApiClient.getCurrentWeather(location),
                    location,
                    "Failed to retrieve current weather data");
        } else {
            return executeApiCall(
                    () -> weatherApiClient.getWeatherForecast(location, days),
                    location,
                    "Failed to retrieve weather forecast data");
        }
    }

    /**
     * Fetch weather data for coordinates with specified number of days.
     *
     * @param latitude The latitude
     * @param longitude The longitude
     * @param days Number of days for forecast
     * @return The raw JSON response from the API
     */
    public String fetchByCoordinates(double latitude, double longitude, int days) {
        log.info("Fetching weather for coordinates: {}, {} for {} days", latitude, longitude, days);
        String locationCoords = formatCoordinates(latitude, longitude);
        if (days == 1) {
            return executeApiCall(
                    () -> weatherApiClient.getCurrentWeatherByCoordinates(latitude, longitude),
                    locationCoords,
                    "Failed to retrieve current weather data");
        } else {
            return executeApiCall(
                    () -> weatherApiClient.getWeatherForecastByCoordinates(latitude, longitude, days),
                    locationCoords,
                    "Failed to retrieve weather forecast data");
        }
    }

    /**
     * Execute a weather API call with consistent error handling
     */
    private String executeApiCall(Supplier<String> apiCallFunction, String location, String errorMessage) {
        try {
            return apiCallFunction.get();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching weather for {}: {}", location, e.getMessage(), e);
            throw new CustomException(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    /**
     * Format coordinates as a string
     */
    private String formatCoordinates(double latitude, double longitude) {
        return String.format(FORMAT_PATTERN, latitude, longitude);
    }
}
