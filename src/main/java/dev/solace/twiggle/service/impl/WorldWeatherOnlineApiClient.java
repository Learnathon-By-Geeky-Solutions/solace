package dev.solace.twiggle.service.impl;

import dev.solace.twiggle.config.WeatherApiConfig;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import java.net.URI;
import java.util.HashMap;
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
        queryParams.put("q", location);
        queryParams.put("format", "json");
        queryParams.put("num_of_days", "1");
        queryParams.put("fx", "yes");
        queryParams.put("cc", "yes");
        queryParams.put("aqi", "yes");
        queryParams.put("alerts", "yes");

        return makeApiCall("/weather.ashx", queryParams);
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
        queryParams.put("format", "json");
        queryParams.put("num_of_days", "1");
        queryParams.put("fx", "yes");
        queryParams.put("cc", "yes");
        queryParams.put("aqi", "yes");
        queryParams.put("alerts", "yes");

        return makeApiCall("/weather.ashx", queryParams);
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
            log.warn("Invalid days parameter: {}. Using default value of 7", days);
            days = 7;
        }

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", location);
        queryParams.put("format", "json");
        queryParams.put("num_of_days", String.valueOf(days));
        queryParams.put("fx", "yes");
        queryParams.put("cc", "yes");
        queryParams.put("tp", "3"); // 3-hourly forecast
        queryParams.put("aqi", "yes");
        queryParams.put("alerts", "yes");

        return makeApiCall("/weather.ashx", queryParams);
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
            log.warn("Invalid days parameter: {}. Using default value of 7", days);
            days = 7;
        }

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", formatCoordinates(latitude, longitude));
        queryParams.put("format", "json");
        queryParams.put("num_of_days", String.valueOf(days));
        queryParams.put("fx", "yes");
        queryParams.put("cc", "yes");
        queryParams.put("tp", "3"); // 3-hourly forecast
        queryParams.put("aqi", "yes");
        queryParams.put("alerts", "yes");

        return makeApiCall("/weather.ashx", queryParams);
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

            // Convert Map to MultiValueMap
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            queryParams.forEach(multiValueMap::add);

            // Build the API URL
            URI uri = UriComponentsBuilder.fromUriString(weatherApiConfig.getBaseUrl())
                    .path(endpoint)
                    .queryParams(multiValueMap)
                    .build()
                    .toUri();

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
     * Formats latitude and longitude as a string for the API query parameter.
     *
     * @param latitude The latitude in decimal degrees
     * @param longitude The longitude in decimal degrees
     * @return Formatted coordinates string
     */
    private String formatCoordinates(double latitude, double longitude) {
        return String.format("%f,%f", latitude, longitude);
    }
}
