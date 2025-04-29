package dev.solace.twiggle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for weather thresholds.
 * These values can be overridden in application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "weather.thresholds")
@Data
public class WeatherThresholds {
    // Number of days for garden weather forecast
    private int gardenWeatherForecastDays = 3;

    // Air quality thresholds
    private double pm25Threshold = 35.0;
    private double pm10Threshold = 150.0;
    private double ozoneThreshold = 100.0;
    private double no2Threshold = 100.0;

    // Plant hazard thresholds
    private double heatStressTemperature = 28.0;
    private double frostRiskTemperature = 5.0;
    private double highHumidity = 85.0;
    private double highUvIndex = 7.0;
    private double strongWindSpeed = 20.0;
    private double heavyRainPrecipitation = 15.0;

    // Gardening advice thresholds
    private double highHumidityAdvice = 80.0;
    private double highTemperatureAdvice = 30.0;
    private double heavyRainAdvice = 10.0;

    // Temperature category thresholds
    private double coldTemperature = 15.0;
    private double idealTemperatureMax = 32.0;
    private double highHeatTemperature = 36.0;

    // Humidity category thresholds
    private double veryDryHumidity = 30.0;
    private double comfortableHumidityMax = 70.0;

    // UV index category thresholds
    private double lowUvIndex = 2.0;
    private double moderateUvIndex = 5.0;
}
