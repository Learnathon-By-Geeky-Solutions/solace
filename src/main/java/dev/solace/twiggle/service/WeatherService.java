package dev.solace.twiggle.service;

import dev.solace.twiggle.dto.WeatherDTO;
import java.util.Optional;

/**
 * Service interface for weather data operations.
 */
public interface WeatherService {

    /**
     * Get current weather data for a specific location.
     *
     * @param location The location (city, coordinates, etc.) to get weather for
     * @return The weather data for the specified location
     */
    WeatherDTO getCurrentWeather(String location);

    /**
     * Get current weather data for a specific latitude and longitude.
     *
     * @param latitude The latitude in decimal degrees
     * @param longitude The longitude in decimal degrees
     * @return The weather data for the specified coordinates
     */
    WeatherDTO getCurrentWeatherByCoordinates(double latitude, double longitude);

    /**
     * Get weather forecast for a specific location.
     *
     * @param location The location (city, coordinates, etc.) to get forecast for
     * @param days Number of days to forecast (1-7)
     * @return The weather forecast for the specified location and days
     */
    WeatherDTO getWeatherForecast(String location, int days);

    /**
     * Get weather forecast for a specific latitude and longitude.
     *
     * @param latitude The latitude in decimal degrees
     * @param longitude The longitude in decimal degrees
     * @param days Number of days to forecast (1-7)
     * @return The weather forecast for the specified coordinates and days
     */
    WeatherDTO getWeatherForecastByCoordinates(double latitude, double longitude, int days);

    /**
     * Get garden-specific weather information and advice.
     *
     * @param location The location (city, coordinates, etc.) of the garden
     * @param gardenPlanId Optional garden plan ID to provide plant-specific advice
     * @return Weather data with gardening-specific information and advice
     */
    WeatherDTO getGardenWeather(String location, Optional<String> gardenPlanId);

    /**
     * Get garden-specific weather information and advice for a specific latitude and longitude.
     *
     * @param latitude The latitude in decimal degrees
     * @param longitude The longitude in decimal degrees
     * @param gardenPlanId Optional garden plan ID to provide plant-specific advice
     * @return Weather data with gardening-specific information and advice for the specified coordinates
     */
    WeatherDTO getGardenWeatherByCoordinates(double latitude, double longitude, Optional<String> gardenPlanId);
}
