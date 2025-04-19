package dev.solace.twiggle.dto.weather;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single day's weather forecast data point from World Weather Online API.
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
    private String totalSnow_cm;
    private String sunHour;
    private String uvIndex;
    private AirQualityData air_quality;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AstronomyData {
        private String sunrise;
        private String sunset;
        private String moonrise;
        private String moonset;
        private String moon_phase;
        private String moon_illumination;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AirQualityData {
        private String co;
        private String no2;
        private String o3;
        private String so2;
        private String pm2_5;
        private String pm10;
        private String us_epa_index;
        private String gb_defra_index;
    }
}
