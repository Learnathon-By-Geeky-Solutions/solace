package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.solace.twiggle.config.RateLimiterConfiguration;
import dev.solace.twiggle.dto.WeatherDTO;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
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
import org.springframework.http.HttpStatus;
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

    private WeatherDTO mockWeatherDTO;

    @BeforeEach
    void setUp() {
        // Reset the mock to clear any previous interactions
        reset(weatherService);

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
    @DisplayName("getCurrentWeather should return error when location is blank")
    void getCurrentWeather_ShouldReturnError_WhenLocationIsBlank() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/current").param("location", "").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getCurrentWeather should return error when service throws exception")
    void getCurrentWeather_ShouldReturnError_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(weatherService.getCurrentWeather(anyString())).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/weather/current")
                        .param("location", "San Francisco")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to retrieve current weather"));
    }

    @Test
    @DisplayName("getCurrentWeatherByCoordinates should return weather data for coordinates")
    void getCurrentWeatherByCoordinates_ShouldReturnWeatherData() throws Exception {
        // Arrange
        doReturn(mockWeatherDTO)
                .when(weatherService)
                .getCurrentWeatherByCoordinates(any(double.class), any(double.class));

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
    @DisplayName("getCurrentWeatherByCoordinates should return error when latitude is out of range")
    void getCurrentWeatherByCoordinates_ShouldReturnError_WhenLatitudeIsOutOfRange() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/current/coordinates")
                        .param("latitude", "91.0")
                        .param("longitude", "-122.4194")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getCurrentWeatherByCoordinates should return error when longitude is out of range")
    void getCurrentWeatherByCoordinates_ShouldReturnError_WhenLongitudeIsOutOfRange() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/current/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "181.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getCurrentWeatherByCoordinates should return error when service throws exception")
    void getCurrentWeatherByCoordinates_ShouldReturnError_WhenServiceThrowsException() throws Exception {
        // Arrange - Use doThrow for throwing exceptions
        doThrow(new RuntimeException("Service error"))
                .when(weatherService)
                .getCurrentWeatherByCoordinates(any(double.class), any(double.class));

        // Act & Assert
        mockMvc.perform(get("/api/weather/current/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "-122.4194")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to retrieve current weather"));
    }

    @Test
    @DisplayName("getWeatherForecast should return forecast data for a location")
    void getWeatherForecast_ShouldReturnForecastData() throws Exception {
        // Arrange - Make sure we use the right method
        doReturn(mockWeatherDTO).when(weatherService).getWeatherForecast(anyString(), anyInt());

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
    @DisplayName("getWeatherForecast should return error when location is blank")
    void getWeatherForecast_ShouldReturnError_WhenLocationIsBlank() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/forecast")
                        .param("location", "")
                        .param("days", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getWeatherForecast should return error when days is out of range")
    void getWeatherForecast_ShouldReturnError_WhenDaysIsOutOfRange() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/forecast")
                        .param("location", "San Francisco")
                        .param("days", "8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getWeatherForecast should return error when service throws CustomException")
    void getWeatherForecast_ShouldReturnError_WhenServiceThrowsCustomException() throws Exception {
        // Arrange
        CustomException customException =
                new CustomException("Custom error", HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST);

        // Use doThrow for throwing exceptions
        doThrow(customException).when(weatherService).getWeatherForecast(anyString(), anyInt());

        // Act & Assert
        mockMvc.perform(get("/api/weather/forecast")
                        .param("location", "San Francisco")
                        .param("days", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Custom error"));
    }

    @Test
    @DisplayName("getWeatherForecast should return error when service throws exception")
    void getWeatherForecast_ShouldReturnError_WhenServiceThrowsException() throws Exception {
        // Arrange - Use doThrow to ensure the mock throws consistently
        when(weatherService.getWeatherForecast(anyString(), anyInt())).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/weather/forecast")
                        .param("location", "San Francisco")
                        .param("days", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to retrieve weather forecast"));
    }

    @Test
    @DisplayName("getWeatherForecastByCoordinates should return forecast data for coordinates")
    void getWeatherForecastByCoordinates_ShouldReturnForecastData() throws Exception {
        // Arrange
        // Use doReturn for primitive parameters to avoid any ambiguity
        doReturn(mockWeatherDTO)
                .when(weatherService)
                .getWeatherForecastByCoordinates(any(double.class), any(double.class), anyInt());

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
    @DisplayName("getWeatherForecastByCoordinates should return error when latitude is out of range")
    void getWeatherForecastByCoordinates_ShouldReturnError_WhenLatitudeIsOutOfRange() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/forecast/coordinates")
                        .param("latitude", "91.0")
                        .param("longitude", "-122.4194")
                        .param("days", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getWeatherForecastByCoordinates should return error when longitude is out of range")
    void getWeatherForecastByCoordinates_ShouldReturnError_WhenLongitudeIsOutOfRange() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/forecast/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "181.0")
                        .param("days", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getWeatherForecastByCoordinates should return error when days is out of range")
    void getWeatherForecastByCoordinates_ShouldReturnError_WhenDaysIsOutOfRange() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/forecast/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "-122.4194")
                        .param("days", "8")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getWeatherForecastByCoordinates should return error when service throws CustomException")
    void getWeatherForecastByCoordinates_ShouldReturnError_WhenServiceThrowsCustomException() throws Exception {
        // Arrange
        CustomException customException =
                new CustomException("Custom error", HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST);

        // Use doThrow for consistent behavior with primitives
        when(weatherService.getWeatherForecastByCoordinates(any(double.class), any(double.class), anyInt()))
                .thenThrow(customException);

        // Act & Assert
        mockMvc.perform(get("/api/weather/forecast/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "-122.4194")
                        .param("days", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Custom error"));
    }

    @Test
    @DisplayName("getWeatherForecastByCoordinates should return error when service throws exception")
    void getWeatherForecastByCoordinates_ShouldReturnError_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(weatherService.getWeatherForecastByCoordinates(any(double.class), any(double.class), anyInt()))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/weather/forecast/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "-122.4194")
                        .param("days", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to retrieve weather forecast"));
    }

    @Test
    @DisplayName("getGardenWeather should return garden-specific weather data for a location")
    void getGardenWeather_ShouldReturnGardenWeatherData() throws Exception {
        // Arrange
        doReturn(mockWeatherDTO).when(weatherService).getGardenWeather(anyString(), any(Optional.class));

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
    @DisplayName("getGardenWeather should return garden-specific weather data with garden plan ID")
    void getGardenWeather_ShouldReturnGardenWeatherData_WithGardenPlanId() throws Exception {
        // Arrange
        doReturn(mockWeatherDTO).when(weatherService).getGardenWeather(anyString(), any(Optional.class));

        // Act & Assert
        mockMvc.perform(get("/api/weather/garden")
                        .param("location", "San Francisco")
                        .param("gardenPlanId", "plan123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved garden weather information"))
                .andExpect(jsonPath("$.data.location").value("San Francisco"));
    }

    @Test
    @DisplayName("getGardenWeather should return error when location is blank")
    void getGardenWeather_ShouldReturnError_WhenLocationIsBlank() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/garden").param("location", "").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getGardenWeather should return error when service throws exception")
    void getGardenWeather_ShouldReturnError_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(weatherService.getGardenWeather(anyString(), any(Optional.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/weather/garden")
                        .param("location", "San Francisco")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to retrieve garden weather information"));
    }

    @Test
    @DisplayName("getGardenWeatherByCoordinates should return garden-specific weather data for coordinates")
    void getGardenWeatherByCoordinates_ShouldReturnGardenWeatherData() throws Exception {
        // Arrange
        doReturn(mockWeatherDTO)
                .when(weatherService)
                .getGardenWeatherByCoordinates(any(double.class), any(double.class), any(Optional.class));

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

    @Test
    @DisplayName("getGardenWeatherByCoordinates should return garden-specific weather data with garden plan ID")
    void getGardenWeatherByCoordinates_ShouldReturnGardenWeatherData_WithGardenPlanId() throws Exception {
        // Arrange
        doReturn(mockWeatherDTO)
                .when(weatherService)
                .getGardenWeatherByCoordinates(any(double.class), any(double.class), any(Optional.class));

        // Act & Assert
        mockMvc.perform(get("/api/weather/garden/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "-122.4194")
                        .param("gardenPlanId", "plan123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved garden weather information"))
                .andExpect(jsonPath("$.data.location").value("San Francisco"));
    }

    @Test
    @DisplayName("getGardenWeatherByCoordinates should return error when latitude is out of range")
    void getGardenWeatherByCoordinates_ShouldReturnError_WhenLatitudeIsOutOfRange() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/garden/coordinates")
                        .param("latitude", "91.0")
                        .param("longitude", "-122.4194")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getGardenWeatherByCoordinates should return error when longitude is out of range")
    void getGardenWeatherByCoordinates_ShouldReturnError_WhenLongitudeIsOutOfRange() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/garden/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "181.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getGardenWeatherByCoordinates should return error when service throws exception")
    void getGardenWeatherByCoordinates_ShouldReturnError_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(weatherService.getGardenWeatherByCoordinates(any(double.class), any(double.class), any(Optional.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/weather/garden/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "-122.4194")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to retrieve garden weather information"));
    }

    @Test
    @DisplayName("getWeatherHazards should return weather hazards data for a location")
    void getWeatherHazards_ShouldReturnWeatherHazardsData() throws Exception {
        // Arrange
        doReturn(mockWeatherDTO).when(weatherService).getGardenWeather(anyString(), any(Optional.class));

        // Act & Assert
        mockMvc.perform(get("/api/weather/hazards")
                        .param("location", "San Francisco")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved weather hazards"))
                .andExpect(jsonPath("$.data.location").value("San Francisco"));
    }

    @Test
    @DisplayName("getWeatherHazards should return error when location is blank")
    void getWeatherHazards_ShouldReturnError_WhenLocationIsBlank() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/hazards").param("location", "").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getWeatherHazards should return error when service throws exception")
    void getWeatherHazards_ShouldReturnError_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(weatherService.getGardenWeather(anyString(), any(Optional.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/weather/hazards")
                        .param("location", "San Francisco")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to retrieve weather hazards"));
    }

    @Test
    @DisplayName("getWeatherHazardsByCoordinates should return weather hazards data for coordinates")
    void getWeatherHazardsByCoordinates_ShouldReturnWeatherHazardsData() throws Exception {
        // Arrange
        doReturn(mockWeatherDTO)
                .when(weatherService)
                .getGardenWeatherByCoordinates(any(double.class), any(double.class), any(Optional.class));

        // Act & Assert
        mockMvc.perform(get("/api/weather/hazards/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "-122.4194")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully retrieved weather hazards"))
                .andExpect(jsonPath("$.data.location").value("San Francisco"));
    }

    @Test
    @DisplayName("getWeatherHazardsByCoordinates should return error when latitude is out of range")
    void getWeatherHazardsByCoordinates_ShouldReturnError_WhenLatitudeIsOutOfRange() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/hazards/coordinates")
                        .param("latitude", "91.0")
                        .param("longitude", "-122.4194")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getWeatherHazardsByCoordinates should return error when longitude is out of range")
    void getWeatherHazardsByCoordinates_ShouldReturnError_WhenLongitudeIsOutOfRange() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/hazards/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "181.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getWeatherHazardsByCoordinates should return error when service throws exception")
    void getWeatherHazardsByCoordinates_ShouldReturnError_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(weatherService.getGardenWeatherByCoordinates(any(double.class), any(double.class), any(Optional.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/weather/hazards/coordinates")
                        .param("latitude", "37.7749")
                        .param("longitude", "-122.4194")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to retrieve weather hazards"));
    }
}
