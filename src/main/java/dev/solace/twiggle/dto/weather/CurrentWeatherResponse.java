package dev.solace.twiggle.dto.weather;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object representing current weather conditions from World Weather
 * Online API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentWeatherResponse {
    private String observationTime;
    private String tempC;
    private String tempF;
    private String weatherCode;
    private List<WeatherIconUrl> weatherIconUrl;
    private List<WeatherDesc> weatherDesc;
    private String windspeedMiles;
    private String windspeedKmph;
    private String winddirDegree;
    private String winddir16Point;
    private String precipMM;
    private String precipInches;
    private String humidity;
    private String visibility;
    private String visibilityMiles;
    private String pressure;
    private String pressureInches;
    private String cloudcover;
    private String feelsLikeC;
    private String feelsLikeF;
    private String uvIndex;
    private WeatherDataPoint.AirQualityData airQuality;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherIconUrl {
        private String value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherDesc {
        private String value;
    }
}
