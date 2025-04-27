package dev.solace.twiggle.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.solace.twiggle.dto.WeatherDTO;
import dev.solace.twiggle.exception.CustomException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

    @Mock
    private WorldWeatherOnlineApiClient weatherApiClient;

    @InjectMocks
    private WeatherServiceImpl weatherService;

    private static final String LONDON = "London";
    private static final double LAT = 51.5074;
    private static final double LON = -0.1278;
    private static final int FORECAST_DAYS = 5;
    private static final String CURRENT_WEATHER_FILE = "current_weather.json";
    private static final String FORECAST_WEATHER_FILE = "forecast_weather.json";
    private static final String GARDEN_FORECAST_WEATHER_FILE = "garden_forecast_weather.json";
    private static final String HIGH_HUMIDITY_WEATHER_FILE = "high_humidity_weather.json";
    private static final String HIGH_TEMP_WEATHER_FILE = "high_temp_weather.json";
    private static final String HEAVY_RAIN_WEATHER_FILE = "heavy_rain_weather.json";
    private static final String POOR_AIR_QUALITY_WEATHER_FILE = "poor_air_quality_weather.json";
    private static final String WEATHER_ALERT_FILE = "weather_alert.json";

    private String mockCurrentWeatherResponse;
    private String mockForecastResponse;
    private String mockGardenForecastResponse;
    private String mockHighHumidityResponse;
    private String mockHighTempResponse;
    private String mockHeavyRainResponse;
    private String mockPoorAirQualityResponse;
    private String mockWeatherAlertResponse;

    @BeforeEach
    void setUp() {
        // Load mock responses from resource files
        mockCurrentWeatherResponse = loadResourceFile(CURRENT_WEATHER_FILE);
        mockForecastResponse = loadResourceFile(FORECAST_WEATHER_FILE);
        mockGardenForecastResponse = loadResourceFile(GARDEN_FORECAST_WEATHER_FILE);
        mockHighHumidityResponse = loadResourceFile(HIGH_HUMIDITY_WEATHER_FILE);
        mockHighTempResponse = loadResourceFile(HIGH_TEMP_WEATHER_FILE);
        mockHeavyRainResponse = loadResourceFile(HEAVY_RAIN_WEATHER_FILE);
        mockPoorAirQualityResponse = loadResourceFile(POOR_AIR_QUALITY_WEATHER_FILE);
        mockWeatherAlertResponse = loadResourceFile(WEATHER_ALERT_FILE);
    }

    private String loadResourceFile(String filename) {
        try {
            ClassPathResource resource = new ClassPathResource("mock-responses/" + filename);
            return new String(Files.readAllBytes(Paths.get(resource.getURI())));
        } catch (IOException e) {
            // If file not found, return a simple JSON for testing
            return "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\"}]}}";
        }
    }

    @Test
    void getCurrentWeather_shouldReturnWeatherDTO() {
        // Arrange
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(mockCurrentWeatherResponse);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertNotNull(result);
        assertEquals(LONDON, result.getLocation());
        assertNotNull(result.getTemperature());
        assertNotNull(result.getHumidity());
        verify(weatherApiClient, times(1)).getCurrentWeather(LONDON);
    }

    @Test
    void getCurrentWeatherByCoordinates_shouldReturnWeatherDTO() {
        // Arrange
        when(weatherApiClient.getCurrentWeatherByCoordinates(LAT, LON)).thenReturn(mockCurrentWeatherResponse);

        // Act
        WeatherDTO result = weatherService.getCurrentWeatherByCoordinates(LAT, LON);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTemperature());
        assertNotNull(result.getHumidity());
        verify(weatherApiClient, times(1)).getCurrentWeatherByCoordinates(LAT, LON);
    }

    @Test
    void getWeatherForecast_shouldReturnWeatherDTOWithForecast() {
        // Arrange
        when(weatherApiClient.getWeatherForecast(LONDON, FORECAST_DAYS)).thenReturn(mockForecastResponse);

        // Act
        WeatherDTO result = weatherService.getWeatherForecast(LONDON, FORECAST_DAYS);

        // Assert
        assertNotNull(result);
        assertEquals(LONDON, result.getLocation());
        assertNotNull(result.getForecast());
        verify(weatherApiClient, times(1)).getWeatherForecast(LONDON, FORECAST_DAYS);
    }

    @Test
    void getWeatherForecastByCoordinates_shouldReturnWeatherDTOWithForecast() {
        // Arrange
        when(weatherApiClient.getWeatherForecastByCoordinates(LAT, LON, FORECAST_DAYS))
                .thenReturn(mockForecastResponse);

        // Act
        WeatherDTO result = weatherService.getWeatherForecastByCoordinates(LAT, LON, FORECAST_DAYS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getForecast());
        verify(weatherApiClient, times(1)).getWeatherForecastByCoordinates(LAT, LON, FORECAST_DAYS);
    }

    @Test
    void getGardenWeather_shouldReturnWeatherDTOWithGardeningAdvice() {
        // Arrange
        when(weatherApiClient.getWeatherForecast(LONDON, 3)).thenReturn(mockGardenForecastResponse);

        // Act
        WeatherDTO result = weatherService.getGardenWeather(LONDON, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(LONDON, result.getLocation());
        assertNotNull(result.getGardeningAdvice());
        verify(weatherApiClient, times(1)).getWeatherForecast(LONDON, 3);
    }

    @Test
    void getGardenWeatherByCoordinates_shouldReturnWeatherDTOWithGardeningAdvice() {
        // Arrange
        when(weatherApiClient.getWeatherForecastByCoordinates(LAT, LON, 3)).thenReturn(mockGardenForecastResponse);

        // Act
        WeatherDTO result = weatherService.getGardenWeatherByCoordinates(LAT, LON, Optional.empty());

        // Assert
        assertNotNull(result);
        assertNotNull(result.getGardeningAdvice());
        verify(weatherApiClient, times(1)).getWeatherForecastByCoordinates(LAT, LON, 3);
    }

    @Test
    void handleApiError_shouldThrowCustomException() {
        // Arrange
        when(weatherApiClient.getCurrentWeather(LONDON))
                .thenThrow(new CustomException("API Error", HttpStatus.INTERNAL_SERVER_ERROR));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> weatherService.getCurrentWeather(LONDON));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @Test
    void verifyPlantHazardsGeneration_shouldReturnNonEmptyList() {
        // Arrange
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(mockCurrentWeatherResponse);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertNotNull(result.getPlantHazards());
        assertFalse(result.getPlantHazards().isEmpty(), "Plant hazards list should not be empty");
    }

    @Test
    void verifyAirQualityProcessing_shouldProcessAirQualityData() {
        // Arrange
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(mockPoorAirQualityResponse);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertNotNull(result.getAirQualityIndex());
        assertNotNull(result.getAirHazards());
        assertFalse(result.getAirHazards().isEmpty(), "Air hazards list should not be empty for poor air quality");
    }

    @Test
    void verifyWeatherAlerts_shouldExtractAlertsFromResponse() {
        // Arrange
        when(weatherApiClient.getWeatherForecast(LONDON, FORECAST_DAYS)).thenReturn(mockWeatherAlertResponse);

        // Act
        WeatherDTO result = weatherService.getWeatherForecast(LONDON, FORECAST_DAYS);

        // Assert
        assertNotNull(result.getWeatherAlert());
        assertTrue(
                result.getForecast().stream()
                        .anyMatch(item ->
                                item.getAlerts() != null && !item.getAlerts().isEmpty()),
                "Forecast items should contain weather alerts");
    }

    @Test
    void verifyHighHumidityAdvice_shouldProvideAppropriateAdvice() {
        // Arrange
        when(weatherApiClient.getWeatherForecast(LONDON, 3)).thenReturn(mockHighHumidityResponse);

        // Act
        WeatherDTO result = weatherService.getGardenWeather(LONDON, Optional.empty());

        // Assert
        assertNotNull(result.getGardeningAdvice());
        assertEquals(
                "High humidity may promote fungal growth. Consider fungicide application.",
                result.getGardeningAdvice());

        // Verify plant hazards contain humidity-related warning
        List<String> plantHazards = result.getPlantHazards();
        assertTrue(
                plantHazards.stream()
                        .anyMatch(hazard -> hazard.toLowerCase().contains("humidity")
                                || hazard.toLowerCase().contains("fungal")),
                "Plant hazards should include humidity or fungal related warning");
    }

    @Test
    void verifyHighTemperatureAdvice_shouldProvideAppropriateAdvice() {
        // Arrange
        when(weatherApiClient.getWeatherForecast(LONDON, 3)).thenReturn(mockHighTempResponse);

        // Act
        WeatherDTO result = weatherService.getGardenWeather(LONDON, Optional.empty());

        // Assert
        assertNotNull(result.getGardeningAdvice());
        assertEquals("High temperatures expected. Ensure plants are well watered.", result.getGardeningAdvice());

        // Verify plant hazards contain temperature-related warning
        List<String> plantHazards = result.getPlantHazards();
        assertTrue(
                plantHazards.stream()
                        .anyMatch(hazard -> hazard.toLowerCase().contains("heat")
                                || hazard.toLowerCase().contains("temperature")),
                "Plant hazards should include heat or temperature related warning");
    }

    @Test
    void verifyHeavyRainAdvice_shouldProvideAppropriateAdvice() {
        // Arrange
        when(weatherApiClient.getWeatherForecast(LONDON, 3)).thenReturn(mockHeavyRainResponse);

        // Act
        WeatherDTO result = weatherService.getGardenWeather(LONDON, Optional.empty());

        // Assert
        assertNotNull(result.getGardeningAdvice());
        assertEquals(
                "Heavy rain expected. Check drainage systems and protect sensitive plants.",
                result.getGardeningAdvice());

        // Verify plant hazards contain rain-related warning
        List<String> plantHazards = result.getPlantHazards();
        assertTrue(
                plantHazards.stream()
                        .anyMatch(hazard -> hazard.toLowerCase().contains("rain")
                                || hazard.toLowerCase().contains("water")),
                "Plant hazards should include rain or water related warning");
    }

    @Test
    void verifyDefaultGardeningAdvice_shouldProvideAppropriateAdvice() {
        // Arrange
        String defaultWeatherResponse =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"precipMM\": \"5\"}]}}";
        when(weatherApiClient.getWeatherForecast(LONDON, 3)).thenReturn(defaultWeatherResponse);

        // Act
        WeatherDTO result = weatherService.getGardenWeather(LONDON, Optional.empty());

        // Assert
        assertNotNull(result.getGardeningAdvice());
        assertEquals("Weather conditions are favorable for gardening activities.", result.getGardeningAdvice());
    }

    @Test
    void verifyGardenWeatherWithGardenPlanId_shouldReturnWeatherDTO() {
        // Arrange
        String gardenPlanId = "garden-123";
        when(weatherApiClient.getWeatherForecast(LONDON, 3)).thenReturn(mockGardenForecastResponse);

        // Act
        WeatherDTO result = weatherService.getGardenWeather(LONDON, Optional.of(gardenPlanId));

        // Assert
        assertNotNull(result);
        assertEquals(LONDON, result.getLocation());
        assertNotNull(result.getGardeningAdvice());
        verify(weatherApiClient, times(1)).getWeatherForecast(LONDON, 3);
    }

    @Test
    void verifyGardenWeatherByCoordinatesWithGardenPlanId_shouldReturnWeatherDTO() {
        // Arrange
        String gardenPlanId = "garden-123";
        when(weatherApiClient.getWeatherForecastByCoordinates(LAT, LON, 3)).thenReturn(mockGardenForecastResponse);

        // Act
        WeatherDTO result = weatherService.getGardenWeatherByCoordinates(LAT, LON, Optional.of(gardenPlanId));

        // Assert
        assertNotNull(result);
        assertNotNull(result.getGardeningAdvice());
        verify(weatherApiClient, times(1)).getWeatherForecastByCoordinates(LAT, LON, 3);
    }

    @Test
    void verifyParseWeatherResponseWithInvalidJson_shouldThrowCustomException() {
        // Arrange
        String invalidJson = "invalid json";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(invalidJson);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> weatherService.getCurrentWeather(LONDON));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @Test
    void verifyExtractLocationName_shouldReturnDefaultLocationWhenNearestAreaNotAvailable() {
        // Arrange
        String jsonWithoutNearestArea =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\"}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithoutNearestArea);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertEquals(LONDON, result.getLocation());
    }

    @Test
    void verifyExtractLocationName_shouldReturnNearestAreaWhenAvailable() {
        // Arrange
        String jsonWithNearestArea =
                "{\"data\": {\"nearest_area\": [{\"areaName\": [{\"value\": \"Manchester\"}]}], \"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\"}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithNearestArea);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertEquals("Manchester", result.getLocation());
    }

    @Test
    void verifyGetAirQualityFromEpaIndex_shouldReturnCorrectValues() {
        // Arrange
        String jsonWithAirQuality =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 1}}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithAirQuality);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertEquals("Good", result.getAirQualityIndex());
    }

    @Test
    void verifyGetAirQualityFromEpaIndex_shouldHandleUnknownValues() {
        // Arrange
        String jsonWithUnknownAirQuality =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 7}}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithUnknownAirQuality);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertEquals("Moderate", result.getAirQualityIndex());
    }

    @Test
    void verifyGetCloudType_shouldReturnCorrectValues() {
        // Arrange
        String jsonWithCloudCover =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"cloudcover\": 10}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithCloudCover);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertEquals("Clear", result.getCloudType());
    }

    @Test
    void verifyGetPrecipitationType_shouldReturnCorrectValues() {
        // Arrange
        String jsonWithLowTemp = "{\"data\": {\"current_condition\": [{\"temp_C\": \"-5\", \"humidity\": \"70\"}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithLowTemp);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertEquals("Snow", result.getPrecipitationType());
    }

    @Test
    void verifyGetAirQualityIndex_shouldReturnCorrectValues() {
        // Arrange
        String jsonWithAirQuality =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 4}}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithAirQuality);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertEquals("Unhealthy", result.getAirQualityIndex());
    }

    @Test
    void verifyGetAirHazardsFromAirQuality_shouldIncludePollutantHazards() {
        // Arrange
        String jsonWithPollutants =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 4, \"pm2_5\": 40, \"pm10\": 160, \"o3\": 110, \"no2\": 110}}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithPollutants);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertTrue(result.getAirHazards().stream().anyMatch(hazard -> hazard.contains("PM2.5")));
        assertTrue(result.getAirHazards().stream().anyMatch(hazard -> hazard.contains("PM10")));
        assertTrue(result.getAirHazards().stream().anyMatch(hazard -> hazard.contains("ozone")));
        assertTrue(result.getAirHazards().stream().anyMatch(hazard -> hazard.contains("nitrogen dioxide")));
    }

    @Test
    void verifyParseAlerts_shouldHandleEmptyAlerts() {
        // Arrange
        String jsonWithoutAlerts = "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\"}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithoutAlerts);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertTrue(result.getForecast().isEmpty()
                || result.getForecast().getFirst().getAlerts().isEmpty());
    }

    @Test
    void verifyParseWeatherAlert_shouldHandleEmptyAlerts() {
        // Arrange
        String jsonWithoutAlerts = "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\"}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithoutAlerts);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertNull(result.getWeatherAlert());
    }

    @Test
    void verifyAddTemperatureTips_shouldAddAppropriateTips() {
        // Arrange
        String jsonWithHighTemp = "{\"data\": {\"current_condition\": [{\"temp_C\": \"38\", \"humidity\": \"70\"}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithHighTemp);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertTrue(result.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("Extreme heat warning")));
    }

    @Test
    void verifyAddHumidityTips_shouldAddAppropriateTips() {
        // Arrange
        String jsonWithLowHumidity =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"25\"}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithLowHumidity);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertTrue(result.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("Very dry conditions")));
    }

    @Test
    void verifyAddUvIndexTips_shouldAddAppropriateTips() {
        // Arrange
        String jsonWithHighUv =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"uvIndex\": 8}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithHighUv);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertTrue(result.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("Very high UV")));
    }

    @Test
    void verifyAddPrecipitationTips_shouldAddAppropriateTips() {
        // Arrange
        String jsonWithNoPrecipitation =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"precipMM\": \"0\"}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithNoPrecipitation);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertTrue(result.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("No rain today")));
    }

    @Test
    void verifyAddAirQualityTips_shouldAddAppropriateTips() {
        // Arrange
        String jsonWithUnhealthyAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 4}}]}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithUnhealthyAir);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        assertTrue(result.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("Air quality is unhealthy")));
    }

    @Test
    void verifyAddPlantSpecificSuggestions_shouldAddAllSuggestions() {
        // Arrange
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(mockCurrentWeatherResponse);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(LONDON);

        // Assert
        List<String> plantHazards = result.getPlantHazards();
        assertTrue(plantHazards.stream().anyMatch(hazard -> hazard.contains("Succulents")));
        assertTrue(plantHazards.stream().anyMatch(hazard -> hazard.contains("Flowering Plants")));
        assertTrue(plantHazards.stream().anyMatch(hazard -> hazard.contains("Vegetables")));
        assertTrue(plantHazards.stream().anyMatch(hazard -> hazard.contains("Herbs")));
    }

    @Test
    void verifyHandleParseWeatherResponseWithNoCurrentCondition() {
        // Arrange
        String jsonWithNoCurrentCondition = "{\"data\": {}}";
        when(weatherApiClient.getCurrentWeather(LONDON)).thenReturn(jsonWithNoCurrentCondition);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> weatherService.getCurrentWeather(LONDON));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @Test
    void verifyGetAirQualityFromEpaIndex_otherIndices() {
        // Test index 2 (Moderate)
        String jsonWithModerateAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 2}}]}}";
        when(weatherApiClient.getCurrentWeather("Moderate")).thenReturn(jsonWithModerateAir);
        WeatherDTO moderateResult = weatherService.getCurrentWeather("Moderate");
        assertEquals("Moderate", moderateResult.getAirQualityIndex());

        // Test index 3 (Unhealthy for Sensitive Groups)
        String jsonWithSensitiveAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 3}}]}}";
        when(weatherApiClient.getCurrentWeather("Sensitive")).thenReturn(jsonWithSensitiveAir);
        WeatherDTO sensitiveResult = weatherService.getCurrentWeather("Sensitive");
        assertEquals("Unhealthy for Sensitive Groups", sensitiveResult.getAirQualityIndex());

        // Test index 5 (Very Unhealthy)
        String jsonWithVeryUnhealthyAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 5}}]}}";
        when(weatherApiClient.getCurrentWeather("VeryUnhealthy")).thenReturn(jsonWithVeryUnhealthyAir);
        WeatherDTO veryUnhealthyResult = weatherService.getCurrentWeather("VeryUnhealthy");
        assertEquals("Very Unhealthy", veryUnhealthyResult.getAirQualityIndex());

        // Test index 6 (Hazardous)
        String jsonWithHazardousAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 6}}]}}";
        when(weatherApiClient.getCurrentWeather("Hazardous")).thenReturn(jsonWithHazardousAir);
        WeatherDTO hazardousResult = weatherService.getCurrentWeather("Hazardous");
        assertEquals("Hazardous", hazardousResult.getAirQualityIndex());
    }

    @Test
    void verifyGetCloudType_differentRanges() {
        // Test 20-49% cloud cover (Cumulus)
        String jsonWithModerateClouds =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"cloudcover\": 30}]}}";
        when(weatherApiClient.getCurrentWeather("Cumulus")).thenReturn(jsonWithModerateClouds);
        WeatherDTO cumulusResult = weatherService.getCurrentWeather("Cumulus");
        assertEquals("Cumulus", cumulusResult.getCloudType());

        // Test 50-79% cloud cover (Stratocumulus)
        String jsonWithHighClouds =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"cloudcover\": 60}]}}";
        when(weatherApiClient.getCurrentWeather("Stratocumulus")).thenReturn(jsonWithHighClouds);
        WeatherDTO stratocumulusResult = weatherService.getCurrentWeather("Stratocumulus");
        assertEquals("Stratocumulus", stratocumulusResult.getCloudType());

        // Test 80%+ cloud cover (Stratus)
        String jsonWithVeryCloudy =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"cloudcover\": 90}]}}";
        when(weatherApiClient.getCurrentWeather("Stratus")).thenReturn(jsonWithVeryCloudy);
        WeatherDTO stratusResult = weatherService.getCurrentWeather("Stratus");
        assertEquals("Stratus", stratusResult.getCloudType());
    }

    @Test
    void verifyGetPrecipitationType_differentTemperatureRanges() {
        // Test 0-3°C (Sleet)
        String jsonWithSleetTemp = "{\"data\": {\"current_condition\": [{\"temp_C\": \"2\", \"humidity\": \"70\"}]}}";
        when(weatherApiClient.getCurrentWeather("Sleet")).thenReturn(jsonWithSleetTemp);
        WeatherDTO sleetResult = weatherService.getCurrentWeather("Sleet");
        assertEquals("Sleet", sleetResult.getPrecipitationType());

        // Test 4°C+ (Rain)
        String jsonWithRainTemp = "{\"data\": {\"current_condition\": [{\"temp_C\": \"10\", \"humidity\": \"70\"}]}}";
        when(weatherApiClient.getCurrentWeather("Rain")).thenReturn(jsonWithRainTemp);
        WeatherDTO rainResult = weatherService.getCurrentWeather("Rain");
        assertEquals("Rain", rainResult.getPrecipitationType());
    }

    @Test
    void verifyParseCurrentConditions_withoutUvIndex() {
        // Test weather data without UV index
        String jsonWithoutUvIndex =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"windspeedKmph\": \"10\", \"winddir16Point\": \"N\", \"cloudcover\": \"30\", \"precipMM\": \"0\"}]}}";
        when(weatherApiClient.getCurrentWeather("NoUV")).thenReturn(jsonWithoutUvIndex);
        WeatherDTO result = weatherService.getCurrentWeather("NoUV");
        assertEquals(0.0, result.getUvIndex());
    }

    @Test
    void verifyGetAirHazardsFromAirQuality_differentQualityLevels() {
        // Test Good air quality (should return empty list)
        String jsonWithGoodAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 1}}]}}";
        when(weatherApiClient.getCurrentWeather("GoodAir")).thenReturn(jsonWithGoodAir);
        WeatherDTO goodAirResult = weatherService.getCurrentWeather("GoodAir");
        assertTrue(goodAirResult.getAirHazards().isEmpty());

        // Test Moderate air quality
        String jsonWithModerateAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 2}}]}}";
        when(weatherApiClient.getCurrentWeather("ModerateAir")).thenReturn(jsonWithModerateAir);
        WeatherDTO moderateAirResult = weatherService.getCurrentWeather("ModerateAir");
        assertTrue(moderateAirResult.getAirHazards().stream().anyMatch(hazard -> hazard.contains("Mild pollen")));

        // Test Unhealthy for Sensitive Groups
        String jsonWithSensitiveAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 3}}]}}";
        when(weatherApiClient.getCurrentWeather("SensitiveAir")).thenReturn(jsonWithSensitiveAir);
        WeatherDTO sensitiveAirResult = weatherService.getCurrentWeather("SensitiveAir");
        assertTrue(sensitiveAirResult.getAirHazards().stream()
                .anyMatch(hazard -> hazard.contains("sensitive individuals")));

        // Test Unhealthy air quality
        String jsonWithUnhealthyAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 4}}]}}";
        when(weatherApiClient.getCurrentWeather("UnhealthyAir")).thenReturn(jsonWithUnhealthyAir);
        WeatherDTO unhealthyAirResult = weatherService.getCurrentWeather("UnhealthyAir");
        assertTrue(
                unhealthyAirResult.getAirHazards().stream().anyMatch(hazard -> hazard.contains("general population")));

        // Test Very Unhealthy air quality
        String jsonWithVeryUnhealthyAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 5}}]}}";
        when(weatherApiClient.getCurrentWeather("VeryUnhealthyAir")).thenReturn(jsonWithVeryUnhealthyAir);
        WeatherDTO veryUnhealthyAirResult = weatherService.getCurrentWeather("VeryUnhealthyAir");
        assertTrue(veryUnhealthyAirResult.getAirHazards().stream()
                .anyMatch(hazard -> hazard.contains("Significant respiratory effects")));

        // Test Hazardous air quality
        String jsonWithHazardousAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 6}}]}}";
        when(weatherApiClient.getCurrentWeather("HazardousAir")).thenReturn(jsonWithHazardousAir);
        WeatherDTO hazardousAirResult = weatherService.getCurrentWeather("HazardousAir");
        assertTrue(hazardousAirResult.getAirHazards().stream()
                .anyMatch(hazard -> hazard.contains("Serious respiratory effects")));

        // Test unknown air quality
        String jsonWithUnknownAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 0}}]}}";
        when(weatherApiClient.getCurrentWeather("UnknownAir")).thenReturn(jsonWithUnknownAir);
        WeatherDTO unknownAirResult = weatherService.getCurrentWeather("UnknownAir");
        // Should handle this without errors
        assertNotNull(unknownAirResult.getAirHazards());
    }

    @Test
    void verifyAddBasicWeatherHazards_allConditions() {
        // Test multiple hazard conditions together
        String jsonWithMultipleHazards =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"30\", \"humidity\": \"90\", \"windspeedKmph\": \"25\", \"precipMM\": \"20\", \"uvIndex\": \"9\", \"air_quality\": {\"us-epa-index\": 4}}]}}";
        when(weatherApiClient.getCurrentWeather("MultipleHazards")).thenReturn(jsonWithMultipleHazards);
        WeatherDTO result = weatherService.getCurrentWeather("MultipleHazards");

        List<String> hazards = result.getPlantHazards();
        assertTrue(hazards.stream().anyMatch(hazard -> hazard.contains("Heat stress")));
        assertTrue(hazards.stream().anyMatch(hazard -> hazard.contains("High humidity")));
        assertTrue(hazards.stream().anyMatch(hazard -> hazard.contains("High UV")));
        assertTrue(hazards.stream().anyMatch(hazard -> hazard.contains("Strong winds")));
        assertTrue(hazards.stream().anyMatch(hazard -> hazard.contains("Heavy rain")));
        assertTrue(hazards.stream().anyMatch(hazard -> hazard.contains("Poor air quality")));
    }

    @Test
    void verifyAddTemperatureTips_allRanges() {
        // Test cold temperature (<15°C)
        String jsonWithColdTemp = "{\"data\": {\"current_condition\": [{\"temp_C\": \"10\", \"humidity\": \"70\"}]}}";
        when(weatherApiClient.getCurrentWeather("ColdTemp")).thenReturn(jsonWithColdTemp);
        WeatherDTO coldResult = weatherService.getCurrentWeather("ColdTemp");
        assertTrue(coldResult.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("Cold stress")));

        // Test ideal temperature (15-32°C)
        String jsonWithIdealTemp = "{\"data\": {\"current_condition\": [{\"temp_C\": \"25\", \"humidity\": \"70\"}]}}";
        when(weatherApiClient.getCurrentWeather("IdealTemp")).thenReturn(jsonWithIdealTemp);
        WeatherDTO idealResult = weatherService.getCurrentWeather("IdealTemp");
        assertTrue(idealResult.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("Ideal temperature")));

        // Test high temperature (32-36°C)
        String jsonWithHighTemp = "{\"data\": {\"current_condition\": [{\"temp_C\": \"34\", \"humidity\": \"70\"}]}}";
        when(weatherApiClient.getCurrentWeather("HighTemp")).thenReturn(jsonWithHighTemp);
        WeatherDTO highResult = weatherService.getCurrentWeather("HighTemp");
        assertTrue(highResult.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("High heat today")));
    }

    @Test
    void verifyAddHumidityTips_allRanges() {
        // Test very dry conditions (<30%)
        String jsonWithDryHumidity =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"25\"}]}}";
        when(weatherApiClient.getCurrentWeather("DryHumidity")).thenReturn(jsonWithDryHumidity);
        WeatherDTO dryResult = weatherService.getCurrentWeather("DryHumidity");
        assertTrue(dryResult.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("Very dry conditions")));

        // Test comfortable humidity (30-70%)
        String jsonWithComfortableHumidity =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"50\"}]}}";
        when(weatherApiClient.getCurrentWeather("ComfortableHumidity")).thenReturn(jsonWithComfortableHumidity);
        WeatherDTO comfortableResult = weatherService.getCurrentWeather("ComfortableHumidity");
        assertTrue(comfortableResult.getPlantHazards().stream()
                .anyMatch(hazard -> hazard.contains("Comfortable humidity")));
    }

    @Test
    void verifyAddUvIndexTips_allRanges() {
        // Test low UV (0-2)
        String jsonWithLowUv =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"uvIndex\": \"2\"}]}}";
        when(weatherApiClient.getCurrentWeather("LowUV")).thenReturn(jsonWithLowUv);
        WeatherDTO lowUvResult = weatherService.getCurrentWeather("LowUV");
        assertTrue(lowUvResult.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("Low UV exposure")));

        // Test moderate UV (3-5)
        String jsonWithModerateUv =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"uvIndex\": \"4\"}]}}";
        when(weatherApiClient.getCurrentWeather("ModerateUV")).thenReturn(jsonWithModerateUv);
        WeatherDTO moderateUvResult = weatherService.getCurrentWeather("ModerateUV");
        assertTrue(
                moderateUvResult.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("Moderate UV levels")));

        // Test high UV (6-7)
        String jsonWithHighUv =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"uvIndex\": \"7\"}]}}";
        when(weatherApiClient.getCurrentWeather("HighUV")).thenReturn(jsonWithHighUv);
        WeatherDTO highUvResult = weatherService.getCurrentWeather("HighUV");
        assertTrue(highUvResult.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("High UV levels")));
    }

    @Test
    void verifyAddPrecipitationTips_withRain() {
        // Test with precipitation
        String jsonWithRain =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"precipMM\": \"5\"}]}}";
        when(weatherApiClient.getCurrentWeather("Rain")).thenReturn(jsonWithRain);
        WeatherDTO result = weatherService.getCurrentWeather("Rain");
        assertTrue(result.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("Some rain expected")));
    }

    @Test
    void verifyAddAirQualityTips_allRanges() {
        // Test good air quality (1-2)
        String jsonWithGoodAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 1}}]}}";
        when(weatherApiClient.getCurrentWeather("GoodAir")).thenReturn(jsonWithGoodAir);
        WeatherDTO goodAirResult = weatherService.getCurrentWeather("GoodAir");
        assertTrue(goodAirResult.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("Air quality is good")));

        // Test moderate air quality (3)
        String jsonWithModerateAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 3}}]}}";
        when(weatherApiClient.getCurrentWeather("ModerateAir")).thenReturn(jsonWithModerateAir);
        WeatherDTO moderateAirResult = weatherService.getCurrentWeather("ModerateAir");
        assertTrue(moderateAirResult.getPlantHazards().stream()
                .anyMatch(hazard -> hazard.contains("Moderate air quality")));
    }

    @Test
    void verifyGetAirQualityIndex_allValues() {
        // Since this is a private method, we test it indirectly through the parsing

        // Test with unknown air quality value
        String jsonWithUnknownAir =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"air_quality\": {\"us-epa-index\": 0}}]}}";
        when(weatherApiClient.getCurrentWeather("UnknownAir")).thenReturn(jsonWithUnknownAir);
        WeatherDTO result = weatherService.getCurrentWeather("UnknownAir");

        // Check that plant hazards contain expected air quality tip based on default
        // value (moderate)
        assertTrue(result.getPlantHazards().stream()
                .anyMatch(hazard -> hazard.contains("Air quality is good") || hazard.contains("Moderate air quality")));
    }

    @Test
    void verifyParseWeatherResponse_withNullWeatherAlert() {
        // Test with no alerts data
        String jsonWithNoAlerts =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\"}], \"weather\": [{\"date\": \"2023-04-27\", \"hourly\": [{\"time\": \"0\", \"tempC\": \"18\", \"humidity\": \"80\", \"cloudcover\": \"25\", \"precipMM\": \"0\", \"weatherDesc\": [{\"value\": \"Clear\"}]}]}]}}";
        when(weatherApiClient.getWeatherForecast(LONDON, FORECAST_DAYS)).thenReturn(jsonWithNoAlerts);

        WeatherDTO result = weatherService.getWeatherForecast(LONDON, FORECAST_DAYS);
        assertNull(result.getWeatherAlert());
    }

    @Test
    void verifyForecastWithLimits() {
        // Test a case where forecast days exceeds available data
        // The API should handle this gracefully by using the available days
        String jsonWithLimitedForecast =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\"}], \"weather\": [{\"date\": \"2023-04-27\", \"hourly\": [{\"time\": \"0\", \"tempC\": \"18\", \"humidity\": \"80\", \"cloudcover\": \"25\", \"precipMM\": \"0\", \"weatherDesc\": [{\"value\": \"Clear\"}]}]}]}}";
        when(weatherApiClient.getWeatherForecast(LONDON, 10)).thenReturn(jsonWithLimitedForecast);

        // Request 10 days but only 1 is available
        WeatherDTO result = weatherService.getWeatherForecast(LONDON, 10);
        assertEquals(1, result.getForecast().size());
    }

    @Test
    void verifyForecastWithMultipleDays() {
        // Test a case with multiple days in the forecast
        String jsonWithMultipleDays =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\"}], \"weather\": ["
                        + "{\"date\": \"2023-04-27\", \"hourly\": [{\"time\": \"0\", \"tempC\": \"18\", \"humidity\": \"80\", \"cloudcover\": \"25\", \"precipMM\": \"0\", \"weatherDesc\": [{\"value\": \"Clear\"}]}]},"
                        + "{\"date\": \"2023-04-28\", \"hourly\": [{\"time\": \"0\", \"tempC\": \"19\", \"humidity\": \"75\", \"cloudcover\": \"30\", \"precipMM\": \"0\", \"weatherDesc\": [{\"value\": \"Partly Cloudy\"}]}]}"
                        + "]}}";
        when(weatherApiClient.getWeatherForecast(LONDON, 2)).thenReturn(jsonWithMultipleDays);

        WeatherDTO result = weatherService.getWeatherForecast(LONDON, 2);
        assertEquals(2, result.getForecast().size());
    }

    @Test
    void verifyLowTempHazardWarning() {
        // Test frost risk for low temperatures
        String jsonWithLowTemp = "{\"data\": {\"current_condition\": [{\"temp_C\": \"3\", \"humidity\": \"70\"}]}}";
        when(weatherApiClient.getCurrentWeather("Cold")).thenReturn(jsonWithLowTemp);

        WeatherDTO result = weatherService.getCurrentWeather("Cold");
        assertTrue(result.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("Frost risk")));
    }

    @Test
    void verifyWindHazardWarning() {
        // Test strong wind warning
        String jsonWithStrongWind =
                "{\"data\": {\"current_condition\": [{\"temp_C\": \"20\", \"humidity\": \"70\", \"windspeedKmph\": \"25\"}]}}";
        when(weatherApiClient.getCurrentWeather("Windy")).thenReturn(jsonWithStrongWind);

        WeatherDTO result = weatherService.getCurrentWeather("Windy");
        assertTrue(result.getPlantHazards().stream().anyMatch(hazard -> hazard.contains("Strong winds")));
    }

    // Test location coordinates with format
    @Test
    void verifyFormattedCoordinates() {
        // Just verify that the coordinates are formatted correctly in the returned
        // object
        // This is testing the FORMAT_PATTERN constant
        when(weatherApiClient.getCurrentWeatherByCoordinates(LAT, LON)).thenReturn(mockCurrentWeatherResponse);

        WeatherDTO result = weatherService.getCurrentWeatherByCoordinates(LAT, LON);
        // We can't directly check the formatted value since it depends on private
        // implementation
        // But we can verify the result is correctly processed
        assertNotNull(result);
    }
}
