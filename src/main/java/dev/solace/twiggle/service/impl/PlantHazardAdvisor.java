package dev.solace.twiggle.service.impl;

import dev.solace.twiggle.config.WeatherThresholds;
import dev.solace.twiggle.dto.WeatherDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Generates plant hazard information based on weather conditions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PlantHazardAdvisor {

    private final WeatherThresholds thresholds;

    /**
     * Rule definition for plant hazards
     */
    private static class Rule {
        private final Predicate<WeatherDTO> condition;
        private final String message;

        private Rule(Predicate<WeatherDTO> condition, String message) {
            this.condition = condition;
            this.message = message;
        }

        public static Rule when(Predicate<WeatherDTO> condition, String message) {
            return new Rule(condition, message);
        }

        public boolean test(WeatherDTO weather) {
            return condition.test(weather);
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Generate plant hazards based on weather conditions
     *
     * @param weather The weather data
     * @return A list of plant hazard messages
     */
    public List<String> hazardsFor(WeatherDTO weather) {
        List<String> hazards = new ArrayList<>();

        // Add basic weather hazards using rule engine
        List<Rule> rules = buildRules();
        rules.stream().filter(rule -> rule.test(weather)).map(Rule::getMessage).forEach(hazards::add);

        // Add tips for different categories
        addTemperatureTips(weather.getTemperature(), hazards);
        addHumidityTips(weather.getHumidity(), hazards);
        addUvIndexTips(weather.getUvIndex(), hazards);
        addPrecipitationTips(weather.getPrecipitation(), hazards);
        addAirQualityTips(weather.getAirQualityIndex(), hazards);
        addPlantSpecificSuggestions(hazards);

        return hazards;
    }

    private List<Rule> buildRules() {
        List<Rule> rules = new ArrayList<>();

        // Basic weather hazards
        rules.add(Rule.when(
                w -> w.getTemperature() > thresholds.getHeatStressTemperature(),
                "Heat stress risk for sensitive plants"));

        rules.add(Rule.when(
                w -> w.getTemperature() < thresholds.getFrostRiskTemperature(), "Frost risk for outdoor plants"));

        rules.add(Rule.when(
                w -> w.getHumidity() > thresholds.getHighHumidity(), "High humidity may increase fungal disease risk"));

        rules.add(Rule.when(
                w -> w.getUvIndex() > thresholds.getHighUvIndex(),
                "High UV may cause leaf scorching on sensitive plants"));

        rules.add(Rule.when(
                w -> w.getWindSpeed() > thresholds.getStrongWindSpeed(),
                "Strong winds may damage tall or unstaked plants"));

        rules.add(Rule.when(
                w -> w.getPrecipitation() > thresholds.getHeavyRainPrecipitation(),
                "Heavy rain may lead to soil erosion and waterlogging"));

        rules.add(Rule.when(
                w -> !isGoodOrModerateAirQuality(w.getAirQualityIndex()),
                "Poor air quality may affect sensitive plant species"));

        return rules;
    }

    private boolean isGoodOrModerateAirQuality(String airQualityIndex) {
        return "Good".equals(airQualityIndex) || "Moderate".equals(airQualityIndex);
    }

    private void addTemperatureTips(double temperature, List<String> hazards) {
        if (temperature < thresholds.getColdTemperature()) {
            hazards.add("\u2744\ufe0f Cold stress possible. Protect delicate plants, especially young seedlings.");
        } else if (temperature <= thresholds.getIdealTemperatureMax()) {
            hazards.add("\ud83c\udf3f Ideal temperature range for healthy plant growth.");
        } else if (temperature <= thresholds.getHighHeatTemperature()) {
            hazards.add("\u2600\ufe0f High heat today. Water early in the morning to prevent heat stress.");
        } else {
            hazards.add("\u26a1\ufe0f Extreme heat warning! Provide shade and monitor plants closely.");
        }
    }

    private void addHumidityTips(double humidity, List<String> hazards) {
        if (humidity < thresholds.getVeryDryHumidity()) {
            hazards.add("\ud83d\udca7 Very dry conditions. Mist indoor plants and check soil moisture more often.");
        } else if (humidity <= thresholds.getComfortableHumidityMax()) {
            hazards.add("\ud83c\udf27\ufe0f Comfortable humidity range for most plants.");
        } else {
            hazards.add("\ud83d\udca7 High humidity detected. Watch for fungal diseases and avoid overhead watering.");
        }
    }

    private void addUvIndexTips(double uvIndex, List<String> hazards) {
        if (uvIndex <= thresholds.getLowUvIndex()) {
            hazards.add("\ud83c\udf1e Low UV exposure. Good for all outdoor plants.");
        } else if (uvIndex <= thresholds.getModerateUvIndex()) {
            hazards.add("\u26a1\ufe0f Moderate UV levels. Shade delicate plants if possible.");
        } else if (uvIndex <= thresholds.getHighUvIndex()) {
            hazards.add("\ud83d\udd25 High UV levels. Protect sensitive plants during peak hours.");
        } else {
            hazards.add("\ud83c\udf1e Very high UV! Ensure shade for vulnerable plants and avoid midday gardening.");
        }
    }

    private void addPrecipitationTips(double precipitation, List<String> hazards) {
        if (precipitation == 0.0) {
            hazards.add(
                    "\ud83d\udca7 No rain today. Ensure manual watering, especially rooftop and container gardens.");
        } else {
            hazards.add("\ud83c\udf27\ufe0f Some rain expected. Check drainage to avoid waterlogged soil.");
        }
    }

    private void addAirQualityTips(String airQualityIndex, List<String> hazards) {
        int airQualityLevel = getAirQualityIndex(airQualityIndex);

        if (airQualityLevel <= 2) {
            hazards.add("\ud83c\udf0d Air quality is good. Great day for outdoor gardening!");
        } else if (airQualityLevel == 3) {
            hazards.add("\ud83c\udf0d Moderate air quality. Sensitive individuals should take light precautions.");
        } else if (airQualityLevel == 4) {
            hazards.add("\ud83d\udeab Air quality is unhealthy for sensitive groups. Limit heavy outdoor gardening.");
        } else {
            hazards.add("\u26a1\ufe0f Very unhealthy air quality. Prefer indoor gardening activities today.");
        }
    }

    private void addPlantSpecificSuggestions(List<String> hazards) {
        hazards.add("\ud83c\udf35 Succulents: Thriving in sunny, dry weather. Minimal watering needed.");
        hazards.add("\ud83c\udf3a Flowering Plants: Great time to deadhead and fertilize to encourage blooms.");
        hazards.add("\ud83c\udf45 Vegetables: Consistent watering critical. Monitor for heat or pest stress.");
        hazards.add("\ud83c\udf3f Herbs: Harvest early in the day for maximum flavor and aroma.");
    }

    /**
     * Maps air quality description to a numerical index similar to EPA index.
     */
    private int getAirQualityIndex(String airQualityDescription) {
        switch (airQualityDescription) {
            case "Good":
                return 1;
            case "Moderate":
                return 2;
            case "Unhealthy for Sensitive Groups":
                return 3;
            case "Unhealthy":
                return 4;
            case "Very Unhealthy":
                return 5;
            case "Hazardous":
                return 6;
            default:
                return 2; // Default to moderate
        }
    }
}
