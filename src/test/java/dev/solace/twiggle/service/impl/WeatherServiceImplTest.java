package dev.solace.twiggle.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
public class WeatherServiceImplTest {

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
    private static final String GARDEN_CURRENT_WEATHER_FILE = "garden_current_weather.json";
    private static final String GARDEN_FORECAST_WEATHER_FILE = "garden_forecast_weather.json";
    private static final String HIGH_HUMIDITY_WEATHER_FILE = "high_humidity_weather.json";
    private static final String HIGH_TEMP_WEATHER_FILE = "high_temp_weather.json";
    private static final String HEAVY_RAIN_WEATHER_FILE = "heavy_rain_weather.json";
    private static final String POOR_AIR_QUALITY_WEATHER_FILE = "poor_air_quality_weather.json";
    private static final String WEATHER_ALERT_FILE = "weather_alert.json";

    private String mockCurrentWeatherResponse;
    private String mockForecastResponse;
    private String mockGardenCurrentWeatherResponse;
    private String mockGardenForecastResponse;
    private String mockHighHumidityResponse;
    private String mockHighTempResponse;
    private String mockHeavyRainResponse;
    private String mockPoorAirQualityResponse;
    private String mockWeatherAlertResponse;

    @BeforeEach
    void setUp() throws IOException {
        // Load mock responses from resource files
        mockCurrentWeatherResponse = loadResourceFile(CURRENT_WEATHER_FILE);
        mockForecastResponse = loadResourceFile(FORECAST_WEATHER_FILE);
        mockGardenCurrentWeatherResponse = loadResourceFile(GARDEN_CURRENT_WEATHER_FILE);
        mockGardenForecastResponse = loadResourceFile(GARDEN_FORECAST_WEATHER_FILE);
        mockHighHumidityResponse = loadResourceFile(HIGH_HUMIDITY_WEATHER_FILE);
        mockHighTempResponse = loadResourceFile(HIGH_TEMP_WEATHER_FILE);
        mockHeavyRainResponse = loadResourceFile(HEAVY_RAIN_WEATHER_FILE);
        mockPoorAirQualityResponse = loadResourceFile(POOR_AIR_QUALITY_WEATHER_FILE);
        mockWeatherAlertResponse = loadResourceFile(WEATHER_ALERT_FILE);
    }

    private String loadResourceFile(String filename) throws IOException {
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
        CustomException exception = assertThrows(CustomException.class, () -> {
            weatherService.getCurrentWeather(LONDON);
        });
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
        assertTrue(result.getPlantHazards().size() > 0, "Plant hazards list should not be empty");
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
        assertTrue(result.getAirHazards().size() > 0, "Air hazards list should not be empty for poor air quality");
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
}
