package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.dto.WeatherDTO;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.service.impl.WeatherServiceImpl;
import dev.solace.twiggle.service.impl.WorldWeatherOnlineApiClient;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest { // Rename class later if desired (e.g., WeatherServiceImplTest)

    // Mock the direct dependency of WeatherServiceImpl
    @Mock
    private WorldWeatherOnlineApiClient weatherApiClient;

    // Inject mocks into the implementation class
    @InjectMocks
    private WeatherServiceImpl weatherService; // Test the implementation

    private final double latitude = 37.7749;
    private final double longitude = -122.4194;
    private final String location = "London";
    private final int days = 3;
    private final Optional<String> gardenPlanId = Optional.of("plan-123");

    // Mock JSON responses (can be simple valid JSON strings)
    private String mockApiResponse =
            "{\"data\": {\"current_condition\": [{\"temp_C\":\"15\"}], \"weather\": []}}"; // Minimal
    // valid
    // structure

    @BeforeEach
    void setUp() {
        // No complex WebClient mocking needed now
    }

    // Test methods corresponding to WeatherService INTERFACE methods
    // But call them on the IMPLEMENTATION instance (weatherService)

    @Test
    void getCurrentWeather_ShouldCallApiClientAndParse() {
        // Arrange
        when(weatherApiClient.getCurrentWeather(location)).thenReturn(mockApiResponse);

        // Act
        WeatherDTO result = weatherService.getCurrentWeather(location);

        // Assert
        assertNotNull(result);
        // Add more specific assertions based on mockApiResponse parsing if needed
        assertEquals(15.0, result.getTemperature()); // Example based on mockApiResponse
        verify(weatherApiClient).getCurrentWeather(location);
    }

    @Test
    void getCurrentWeatherByCoordinates_ShouldCallApiClientAndParse() {
        // Arrange
        when(weatherApiClient.getCurrentWeatherByCoordinates(latitude, longitude))
                .thenReturn(mockApiResponse);

        // Act
        WeatherDTO result = weatherService.getCurrentWeatherByCoordinates(latitude, longitude);

        // Assert
        assertNotNull(result);
        assertEquals(15.0, result.getTemperature()); // Example based on mockApiResponse
        verify(weatherApiClient).getCurrentWeatherByCoordinates(latitude, longitude);
    }

    @Test
    void getWeatherForecast_ShouldCallApiClientAndParse() {
        // Arrange
        when(weatherApiClient.getWeatherForecast(location, days)).thenReturn(mockApiResponse);

        // Act
        WeatherDTO result = weatherService.getWeatherForecast(location, days);

        // Assert
        assertNotNull(result);
        // Add assertions for forecast data if mockApiResponse included it
        verify(weatherApiClient).getWeatherForecast(location, days);
    }

    @Test
    void getWeatherForecastByCoordinates_ShouldCallApiClientAndParse() {
        // Arrange
        when(weatherApiClient.getWeatherForecastByCoordinates(latitude, longitude, days))
                .thenReturn(mockApiResponse);

        // Act
        WeatherDTO result = weatherService.getWeatherForecastByCoordinates(latitude, longitude, days);

        // Assert
        assertNotNull(result);
        // Add assertions for forecast data if mockApiResponse included it
        verify(weatherApiClient).getWeatherForecastByCoordinates(latitude, longitude, days);
    }

    @Test
    void getGardenWeather_ShouldCallApiClientAndParse() {
        // Arrange
        when(weatherApiClient.getWeatherForecast(location, 3))
                .thenReturn(mockApiResponse); // Garden weather uses 3-day forecast

        // Act
        WeatherDTO result = weatherService.getGardenWeather(location, gardenPlanId);

        // Assert
        assertNotNull(result);
        // Add assertions for garden advice if needed
        verify(weatherApiClient).getWeatherForecast(location, 3);
    }

    @Test
    void getGardenWeatherByCoordinates_ShouldCallApiClientAndParse() {
        // Arrange
        when(weatherApiClient.getWeatherForecastByCoordinates(latitude, longitude, 3))
                .thenReturn(mockApiResponse); // Garden weather uses 3-day forecast

        // Act
        WeatherDTO result = weatherService.getGardenWeatherByCoordinates(latitude, longitude, gardenPlanId);

        // Assert
        assertNotNull(result);
        // Add assertions for garden advice if needed
        verify(weatherApiClient).getWeatherForecastByCoordinates(latitude, longitude, 3);
    }

    @Test
    void getCurrentWeather_WhenApiThrowsException_ShouldThrowCustomException() {
        // Arrange
        when(weatherApiClient.getCurrentWeather(location)).thenThrow(new RuntimeException("API down"));

        // Act & Assert
        assertThrows(CustomException.class, () -> weatherService.getCurrentWeather(location));
        verify(weatherApiClient).getCurrentWeather(location);
    }

    // Add similar exception tests for other methods...
}
