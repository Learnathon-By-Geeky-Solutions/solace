package dev.solace.twiggle.dto.weather;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object representing current weather conditions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentWeatherResponse {
    private double temperature;
    private int humidity;
    private double windSpeed;
    private double precipitation;
    private String condition;
    private String icon;
}
