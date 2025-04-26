package dev.solace.twiggle.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.config.WeatherApiConfig;
import dev.solace.twiggle.exception.CustomException;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class WorldWeatherOnlineApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private WeatherApiConfig weatherApiConfig;

    @InjectMocks
    private WorldWeatherOnlineApiClient worldWeatherOnlineApiClient;

    private static final String API_KEY = "test-api-key";
    private static final String BASE_URL = "https://api.worldweatheronline.com";
    private static final String VALID_LOCATION = "London";
    private static final String VALID_RESPONSE = "{\"data\":{\"current_condition\":[{\"temp_C\":\"20\"}]}}";

    @Test
    void getCurrentWeather_Success() {
        // Arrange
        when(weatherApiConfig.getKey()).thenReturn(API_KEY);
        when(weatherApiConfig.getBaseUrl()).thenReturn(BASE_URL);
        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(VALID_RESPONSE, HttpStatus.OK));

        // Act
        String result = worldWeatherOnlineApiClient.getCurrentWeather(VALID_LOCATION);

        // Assert
        assertNotNull(result);
        assertEquals(VALID_RESPONSE, result);
        verify(restTemplate).getForEntity(any(URI.class), eq(String.class));
    }

    @Test
    void getCurrentWeather_InvalidLocation() {
        // No need to configure mocks that aren't used in this test

        // Act & Assert
        CustomException exception =
                assertThrows(CustomException.class, () -> worldWeatherOnlineApiClient.getCurrentWeather(""));
        assertEquals("Location parameter is required", exception.getMessage());
        verify(restTemplate, never()).getForEntity(any(URI.class), eq(String.class));
    }

    @Test
    void getCurrentWeatherByCoordinates_Success() {
        // Arrange
        when(weatherApiConfig.getKey()).thenReturn(API_KEY);
        when(weatherApiConfig.getBaseUrl()).thenReturn(BASE_URL);
        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(VALID_RESPONSE, HttpStatus.OK));

        // Act
        String result = worldWeatherOnlineApiClient.getCurrentWeatherByCoordinates(51.5074, -0.1278);

        // Assert
        assertNotNull(result);
        assertEquals(VALID_RESPONSE, result);
        verify(restTemplate).getForEntity(any(URI.class), eq(String.class));
    }

    @Test
    void getCurrentWeatherByCoordinates_InvalidCoordinates() {
        // No need to configure mocks that aren't used in this test

        // Act & Assert
        CustomException exception = assertThrows(
                CustomException.class, () -> worldWeatherOnlineApiClient.getCurrentWeatherByCoordinates(91.0, 0.0));
        assertEquals(
                "Invalid coordinates: latitude must be between -90 and 90, longitude between -180 and 180",
                exception.getMessage());
        verify(restTemplate, never()).getForEntity(any(URI.class), eq(String.class));
    }

    @Test
    void getWeatherForecast_Success() {
        // Arrange
        when(weatherApiConfig.getKey()).thenReturn(API_KEY);
        when(weatherApiConfig.getBaseUrl()).thenReturn(BASE_URL);
        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(VALID_RESPONSE, HttpStatus.OK));

        // Act
        String result = worldWeatherOnlineApiClient.getWeatherForecast(VALID_LOCATION, 3);

        // Assert
        assertNotNull(result);
        assertEquals(VALID_RESPONSE, result);
        verify(restTemplate).getForEntity(any(URI.class), eq(String.class));
    }

    @Test
    void getWeatherForecast_InvalidDays() {
        // Arrange
        when(weatherApiConfig.getKey()).thenReturn(API_KEY);
        when(weatherApiConfig.getBaseUrl()).thenReturn(BASE_URL);
        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(VALID_RESPONSE, HttpStatus.OK));

        // Act
        String result = worldWeatherOnlineApiClient.getWeatherForecast(VALID_LOCATION, 15);

        // Assert
        assertNotNull(result);
        assertEquals(VALID_RESPONSE, result);
        verify(restTemplate).getForEntity(any(URI.class), eq(String.class));
    }

    @Test
    void getWeatherForecastByCoordinates_Success() {
        // Arrange
        when(weatherApiConfig.getKey()).thenReturn(API_KEY);
        when(weatherApiConfig.getBaseUrl()).thenReturn(BASE_URL);
        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(VALID_RESPONSE, HttpStatus.OK));

        // Act
        String result = worldWeatherOnlineApiClient.getWeatherForecastByCoordinates(51.5074, -0.1278, 3);

        // Assert
        assertNotNull(result);
        assertEquals(VALID_RESPONSE, result);
        verify(restTemplate).getForEntity(any(URI.class), eq(String.class));
    }

    @Test
    void getWeatherForecastByCoordinates_InvalidDays() {
        // Arrange
        when(weatherApiConfig.getKey()).thenReturn(API_KEY);
        when(weatherApiConfig.getBaseUrl()).thenReturn(BASE_URL);
        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(VALID_RESPONSE, HttpStatus.OK));

        // Act
        String result = worldWeatherOnlineApiClient.getWeatherForecastByCoordinates(51.5074, -0.1278, 15);

        // Assert
        assertNotNull(result);
        assertEquals(VALID_RESPONSE, result);
        verify(restTemplate).getForEntity(any(URI.class), eq(String.class));
    }

    @Test
    void makeApiCall_ApiError() {
        // Arrange
        when(weatherApiConfig.getKey()).thenReturn(API_KEY);
        when(weatherApiConfig.getBaseUrl()).thenReturn(BASE_URL);
        when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenThrow(new RuntimeException("API Error"));

        // Act & Assert
        CustomException exception = assertThrows(
                CustomException.class, () -> worldWeatherOnlineApiClient.getCurrentWeather(VALID_LOCATION));
        assertEquals("Failed to retrieve weather data", exception.getMessage());
        verify(restTemplate).getForEntity(any(URI.class), eq(String.class));
    }

    @Test
    void makeApiCall_UntrustedDomain() {
        // Arrange
        when(weatherApiConfig.getKey()).thenReturn(API_KEY);
        when(weatherApiConfig.getBaseUrl()).thenReturn("https://malicious-site.com");

        // Act & Assert
        CustomException exception = assertThrows(
                CustomException.class, () -> worldWeatherOnlineApiClient.getCurrentWeather(VALID_LOCATION));
        assertEquals("Untrusted domain for external API", exception.getMessage());
        verify(restTemplate, never()).getForEntity(any(URI.class), eq(String.class));
    }
}
