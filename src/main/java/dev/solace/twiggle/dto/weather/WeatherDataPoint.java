package dev.solace.twiggle.dto.weather;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single day's weather forecast data point.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDataPoint {
    private LocalDate date;
    private double temperatureHigh;
    private double temperatureLow;
    private int humidity;
    private int precipitationChance;
    private String condition;
    private String icon;
}
