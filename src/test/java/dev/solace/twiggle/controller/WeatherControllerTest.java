package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.solace.twiggle.config.RateLimiterConfiguration;
import dev.solace.twiggle.dto.weather.CurrentWeatherResponse;
import dev.solace.twiggle.dto.weather.ForecastResponse;
import dev.solace.twiggle.dto.weather.WeatherDataPoint;
import dev.solace.twiggle.service.WeatherService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WeatherController.class)
@Import({RateLimiterConfiguration.class})
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    private CurrentWeatherResponse mockCurrentWeather;
    private ForecastResponse mockForecast;

    @BeforeEach
    void setUp() {
        // Set up mock current weather data
        mockCurrentWeather = new CurrentWeatherResponse();
        mockCurrentWeather.setTemperature(25.5);
        mockCurrentWeather.setHumidity(65);
        mockCurrentWeather.setWindSpeed(10.2);
        mockCurrentWeather.setPrecipitation(0.0);
        mockCurrentWeather.setCondition("Clear");
        mockCurrentWeather.setIcon("sunny");

        // Set up mock forecast data
        WeatherDataPoint dataPoint1 = new WeatherDataPoint();
        dataPoint1.setDate(LocalDate.now().plusDays(1));
        dataPoint1.setTemperatureHigh(28.0);
        dataPoint1.setTemperatureLow(18.0);
        dataPoint1.setHumidity(60);
        dataPoint1.setPrecipitationChance(10);
        dataPoint1.setCondition("Partly Cloudy");
        dataPoint1.setIcon("partly-cloudy");

        WeatherDataPoint dataPoint2 = new WeatherDataPoint();
        dataPoint2.setDate(LocalDate.now().plusDays(2));
        dataPoint2.setTemperatureHigh(30.0);
        dataPoint2.setTemperatureLow(20.0);
        dataPoint2.setHumidity(55);
        dataPoint2.setPrecipitationChance(5);
        dataPoint2.setCondition("Sunny");
        dataPoint2.setIcon("sunny");

        mockForecast = new ForecastResponse();
        mockForecast.setForecast(List.of(dataPoint1, dataPoint2));
    }

    @Test
    void getCurrentWeather_ShouldReturnWeatherData() throws Exception {
        // Arrange
        when(weatherService.getCurrentWeather(anyDouble(), anyDouble())).thenReturn(mockCurrentWeather);

        // Act & Assert
        mockMvc.perform(get("/api/v1/weather/current")
                        .param("latitude", "37.7749")
                        .param("longitude", "-122.4194")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Current weather retrieved successfully"))
                .andExpect(jsonPath("$.data.temperature").value(25.5))
                .andExpect(jsonPath("$.data.humidity").value(65))
                .andExpect(jsonPath("$.data.condition").value("Clear"));
    }

    @Test
    void getForecast_ShouldReturnForecastData() throws Exception {
        // Arrange
        when(weatherService.getWeatherForecast(anyDouble(), anyDouble(), any())).thenReturn(mockForecast);

        // Act & Assert
        mockMvc.perform(get("/api/v1/weather/forecast")
                        .param("latitude", "37.7749")
                        .param("longitude", "-122.4194")
                        .param("days", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Weather forecast retrieved successfully"))
                .andExpect(jsonPath("$.data.forecast[0].temperatureHigh").value(28.0))
                .andExpect(jsonPath("$.data.forecast[1].temperatureHigh").value(30.0));
    }

    @Test
    void getCurrentWeather_WithMissingParameters_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/weather/current").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getForecast_WithMissingParameters_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/weather/forecast").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
