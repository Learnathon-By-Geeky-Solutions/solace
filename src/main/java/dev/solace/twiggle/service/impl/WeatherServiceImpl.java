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
            return parseWeatherResponse(apiResponse, 1, String.format("%f,%f", latitude, longitude));
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
            return parseWeatherResponse(apiResponse, days, String.format("%f,%f", latitude, longitude));
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

            // Add plant-specific hazards
            weather.setPlantHazards(generatePlantHazards(weather));

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
            WeatherDTO weather = parseWeatherResponse(apiResponse, 3, String.format("%f,%f", latitude, longitude));

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

            // Add plant-specific hazards
            weather.setPlantHazards(generatePlantHazards(weather));

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

            // Get current conditions
            JsonNode currentCondition = data.path("current_condition").get(0);

            // Extract temperature and other current weather details
            double tempC = currentCondition.path("temp_C").asDouble();
            double humidity = currentCondition.path("humidity").asDouble();
            double windSpeed = currentCondition.path("windspeedKmph").asDouble();
            String windDirection = currentCondition.path("winddir16Point").asText();
            int cloudCover = currentCondition.path("cloudcover").asInt();
            double precipitation = currentCondition.path("precipMM").asDouble();

            // Extract air quality if available
            String airQuality = "Good"; // Default value
            List<String> airHazards = new ArrayList<>();
            if (currentCondition.has("air_quality")) {
                JsonNode airQualityNode = currentCondition.path("air_quality");
                int epaIndex = airQualityNode.path("us-epa-index").asInt();
                airQuality = getAirQualityFromEpaIndex(epaIndex);
                airHazards = getAirHazardsFromAirQuality(airQuality, airQualityNode);
            }

            // Get weather description
            String weatherDesc =
                    currentCondition.path("weatherDesc").get(0).path("value").asText();

            // Build forecast items
            List<WeatherDTO.ForecastItem> forecastItems = new ArrayList<>();
            JsonNode weatherArray = data.path("weather");

            // Format for parsing date
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (int i = 0; i < Math.min(days, weatherArray.size()); i++) {
                JsonNode dayForecast = weatherArray.get(i);
                String dateStr = dayForecast.path("date").asText();

                // Parse hourly forecasts (every 3 hours)
                JsonNode hourlyArray = dayForecast.path("hourly");
                for (JsonNode hourly : hourlyArray) {
                    // Parse time (format is in hmm, e.g., 0 for midnight, 300 for 3 AM, etc.)
                    String timeStr = hourly.path("time").asText();
                    int hour = Integer.parseInt(timeStr) / 100;

                    // Create forecast time - FIX: Parse to LocalDate first, then convert to LocalDateTime
                    LocalDateTime forecastTime =
                            LocalDate.parse(dateStr, dateFormatter).atTime(hour, 0);

                    // Parse weather data
                    double forecastTemp = hourly.path("tempC").asDouble();
                    double forecastHumidity = hourly.path("humidity").asDouble();
                    int forecastCloudCover = hourly.path("cloudcover").asInt();
                    double forecastPrecip = hourly.path("precipMM").asDouble();
                    String forecastCondition =
                            hourly.path("weatherDesc").get(0).path("value").asText();

                    // Get any weather alerts
                    List<String> alerts = new ArrayList<>();
                    JsonNode alertsNode = data.path("alerts").path("alert");
                    if (alertsNode.isArray() && alertsNode.size() > 0) {
                        for (JsonNode alert : alertsNode) {
                            alerts.add(alert.path("headline").asText());
                        }
                    }

                    // Create and add forecast item
                    WeatherDTO.ForecastItem item = WeatherDTO.ForecastItem.builder()
                            .forecastTime(forecastTime)
                            .temperature(forecastTemp)
                            .humidity(forecastHumidity)
                            .cloudCover(forecastCloudCover)
                            .precipitation(forecastPrecip)
                            .conditions(forecastCondition)
                            .alerts(alerts)
                            .build();

                    forecastItems.add(item);
                }
            }

            // Get weather alert if available
            String weatherAlert = null;
            JsonNode alertsNode = data.path("alerts").path("alert");
            if (alertsNode.isArray() && alertsNode.size() > 0) {
                weatherAlert = alertsNode.get(0).path("headline").asText();
            }

            // Extract UV index
            double uvIndex = 0;
            if (currentCondition.has("uvIndex")) {
                uvIndex = currentCondition.path("uvIndex").asDouble();
            }

            // Get nearest area name
            String locationName = location;
            if (data.has("nearest_area") && data.path("nearest_area").size() > 0) {
                JsonNode nearestArea = data.path("nearest_area").get(0);
                if (nearestArea.has("areaName") && nearestArea.path("areaName").size() > 0) {
                    locationName =
                            nearestArea.path("areaName").get(0).path("value").asText();
                }
            }

            // Build and return the weather DTO
            return WeatherDTO.builder()
                    .location(locationName)
                    .timestamp(LocalDateTime.now())
                    .temperature(tempC)
                    .temperatureUnit("Celsius")
                    .humidity(humidity)
                    .windSpeed(windSpeed)
                    .windSpeedUnit("km/h")
                    .windDirection(windDirection)
                    .cloudCover(cloudCover)
                    .cloudType(getCloudType(cloudCover))
                    .precipitation(precipitation)
                    .precipitationType(getPrecipitationType(tempC))
                    .uvIndex(uvIndex)
                    .airQualityIndex(airQuality)
                    .airHazards(airHazards)
                    .plantHazards(new ArrayList<>()) // Will be populated in getGardenWeather
                    .forecast(forecastItems)
                    .weatherAlert(weatherAlert)
                    .build();
        } catch (Exception e) {
            log.error("Error parsing weather API response: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to parse weather data", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    private String getAirQualityFromEpaIndex(int epaIndex) {
        switch (epaIndex) {
            case 1:
                return "Good";
            case 2:
                return "Moderate";
            case 3:
                return "Unhealthy for Sensitive Groups";
            case 4:
                return "Unhealthy";
            case 5:
                return "Very Unhealthy";
            case 6:
                return "Hazardous";
            default:
                return "Unknown";
        }
    }

    private List<String> getAirHazardsFromAirQuality(String airQuality, JsonNode airQualityNode) {
        List<String> hazards = new ArrayList<>();

        // Add general description based on air quality level
        switch (airQuality) {
            case "Good":
                return hazards; // No hazards for good air quality
            case "Moderate":
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

        if (!"Good".equals(weather.getAirQualityIndex()) && !"Moderate".equals(weather.getAirQualityIndex())) {
            hazards.add("Poor air quality may affect sensitive plant species");
        }

        return hazards;
    }
}
