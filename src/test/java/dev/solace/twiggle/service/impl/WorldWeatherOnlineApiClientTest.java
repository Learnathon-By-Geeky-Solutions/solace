package dev.solace.twiggle.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.config.WeatherApiConfig;
import dev.solace.twiggle.exception.CustomException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
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

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {"London<script>", "<London>", "London'", "London\"", "London;", "London--", "London://Paris"})
    void getCurrentWeather_InvalidLocationInputs(String location) {
        // Act & Assert
        CustomException exception =
                assertThrows(CustomException.class, () -> worldWeatherOnlineApiClient.getCurrentWeather(location));
        if (location == null || location.isBlank()) {
            assertEquals("Location parameter is required", exception.getMessage());
        } else {
            assertEquals("Invalid characters in location parameter", exception.getMessage());
        }
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

    @ParameterizedTest
    @CsvSource({"London, 3, 3, false", "London, 15, 15, false", "51.5074, -0.1278, 3, true"})
    void getWeatherForecast_VariousScenarios(
            String locationOrLat, double longitudeOrDays, int days, boolean isCoordinates) {
        // Arrange
        when(weatherApiConfig.getKey()).thenReturn(API_KEY);
        when(weatherApiConfig.getBaseUrl()).thenReturn(BASE_URL);
        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(VALID_RESPONSE, HttpStatus.OK));

        // Act
        String result;
        if (isCoordinates) {
            double lat = Double.parseDouble(locationOrLat);
            result = worldWeatherOnlineApiClient.getWeatherForecastByCoordinates(lat, longitudeOrDays, days);
        } else {
            result = worldWeatherOnlineApiClient.getWeatherForecast(locationOrLat, days);
        }

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

    @ParameterizedTest
    @CsvSource({
        "https://malicious-site.com,Untrusted domain for external API",
        "://invalid-url-format,Invalid URL format for external API",
        "http://api.worldweatheronline.com,Only HTTPS is allowed for external API calls"
    })
    void apiCall_InvalidBaseUrls(String baseUrl, String expectedMessage) {
        // Arrange
        when(weatherApiConfig.getKey()).thenReturn(API_KEY);
        when(weatherApiConfig.getBaseUrl()).thenReturn(baseUrl);

        // Act & Assert
        CustomException exception = assertThrows(
                CustomException.class, () -> worldWeatherOnlineApiClient.getCurrentWeather(VALID_LOCATION));
        assertEquals(expectedMessage, exception.getMessage());
        verify(restTemplate, never()).getForEntity(any(URI.class), eq(String.class));
    }

    @Test
    void validateFinalUri_InvalidEndpoint() {
        try {
            java.lang.reflect.Method method =
                    WorldWeatherOnlineApiClient.class.getDeclaredMethod("validateFinalUri", URI.class);
            method.setAccessible(true);

            URI invalidUri = new URI("https://api.worldweatheronline.com/invalid-endpoint");

            // Act & Assert
            InvocationTargetException exception = assertThrows(
                    InvocationTargetException.class, () -> method.invoke(worldWeatherOnlineApiClient, invalidUri));
            CustomException customException = (CustomException) exception.getCause();
            assertEquals("Invalid API endpoint", customException.getMessage());
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void makeApiCall_NonOkResponse() {
        // Arrange
        when(weatherApiConfig.getKey()).thenReturn(API_KEY);
        when(weatherApiConfig.getBaseUrl()).thenReturn(BASE_URL);
        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(VALID_RESPONSE, HttpStatus.BAD_REQUEST));

        // Act & Assert
        CustomException exception = assertThrows(
                CustomException.class, () -> worldWeatherOnlineApiClient.getCurrentWeather(VALID_LOCATION));
        assertEquals("Failed to retrieve weather data", exception.getMessage());
        verify(restTemplate).getForEntity(any(URI.class), eq(String.class));
    }
}
