package dev.solace.twiggle.service.impl;

import dev.solace.twiggle.config.WeatherApiConfig;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Client for making requests to the World Weather Online API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorldWeatherOnlineApiClient {

    private static final String FORMAT_PARAM = "format";
    private static final String FORMAT_JSON = "json";
    private static final String NUM_OF_DAYS_PARAM = "num_of_days";
    private static final String ALERTS_PARAM = "alerts";
    private static final String WEATHER_ENDPOINT = "/weather.ashx";
    private static final String YES_VALUE = "yes";

    // List of trusted domains for external API calls
    private static final List<String> TRUSTED_DOMAINS = Arrays.asList("worldweatheronline.com", "wttr.in");

    private final RestTemplate restTemplate = new RestTemplate();
    private final WeatherApiConfig weatherApiConfig;

    /**
     * Get current weather data from the World Weather Online API.
     *
     * @param location The location to get weather for
     * @return The JSON response from the API
     */
    public String getCurrentWeather(String location) {
        Map<String, String> queryParams = new HashMap<>();
        // Validate location input to prevent injection
        queryParams.put("q", validateLocationInput(location));
        queryParams.put(FORMAT_PARAM, FORMAT_JSON);
        queryParams.put(NUM_OF_DAYS_PARAM, "1");
        queryParams.put("fx", YES_VALUE);
        queryParams.put("cc", YES_VALUE);
        queryParams.put("aqi", YES_VALUE);
        queryParams.put(ALERTS_PARAM, YES_VALUE);

        return makeApiCall(WEATHER_ENDPOINT, queryParams);
    }

    /**
     * Get current weather data from the World Weather Online API using latitude and longitude.
     *
     * @param latitude The latitude in decimal degrees
     * @param longitude The longitude in decimal degrees
     * @return The JSON response from the API
     */
    public String getCurrentWeatherByCoordinates(double latitude, double longitude) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", formatCoordinates(latitude, longitude));
        queryParams.put(FORMAT_PARAM, FORMAT_JSON);
        queryParams.put(NUM_OF_DAYS_PARAM, "1");
        queryParams.put("fx", YES_VALUE);
        queryParams.put("cc", YES_VALUE);
        queryParams.put("aqi", YES_VALUE);
        queryParams.put(ALERTS_PARAM, YES_VALUE);

        return makeApiCall(WEATHER_ENDPOINT, queryParams);
    }

    /**
     * Get weather forecast data from the World Weather Online API.
     *
     * @param location The location to get forecast for
     * @param days Number of days for the forecast (1-14)
     * @return The JSON response from the API
     */
    public String getWeatherForecast(String location, int days) {
        if (days < 1 || days > 14) {
            log.warn("Invalid days parameter: {}. Using default value of 3", days);
            days = 3;
        }

        Map<String, String> queryParams = new HashMap<>();
        // Validate location input to prevent injection
        queryParams.put("q", validateLocationInput(location));
        queryParams.put(FORMAT_PARAM, FORMAT_JSON);
        queryParams.put(NUM_OF_DAYS_PARAM, String.valueOf(days));
        queryParams.put("fx", YES_VALUE);
        queryParams.put("cc", YES_VALUE);
        queryParams.put("tp", "24"); // 3-hourly forecast
        queryParams.put("aqi", YES_VALUE);
        queryParams.put(ALERTS_PARAM, YES_VALUE);

        return makeApiCall(WEATHER_ENDPOINT, queryParams);
    }

    /**
     * Get weather forecast data from the World Weather Online API using latitude and longitude.
     *
     * @param latitude The latitude in decimal degrees
     * @param longitude The longitude in decimal degrees
     * @param days Number of days for the forecast (1-14)
     * @return The JSON response from the API
     */
    public String getWeatherForecastByCoordinates(double latitude, double longitude, int days) {
        if (days < 1 || days > 14) {
            log.warn("Invalid days parameter: {}. Using default value of 3", days);
            days = 3;
        }

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", formatCoordinates(latitude, longitude));
        queryParams.put(FORMAT_PARAM, FORMAT_JSON);
        queryParams.put(NUM_OF_DAYS_PARAM, String.valueOf(days));
        queryParams.put("fx", YES_VALUE);
        queryParams.put("cc", YES_VALUE);
        queryParams.put("tp", "24"); // 3-hourly forecast
        queryParams.put("aqi", YES_VALUE);
        queryParams.put(ALERTS_PARAM, YES_VALUE);

        return makeApiCall(WEATHER_ENDPOINT, queryParams);
    }

    /**
     * Makes a request to the World Weather Online API.
     *
     * @param endpoint The API endpoint
     * @param queryParams The query parameters
     * @return The API response
     */
    private String makeApiCall(String endpoint, Map<String, String> queryParams) {
        try {
            // Add API key to query parameters
            queryParams.put("key", weatherApiConfig.getKey());

            // Validate the base URL before constructing the request
            String baseUrl = weatherApiConfig.getBaseUrl();
            validateTrustedDomain(baseUrl);

            // Convert Map to MultiValueMap
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            queryParams.forEach(multiValueMap::add);

            // Build the API URL
            URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                    .path(endpoint)
                    .queryParams(multiValueMap)
                    .build()
                    .toUri();

            // Final validation of the constructed URI
            validateTrustedDomain(uri.toString());

            log.debug("Making API call to: {}", uri);
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                log.error("API call failed with status: {}", response.getStatusCode());
                throw new CustomException(
                        "Failed to retrieve weather data",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorCode.EXTERNAL_API_ERROR);
            }
        } catch (Exception e) {
            log.error("Error making API call: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve weather data", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.EXTERNAL_API_ERROR);
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
     * Validate location input to prevent injection attacks
     *
     * @param location The location to validate
     * @return The validated location
     */
    private String validateLocationInput(String location) {
        if (location == null || location.isBlank()) {
            throw new CustomException(
                    "Location parameter is required", HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
        }

        // Check for potentially dangerous characters or patterns
        if (location.contains("<")
                || location.contains(">")
                || location.contains("\"")
                || location.contains("'")
                || location.contains(";")
                || location.contains("--")
                || location.contains("://")) {
            throw new CustomException(
                    "Invalid characters in location parameter", HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
        }

        return location;
    }

    /**
     * Formats latitude and longitude as a string for the API query parameter.
     *
     * @param latitude The latitude in decimal degrees
     * @param longitude The longitude in decimal degrees
     * @return Formatted coordinates string
     */
    private String formatCoordinates(double latitude, double longitude) {
        // Validate coordinate range
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new CustomException(
                    "Invalid coordinates: latitude must be between -90 and 90, longitude between -180 and 180",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDATION_ERROR);
        }

        return String.format("%f,%f", latitude, longitude);
    }
}
