package dev.solace.twiggle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Location cannot be blank")
    private String location;

    @NotNull(message = "Timestamp is required") @PastOrPresent(message = "Timestamp must be in the past or present")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime timestamp;

    @NotNull(message = "Temperature is required") private Double temperature;

    @NotBlank(message = "Temperature unit is required")
    private String temperatureUnit;

    @NotNull(message = "Humidity is required") @Min(value = 0, message = "Humidity must be between 0 and 100")
    @Max(value = 100, message = "Humidity must be between 0 and 100")
    private Double humidity;

    @NotNull(message = "Wind speed is required") @PositiveOrZero(message = "Wind speed must be a positive number or zero")
    private Double windSpeed;

    @NotBlank(message = "Wind speed unit is required")
    private String windSpeedUnit;

    private String windDirection;

    @Min(value = 0, message = "Cloud cover must be between 0 and 100")
    @Max(value = 100, message = "Cloud cover must be between 0 and 100")
    private Integer cloudCover;

    private String cloudType;

    @PositiveOrZero(message = "Precipitation must be a positive number or zero")
    private Double precipitation;

    private String precipitationType;

    @PositiveOrZero(message = "UV index must be a positive number or zero")
    @Max(value = 12, message = "UV index must be between 0 and 12")
    private Double uvIndex;

    private String airQualityIndex;

    @Size(max = 10, message = "Too many air hazards listed")
    private List<String> airHazards;

    @Size(max = 10, message = "Too many plant hazards listed")
    private List<String> plantHazards;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastItem {
        @NotNull(message = "Forecast time is required") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        private LocalDateTime forecastTime;

        @NotNull(message = "Temperature is required") private Double temperature;

        @Min(value = 0, message = "Humidity must be between 0 and 100")
        @Max(value = 100, message = "Humidity must be between 0 and 100")
        private Double humidity;

        @Min(value = 0, message = "Cloud cover must be between 0 and 100")
        @Max(value = 100, message = "Cloud cover must be between 0 and 100")
        private Integer cloudCover;

        @PositiveOrZero(message = "Precipitation must be a positive number or zero")
        private Double precipitation;

        private String conditions;

        @Size(max = 5, message = "Too many alerts listed")
        private List<String> alerts;
    }

    @Valid
    private List<ForecastItem> forecast;

    @Size(max = 500, message = "Weather alert message is too long")
    private String weatherAlert;

    @Size(max = 1000, message = "Gardening advice is too long")
    private String gardeningAdvice;
}
