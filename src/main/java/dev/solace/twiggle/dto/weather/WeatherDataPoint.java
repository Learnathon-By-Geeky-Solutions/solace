package dev.solace.twiggle.dto.weather;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single day's weather forecast data point from World Weather
 * Online API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDataPoint {
    private String date;
    private List<AstronomyData> astronomy;
    private String maxtempC;
    private String maxtempF;
    private String mintempC;
    private String mintempF;
    private String avgtempC;
    private String avgtempF;
    private String totalSnowCm;
    private String sunHour;
    private String uvIndex;
    private AirQualityData airQuality;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AstronomyData {
        private String sunrise;
        private String sunset;
        private String moonrise;
        private String moonset;
        private String moonPhase;
        private String moonIllumination;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AirQualityData {
        private String co;
        private String no2;
        private String o3;
        private String so2;
        private String pm25;
        private String pm10;
        private String usEpaIndex;
        private String gbDefraIndex;
    }
}
