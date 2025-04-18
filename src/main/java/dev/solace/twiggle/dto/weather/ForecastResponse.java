package dev.solace.twiggle.dto.weather;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object containing a list of weather forecast data points.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForecastResponse {
    private List<WeatherDataPoint> forecast;
}
