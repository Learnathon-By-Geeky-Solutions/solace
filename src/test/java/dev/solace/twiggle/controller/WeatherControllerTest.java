package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.config.RateLimiterConfiguration;
import dev.solace.twiggle.dto.WeatherDTO;
import dev.solace.twiggle.service.WeatherService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WeatherController.class)
@Import({RateLimiterConfiguration.class, WeatherControllerTest.WeatherTestConfig.class})
class WeatherControllerTest {

    @TestConfiguration
    static class WeatherTestConfig {
        @Bean
        @Primary
        public WeatherService weatherService() {
            return org.mockito.Mockito.mock(WeatherService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private ObjectMapper objectMapper;

    private WeatherDTO mockWeatherDTO;

    @BeforeEach
    void setUp() {
        mockWeatherDTO = WeatherDTO.builder()
                .location("San Francisco")
                .timestamp(LocalDateTime.now())
                .temperature(21.5)
                .temperatureUnit("C")
                .humidity(65.0)
                .windSpeed(10.0)
                .windSpeedUnit("km/h")
                .windDirection("NW")
                .cloudCover(30)
                .build();
    }

    @Test
    @DisplayName("getCurrentWeather should return weather data for a location")
    void getCurrentWeather_ShouldReturnWeatherData() throws Exception {
        // Arrange
        when(weatherService.getCurrentWeather(anyString())).thenReturn(mockWeatherDTO);

        // Act & Assert
        mockMvc.perform(get("/api/weather/current")
                        .param("location", "San Francisco")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved current weather"))
                .andExpect(jsonPath("$.data.location").value("San Francisco"))
                .andExpect(jsonPath("$.data.temperature").value(21.5));
    }

    @Test
    @DisplayName("getCurrentWeatherByCoordinates should return weather data for coordinates")
    void getCurrentWeatherByCoordinates_ShouldReturnWeatherData() throws Exception {
        // Arrange
        when(weatherService.getCurrentWeatherByCoordinates(any(double.class), any(double.class)))
                .thenReturn(mockWeatherDTO);

        // Act & Assert
        mockMvc.perform(get("/api/weather/current/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "-122.4194")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved current weather"))
                .andExpect(jsonPath("$.data.location").value("San Francisco"))
                .andExpect(jsonPath("$.data.temperature").value(21.5));
    }

    @Test
    @DisplayName("getWeatherForecast should return forecast data for a location")
    void getWeatherForecast_ShouldReturnForecastData() throws Exception {
        // Arrange
        when(weatherService.getWeatherForecast(anyString(), any(int.class))).thenReturn(mockWeatherDTO);

        // Act & Assert
        mockMvc.perform(get("/api/weather/forecast")
                        .param("location", "San Francisco")
                        .param("days", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved weather forecast"))
                .andExpect(jsonPath("$.data.location").value("San Francisco"));
    }

    @Test
    @DisplayName("getWeatherForecastByCoordinates should return forecast data for coordinates")
    void getWeatherForecastByCoordinates_ShouldReturnForecastData() throws Exception {
        // Arrange
        when(weatherService.getWeatherForecastByCoordinates(any(double.class), any(double.class), any(int.class)))
                .thenReturn(mockWeatherDTO);

        // Act & Assert
        mockMvc.perform(get("/api/weather/forecast/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "-122.4194")
                        .param("days", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved weather forecast"))
                .andExpect(jsonPath("$.data.location").value("San Francisco"));
    }

    @Test
    @DisplayName("getGardenWeather should return garden-specific weather data for a location")
    void getGardenWeather_ShouldReturnGardenWeatherData() throws Exception {
        // Arrange
        when(weatherService.getGardenWeather(anyString(), any(Optional.class))).thenReturn(mockWeatherDTO);

        // Act & Assert
        mockMvc.perform(get("/api/weather/garden")
                        .param("location", "San Francisco")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved garden weather information"))
                .andExpect(jsonPath("$.data.location").value("San Francisco"));
    }

    @Test
    @DisplayName("getGardenWeatherByCoordinates should return garden-specific weather data for coordinates")
    void getGardenWeatherByCoordinates_ShouldReturnGardenWeatherData() throws Exception {
        // Arrange
        when(weatherService.getGardenWeatherByCoordinates(any(double.class), any(double.class), any(Optional.class)))
                .thenReturn(mockWeatherDTO);

        // Act & Assert
        mockMvc.perform(get("/api/weather/garden/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "-122.4194")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved garden weather information"))
                .andExpect(jsonPath("$.data.location").value("San Francisco"));
    }
}
