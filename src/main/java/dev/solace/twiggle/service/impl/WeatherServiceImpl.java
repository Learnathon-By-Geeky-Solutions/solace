package dev.solace.twiggle.service.impl;

import dev.solace.twiggle.config.WeatherThresholds;
import dev.solace.twiggle.dto.WeatherDTO;
import dev.solace.twiggle.mapper.WeatherJsonMapper;
import dev.solace.twiggle.service.WeatherService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of WeatherService that provides weather data using the World
 * Weather Online API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    private final WorldWeatherOnlineFacade facade;
    private final WeatherJsonMapper mapper;
    private final PlantHazardAdvisor plantAdvisor;
    private final GardeningAdviceAdvisor gardenAdvisor;
    private final WeatherThresholds thresholds;

    @Override
    public WeatherDTO getCurrentWeather(String location) {
        log.info("Fetching current weather for location: {}", location);
        return getWeather(location, 1);
    }

    @Override
    public WeatherDTO getCurrentWeatherByCoordinates(double latitude, double longitude) {
        log.info("Fetching current weather for coordinates: {}, {}", latitude, longitude);
        return getWeatherByCoordinates(latitude, longitude, 1);
    }

    @Override
    public WeatherDTO getWeatherForecast(String location, int days) {
        log.info("Fetching weather forecast for location: {} for {} days", location, days);
        return getWeather(location, days);
    }

    @Override
    public WeatherDTO getWeatherForecastByCoordinates(double latitude, double longitude, int days) {
        log.info("Fetching weather forecast for coordinates: {}, {} for {} days", latitude, longitude, days);
        return getWeatherByCoordinates(latitude, longitude, days);
    }

    @Override
    public WeatherDTO getGardenWeather(String location, Optional<String> gardenPlanId) {
        log.info(
                "Fetching garden weather for location: {}, garden plan ID: {}",
                location,
                gardenPlanId.orElse("not provided"));

        return getGardenWeatherImpl(location, gardenPlanId);
    }

    @Override
    public WeatherDTO getGardenWeatherByCoordinates(double latitude, double longitude, Optional<String> gardenPlanId) {
        log.info(
                "Fetching garden weather for coordinates: {}, {}, garden plan ID: {}",
                latitude,
                longitude,
                gardenPlanId.orElse("not provided"));

        return getGardenWeatherByCoordinatesImpl(latitude, longitude, gardenPlanId);
    }

    /**
     * Common implementation for getting weather data by location
     */
    private WeatherDTO getWeather(String location, int days) {
        String response = facade.fetch(location, days);
        WeatherDTO dto = mapper.toDto(response, days, location);
        dto.setPlantHazards(plantAdvisor.hazardsFor(dto));
        return dto;
    }

    /**
     * Common implementation for getting weather data by coordinates
     */
    private WeatherDTO getWeatherByCoordinates(double latitude, double longitude, int days) {
        String response = facade.fetchByCoordinates(latitude, longitude, days);
        WeatherDTO dto = mapper.toDto(response, days, String.format("%f,%f", latitude, longitude));
        dto.setPlantHazards(plantAdvisor.hazardsFor(dto));
        return dto;
    }

    /**
     * Implementation for garden weather by location
     */
    private WeatherDTO getGardenWeatherImpl(String location, Optional<String> gardenPlanId) {
        WeatherDTO dto = getWeather(location, thresholds.getGardenWeatherForecastDays());
        gardenAdvisor.enrich(dto);
        return dto;
    }

    /**
     * Implementation for garden weather by coordinates
     */
    private WeatherDTO getGardenWeatherByCoordinatesImpl(
            double latitude, double longitude, Optional<String> gardenPlanId) {
        WeatherDTO dto = getWeatherByCoordinates(latitude, longitude, thresholds.getGardenWeatherForecastDays());
        gardenAdvisor.enrich(dto);
        return dto;
    }
}
