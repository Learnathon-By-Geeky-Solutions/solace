package dev.solace.twiggle.service.impl;

import dev.solace.twiggle.config.WeatherThresholds;
import dev.solace.twiggle.dto.WeatherDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Provides gardening advice based on weather conditions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GardeningAdviceAdvisor {

    private final WeatherThresholds thresholds;

    /**
     * Add gardening advice to the weather data
     *
     * @param weather The weather data to enrich with gardening advice
     */
    public void enrich(WeatherDTO weather) {
        if (weather.getHumidity() > thresholds.getHighHumidityAdvice()) {
            weather.setGardeningAdvice("High humidity may promote fungal growth. Consider fungicide application.");
        } else if (weather.getTemperature() > thresholds.getHighTemperatureAdvice()) {
            weather.setGardeningAdvice("High temperatures expected. Ensure plants are well watered.");
        } else if (weather.getPrecipitation() > thresholds.getHeavyRainAdvice()) {
            weather.setGardeningAdvice("Heavy rain expected. Check drainage systems and protect sensitive plants.");
        } else {
            weather.setGardeningAdvice("Weather conditions are favorable for gardening activities.");
        }
    }
}
