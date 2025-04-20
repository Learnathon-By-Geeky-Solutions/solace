package dev.solace.twiggle.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.solace.twiggle.dto.WeatherDTO;
import dev.solace.twiggle.exception.CustomException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    private String mockCurrentWeatherResponse;
    private String mockForecastResponse;
    private String mockGardenCurrentWeatherResponse;
    private String mockGardenForecastResponse;

    @BeforeEach
    void setUp() throws IOException {
        // Load mock responses from resource files
        mockCurrentWeatherResponse = loadResourceFile(CURRENT_WEATHER_FILE);
        mockForecastResponse = loadResourceFile(FORECAST_WEATHER_FILE);
        mockGardenCurrentWeatherResponse = loadResourceFile(GARDEN_CURRENT_WEATHER_FILE);
        mockGardenForecastResponse = loadResourceFile(GARDEN_FORECAST_WEATHER_FILE);
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
}
