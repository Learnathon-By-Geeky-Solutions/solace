package dev.solace.twiggle.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.WeatherDTO;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.WeatherService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Implementation of WeatherService that provides weather data using the World Weather Online API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    private static final String FORMAT_PATTERN = "%f,%f";
    private static final String VALUE_KEY = "value";
    private static final String NEAREST_AREA_KEY = "nearest_area";
    private static final String AREA_NAME_KEY = "areaName";
    private static final String MODERATE_QUALITY = "Moderate";
    private static final String CLOUD_COVER_KEY = "cloudcover";

    private final WorldWeatherOnlineApiClient weatherApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public WeatherDTO getCurrentWeather(String location) {
        log.info("Fetching current weather for location: {}", location);
        try {
            String apiResponse = weatherApiClient.getCurrentWeather(location);
            return parseWeatherResponse(apiResponse, 1, location);
        } catch (Exception e) {
            log.error("Error fetching current weather for location {}: {}", location, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve current weather data",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    @Override
    public WeatherDTO getCurrentWeatherByCoordinates(double latitude, double longitude) {
        log.info("Fetching current weather for coordinates: {}, {}", latitude, longitude);
        try {
            String apiResponse = weatherApiClient.getCurrentWeatherByCoordinates(latitude, longitude);
            return parseWeatherResponse(apiResponse, 1, String.format(FORMAT_PATTERN, latitude, longitude));
        } catch (Exception e) {
            log.error(
                    "Error fetching current weather for coordinates {}, {}: {}",
                    latitude,
                    longitude,
                    e.getMessage(),
                    e);
            throw new CustomException(
                    "Failed to retrieve current weather data",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    @Override
    public WeatherDTO getWeatherForecast(String location, int days) {
        log.info("Fetching weather forecast for location: {} for {} days", location, days);
        try {
            String apiResponse = weatherApiClient.getWeatherForecast(location, days);
            return parseWeatherResponse(apiResponse, days, location);
        } catch (Exception e) {
            log.error("Error fetching weather forecast for location {}: {}", location, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve weather forecast data",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    @Override
    public WeatherDTO getWeatherForecastByCoordinates(double latitude, double longitude, int days) {
        log.info("Fetching weather forecast for coordinates: {}, {} for {} days", latitude, longitude, days);
        try {
            String apiResponse = weatherApiClient.getWeatherForecastByCoordinates(latitude, longitude, days);
            return parseWeatherResponse(apiResponse, days, String.format(FORMAT_PATTERN, latitude, longitude));
        } catch (Exception e) {
            log.error(
                    "Error fetching weather forecast for coordinates {}, {}: {}",
                    latitude,
                    longitude,
                    e.getMessage(),
                    e);
            throw new CustomException(
                    "Failed to retrieve weather forecast data",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    @Override
    public WeatherDTO getGardenWeather(String location, Optional<String> gardenPlanId) {
        log.info(
                "Fetching garden weather for location: {}, garden plan ID: {}",
                location,
                gardenPlanId.orElse("not provided"));

        try {
            // Use 3-day forecast for garden weather
            String apiResponse = weatherApiClient.getWeatherForecast(location, 3);
            WeatherDTO weather = parseWeatherResponse(apiResponse, 3, location);

            // Add garden-specific advice based on weather conditions
            if (weather.getHumidity() > 80) {
                weather.setGardeningAdvice("High humidity may promote fungal growth. Consider fungicide application.");
            } else if (weather.getTemperature() > 30) {
                weather.setGardeningAdvice("High temperatures expected. Ensure plants are well watered.");
            } else if (weather.getPrecipitation() > 10) {
                weather.setGardeningAdvice("Heavy rain expected. Check drainage systems and protect sensitive plants.");
            } else {
                weather.setGardeningAdvice("Weather conditions are favorable for gardening activities.");
            }

            // Plant hazards are already set in parseWeatherResponse

            return weather;
        } catch (Exception e) {
            log.error("Error fetching garden weather for location {}: {}", location, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve garden weather data",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    @Override
    public WeatherDTO getGardenWeatherByCoordinates(double latitude, double longitude, Optional<String> gardenPlanId) {
        log.info(
                "Fetching garden weather for coordinates: {}, {}, garden plan ID: {}",
                latitude,
                longitude,
                gardenPlanId.orElse("not provided"));

        try {
            // Use 3-day forecast for garden weather
            String apiResponse = weatherApiClient.getWeatherForecastByCoordinates(latitude, longitude, 3);
            WeatherDTO weather =
                    parseWeatherResponse(apiResponse, 3, String.format(FORMAT_PATTERN, latitude, longitude));

            // Add garden-specific advice based on weather conditions
            if (weather.getHumidity() > 80) {
                weather.setGardeningAdvice("High humidity may promote fungal growth. Consider fungicide application.");
            } else if (weather.getTemperature() > 30) {
                weather.setGardeningAdvice("High temperatures expected. Ensure plants are well watered.");
            } else if (weather.getPrecipitation() > 10) {
                weather.setGardeningAdvice("Heavy rain expected. Check drainage systems and protect sensitive plants.");
            } else {
                weather.setGardeningAdvice("Weather conditions are favorable for gardening activities.");
            }

            // Plant hazards are already set in parseWeatherResponse

            return weather;
        } catch (Exception e) {
            log.error(
                    "Error fetching garden weather for coordinates {}, {}: {}", latitude, longitude, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve garden weather data",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    /**
     * Parse the JSON response from the World Weather Online API into a WeatherDTO object.
     *
     * @param apiResponse The JSON response from the API
     * @param days The number of forecast days
     * @param location The location string used in the request
     * @return A WeatherDTO object
     */
    private WeatherDTO parseWeatherResponse(String apiResponse, int days, String location) {
        try {
            JsonNode root = objectMapper.readTree(apiResponse);
            JsonNode data = root.path("data");
            JsonNode currentCondition = data.path("current_condition").get(0);

            WeatherDTO.WeatherDTOBuilder builder = WeatherDTO.builder()
                    .location(extractLocationName(data, location))
                    .timestamp(LocalDateTime.now());

            // Parse current conditions
            parseCurrentConditions(currentCondition, builder);

            // Parse air quality
            parseAirQuality(currentCondition, builder);

            // Parse forecast
            List<WeatherDTO.ForecastItem> forecastItems = parseForecast(data, days);
            builder.forecast(forecastItems);

            // Parse weather alert
            String weatherAlert = parseWeatherAlert(data);
            builder.weatherAlert(weatherAlert);

            // Build the weather DTO
            WeatherDTO weatherDTO = builder.build();

            // Generate and add plant hazards for all weather endpoints
            weatherDTO.setPlantHazards(generatePlantHazards(weatherDTO));

            return weatherDTO;
        } catch (Exception e) {
            log.error("Error parsing weather API response: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to parse weather data", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    private void parseCurrentConditions(JsonNode currentCondition, WeatherDTO.WeatherDTOBuilder builder) {
        builder.temperature(currentCondition.path("temp_C").asDouble())
                .temperatureUnit("Celsius")
                .humidity(currentCondition.path("humidity").asDouble())
                .windSpeed(currentCondition.path("windspeedKmph").asDouble())
                .windSpeedUnit("km/h")
                .windDirection(currentCondition.path("winddir16Point").asText())
                .cloudCover(currentCondition.path(CLOUD_COVER_KEY).asInt())
                .precipitation(currentCondition.path("precipMM").asDouble())
                .uvIndex(
                        currentCondition.has("uvIndex")
                                ? currentCondition.path("uvIndex").asDouble()
                                : 0);

        int cloudCover = currentCondition.path(CLOUD_COVER_KEY).asInt();
        double temperature = currentCondition.path("temp_C").asDouble();

        builder.cloudType(getCloudType(cloudCover)).precipitationType(getPrecipitationType(temperature));
    }

    private void parseAirQuality(JsonNode currentCondition, WeatherDTO.WeatherDTOBuilder builder) {
        String airQuality = "Good"; // Default value
        List<String> airHazards = new ArrayList<>();

        if (currentCondition.has("air_quality")) {
            JsonNode airQualityNode = currentCondition.path("air_quality");
            int epaIndex = airQualityNode.path("us-epa-index").asInt();
            airQuality = getAirQualityFromEpaIndex(epaIndex);
            airHazards = getAirHazardsFromAirQuality(airQuality, airQualityNode);
        }

        builder.airQualityIndex(airQuality)
                .airHazards(airHazards)
                .plantHazards(new ArrayList<>()); // Initialize with empty list, will be populated later
    }

    private List<WeatherDTO.ForecastItem> parseForecast(JsonNode data, int days) {
        List<WeatherDTO.ForecastItem> forecastItems = new ArrayList<>();
        JsonNode weatherArray = data.path("weather");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < Math.min(days, weatherArray.size()); i++) {
            JsonNode dayForecast = weatherArray.get(i);
            String dateStr = dayForecast.path("date").asText();
            parseHourlyForecasts(dayForecast, dateStr, dateFormatter, data, forecastItems);
        }

        return forecastItems;
    }

    private void parseHourlyForecasts(
            JsonNode dayForecast,
            String dateStr,
            DateTimeFormatter dateFormatter,
            JsonNode data,
            List<WeatherDTO.ForecastItem> forecastItems) {
        JsonNode hourlyArray = dayForecast.path("hourly");
        for (JsonNode hourly : hourlyArray) {
            String timeStr = hourly.path("time").asText();
            int hour = Integer.parseInt(timeStr) / 100;
            LocalDateTime forecastTime = LocalDate.parse(dateStr, dateFormatter).atTime(hour, 0);

            WeatherDTO.ForecastItem item = WeatherDTO.ForecastItem.builder()
                    .forecastTime(forecastTime)
                    .temperature(hourly.path("tempC").asDouble())
                    .humidity(hourly.path("humidity").asDouble())
                    .cloudCover(hourly.path(CLOUD_COVER_KEY).asInt())
                    .precipitation(hourly.path("precipMM").asDouble())
                    .conditions(
                            hourly.path("weatherDesc").get(0).path(VALUE_KEY).asText())
                    .alerts(parseAlerts(data))
                    .build();

            forecastItems.add(item);
        }
    }

    private List<String> parseAlerts(JsonNode data) {
        List<String> alerts = new ArrayList<>();
        JsonNode alertsNode = data.path("alerts").path("alert");
        if (alertsNode.isArray() && alertsNode.size() > 0) {
            for (JsonNode alert : alertsNode) {
                alerts.add(alert.path("headline").asText());
            }
        }
        return alerts;
    }

    private String parseWeatherAlert(JsonNode data) {
        JsonNode alertsNode = data.path("alerts").path("alert");
        if (alertsNode.isArray() && alertsNode.size() > 0) {
            return alertsNode.get(0).path("headline").asText();
        }
        return null;
    }

    private String extractLocationName(JsonNode data, String defaultLocation) {
        if (data.has(NEAREST_AREA_KEY) && data.path(NEAREST_AREA_KEY).size() > 0) {
            JsonNode nearestArea = data.path(NEAREST_AREA_KEY).get(0);
            if (nearestArea.has(AREA_NAME_KEY)
                    && nearestArea.path(AREA_NAME_KEY).size() > 0) {
                return nearestArea.path(AREA_NAME_KEY).get(0).path(VALUE_KEY).asText();
            }
        }
        return defaultLocation;
    }

    private String getAirQualityFromEpaIndex(int epaIndex) {
        switch (epaIndex) {
            case 1:
                return "Good";
            case 2:
                return MODERATE_QUALITY;
            case 3:
                return "Unhealthy for Sensitive Groups";
            case 4:
                return "Unhealthy";
            case 5:
                return "Very Unhealthy";
            case 6:
                return "Hazardous";
            default:
                log.warn("Unknown EPA index value: {}, defaulting to Moderate", epaIndex);
                return MODERATE_QUALITY;
        }
    }

    private List<String> getAirHazardsFromAirQuality(String airQuality, JsonNode airQualityNode) {
        List<String> hazards = new ArrayList<>();

        // Add general description based on air quality level
        switch (airQuality) {
            case "Good":
                return hazards; // No hazards for good air quality
            case MODERATE_QUALITY:
                hazards.add("Mild pollen and low-level particulates");
                break;
            case "Unhealthy for Sensitive Groups":
                hazards.add("May cause respiratory symptoms in sensitive individuals");
                break;
            case "Unhealthy":
                hazards.add("Increased likelihood of adverse respiratory effects in general population");
                break;
            case "Very Unhealthy":
                hazards.add("Significant respiratory effects can be expected in general population");
                break;
            case "Hazardous":
                hazards.add("Serious respiratory effects and health impacts for all");
                break;
            default:
                log.warn("Unknown air quality value: {}, no specific hazards will be added", airQuality);
                break;
        }

        // Add specific pollutant hazards if high levels
        if (airQualityNode.has("pm2_5") && airQualityNode.path("pm2_5").asDouble() > 35) {
            hazards.add("High PM2.5 (fine particulate matter) levels");
        }
        if (airQualityNode.has("pm10") && airQualityNode.path("pm10").asDouble() > 150) {
            hazards.add("High PM10 (coarse particulate matter) levels");
        }
        if (airQualityNode.has("o3") && airQualityNode.path("o3").asDouble() > 100) {
            hazards.add("High ozone levels");
        }
        if (airQualityNode.has("no2") && airQualityNode.path("no2").asDouble() > 100) {
            hazards.add("High nitrogen dioxide levels");
        }

        return hazards;
    }

    private String getCloudType(int cloudCover) {
        if (cloudCover < 20) {
            return "Clear";
        } else if (cloudCover < 50) {
            return "Cumulus";
        } else if (cloudCover < 80) {
            return "Stratocumulus";
        } else {
            return "Stratus";
        }
    }

    private String getPrecipitationType(double temperature) {
        if (temperature < 0) {
            return "Snow";
        } else if (temperature < 4) {
            return "Sleet";
        } else {
            return "Rain";
        }
    }

    private List<String> generatePlantHazards(WeatherDTO weather) {
        List<String> hazards = new ArrayList<>();

        if (weather.getTemperature() > 28) {
            hazards.add("Heat stress risk for sensitive plants");
        }

        if (weather.getTemperature() < 5) {
            hazards.add("Frost risk for outdoor plants");
        }

        if (weather.getHumidity() > 85) {
            hazards.add("High humidity may increase fungal disease risk");
        }

        if (weather.getUvIndex() > 7) {
            hazards.add("High UV may cause leaf scorching on sensitive plants");
        }

        if (weather.getWindSpeed() > 20) {
            hazards.add("Strong winds may damage tall or unstaked plants");
        }

        if (weather.getPrecipitation() > 15) {
            hazards.add("Heavy rain may lead to soil erosion and waterlogging");
        }

        if (!"Good".equals(weather.getAirQualityIndex()) && !MODERATE_QUALITY.equals(weather.getAirQualityIndex())) {
            hazards.add("Poor air quality may affect sensitive plant species");
        }

        // Add plant-specific gardening tips with emojis
        double temperature = weather.getTemperature();
        double humidity = weather.getHumidity();
        double uvIndex = weather.getUvIndex();
        double precipitation = weather.getPrecipitation();
        String airQuality = weather.getAirQualityIndex();

        // Temperature Tips
        if (temperature < 15) {
            hazards.add("\u2744\ufe0f Cold stress possible. Protect delicate plants, especially young seedlings.");
        } else if (temperature <= 32) {
            hazards.add("\ud83c\udf3f Ideal temperature range for healthy plant growth.");
        } else if (temperature <= 36) {
            hazards.add("\u2600\ufe0f High heat today. Water early in the morning to prevent heat stress.");
        } else {
            hazards.add("\u26a1\ufe0f Extreme heat warning! Provide shade and monitor plants closely.");
        }

        // Humidity Tips
        if (humidity < 30) {
            hazards.add("\ud83d\udca7 Very dry conditions. Mist indoor plants and check soil moisture more often.");
        } else if (humidity <= 70) {
            hazards.add("\ud83c\udf27\ufe0f Comfortable humidity range for most plants.");
        } else {
            hazards.add("\ud83d\udca7 High humidity detected. Watch for fungal diseases and avoid overhead watering.");
        }

        // UV Index Tips
        if (uvIndex <= 2) {
            hazards.add("\ud83c\udf1e Low UV exposure. Good for all outdoor plants.");
        } else if (uvIndex <= 5) {
            hazards.add("\u26a1\ufe0f Moderate UV levels. Shade delicate plants if possible.");
        } else if (uvIndex <= 7) {
            hazards.add("\ud83d\udd25 High UV levels. Protect sensitive plants during peak hours.");
        } else {
            hazards.add("\ud83c\udf1e Very high UV! Ensure shade for vulnerable plants and avoid midday gardening.");
        }

        // Precipitation Tips
        if (precipitation == 0.0) {
            hazards.add(
                    "\ud83d\udca7 No rain today. Ensure manual watering, especially rooftop and container gardens.");
        } else {
            hazards.add("\ud83c\udf27\ufe0f Some rain expected. Check drainage to avoid waterlogged soil.");
        }

        // Air Quality Tips
        int airQualityIndex = getAirQualityIndex(airQuality);
        if (airQualityIndex <= 2) {
            hazards.add("\ud83c\udf0d Air quality is good. Great day for outdoor gardening!");
        } else if (airQualityIndex == 3) {
            hazards.add("\ud83c\udf0d Moderate air quality. Sensitive individuals should take light precautions.");
        } else if (airQualityIndex == 4) {
            hazards.add("\ud83d\udeab Air quality is unhealthy for sensitive groups. Limit heavy outdoor gardening.");
        } else {
            hazards.add("\u26a1\ufe0f Very unhealthy air quality. Prefer indoor gardening activities today.");
        }

        // Plant-Specific Suggestions
        hazards.add("\ud83c\udf35 **Succulents**: Thriving in sunny, dry weather. Minimal watering needed.");
        hazards.add("\ud83c\udf3a **Flowering Plants**: Great time to deadhead and fertilize to encourage blooms.");
        hazards.add("\ud83c\udf45 **Vegetables**: Consistent watering critical. Monitor for heat or pest stress.");
        hazards.add("\ud83c\udf3f **Herbs**: Harvest early in the day for maximum flavor and aroma.");

        return hazards;
    }

    /**
     * Converts air quality description to a numerical index similar to EPA index.
     *
     * @param airQuality The air quality description
     * @return A numerical index (1-6)
     */
    private int getAirQualityIndex(String airQuality) {
        if ("Good".equals(airQuality)) {
            return 1;
        } else if (MODERATE_QUALITY.equals(airQuality)) {
            return 2;
        } else if ("Unhealthy for Sensitive Groups".equals(airQuality)) {
            return 3;
        } else if ("Unhealthy".equals(airQuality)) {
            return 4;
        } else if ("Very Unhealthy".equals(airQuality)) {
            return 5;
        } else if ("Hazardous".equals(airQuality)) {
            return 6;
        } else {
            return 2; // Default to moderate
        }
    }
}
