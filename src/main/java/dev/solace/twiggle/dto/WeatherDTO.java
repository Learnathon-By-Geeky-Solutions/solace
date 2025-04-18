package dev.solace.twiggle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class for weather data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDTO {
    private String location;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime timestamp;

    private Double temperature;
    private String temperatureUnit;
    private Double humidity;
    private Double windSpeed;
    private String windSpeedUnit;
    private String windDirection;

    private Integer cloudCover;
    private String cloudType;
    private Double precipitation;
    private String precipitationType;

    private Double uvIndex;
    private String airQualityIndex;
    private List<String> airHazards;
    private List<String> plantHazards;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastItem {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        private LocalDateTime forecastTime;

        private Double temperature;
        private Double humidity;
        private Integer cloudCover;
        private Double precipitation;
        private String conditions;
        private List<String> alerts;
    }

    private List<ForecastItem> forecast;

    private String weatherAlert;
    private String gardeningAdvice;
}
