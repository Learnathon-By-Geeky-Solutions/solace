package dev.solace.twiggle.model.weather.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Top-level response object from the World Weather Online API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private WeatherData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherData {
        @JsonProperty("current_condition")
        private List<CurrentCondition> currentCondition;

        @JsonProperty("weather")
        private List<WeatherDay> weather;

        @JsonProperty("nearest_area")
        private List<NearestArea> nearestArea;

        private Alerts alerts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alerts {
        private List<Alert> alert;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alert {
        private String headline;
        // Add more fields as needed
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NearestArea {
        @JsonProperty("areaName")
        private List<NameValue> areaName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NameValue {
        private String value;
    }
}
