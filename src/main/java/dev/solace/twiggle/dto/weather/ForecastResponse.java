package dev.solace.twiggle.dto.weather;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object representing the complete weather data from World Weather Online API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForecastResponse {
    private DataBlock data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataBlock {
        private List<Request> request;
        private List<CurrentWeatherResponse> current_condition;
        private List<WeatherDataPoint> weather;
        private AlertBlock alerts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String type;
        private String query;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertBlock {
        private List<Alert> alert;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alert {
        private String headline;
        // Add more fields as needed for alerts
    }
}
