package dev.solace.twiggle.model.weather.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WeatherDay data from World Weather Online API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDay {
    private String date;

    @JsonProperty("hourly")
    private List<HourlyForecast> hourly;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyForecast {
        private String time;

        @JsonProperty("tempC")
        private String tempC;

        private String humidity;

        private String cloudcover;

        private String precipMM;

        @JsonProperty("weatherDesc")
        private List<ApiResponse.NameValue> weatherDesc;
    }
}
