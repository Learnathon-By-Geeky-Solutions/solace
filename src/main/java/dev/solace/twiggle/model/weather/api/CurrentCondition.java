package dev.solace.twiggle.model.weather.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CurrentCondition data from World Weather Online API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentCondition {
    @JsonProperty("temp_C")
    private String tempC;

    private String humidity;

    private String windspeedKmph;

    private String winddir16Point;

    private String cloudcover;

    private String precipMM;

    private String uvIndex;

    @JsonProperty("air_quality")
    private AirQuality airQuality;

    @JsonProperty("weatherDesc")
    private List<ApiResponse.NameValue> weatherDesc;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AirQuality {
        private String co;
        private String no2;
        private String o3;
        private String so2;
        private String pm2_5;
        private String pm10;

        @JsonProperty("us-epa-index")
        private String usEpaIndex;
    }
}
