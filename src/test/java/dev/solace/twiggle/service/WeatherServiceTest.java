package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.solace.twiggle.dto.weather.CurrentWeatherResponse;
import dev.solace.twiggle.dto.weather.ForecastResponse;
import dev.solace.twiggle.dto.weather.WeatherDataPoint;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private ResponseSpec responseSpec;

    @InjectMocks
    private WeatherService weatherService;

    private final double latitude = 37.7749;
    private final double longitude = -122.4194;
    private Map<String, Object> currentWeatherResponse;
    private Map<String, Object> forecastResponse;

    @BeforeEach
    void setUp() {
        // Set up mock response for current weather
        currentWeatherResponse = new HashMap<>();
        Map<String, Object> current = new HashMap<>();
        current.put("temperature", 25.5);
        current.put("humidity", 65);
        current.put("wind_speed", 10.2);
        current.put("precipitation", 0.0);

        Map<String, Object> condition = new HashMap<>();
        condition.put("text", "Clear");
        condition.put("icon", "sunny");
        current.put("condition", condition);

        currentWeatherResponse.put("current", current);

        // Set up mock response for forecast
        forecastResponse = new HashMap<>();
        Map<String, Object> forecastDay1 = new HashMap<>();
        Map<String, Object> day1 = new HashMap<>();
        day1.put("maxtemp_c", 28.0);
        day1.put("mintemp_c", 18.0);
        day1.put("avghumidity", 60);
        day1.put("daily_chance_of_rain", 10);

        Map<String, Object> condition1 = new HashMap<>();
        condition1.put("text", "Partly Cloudy");
        condition1.put("icon", "partly-cloudy");
        day1.put("condition", condition1);

        forecastDay1.put("date", LocalDate.now().plusDays(1).toString());
        forecastDay1.put("day", day1);

        Map<String, Object> forecastDay2 = new HashMap<>();
        Map<String, Object> day2 = new HashMap<>();
        day2.put("maxtemp_c", 30.0);
        day2.put("mintemp_c", 20.0);
        day2.put("avghumidity", 55);
        day2.put("daily_chance_of_rain", 5);

        Map<String, Object> condition2 = new HashMap<>();
        condition2.put("text", "Sunny");
        condition2.put("icon", "sunny");
        day2.put("condition", condition2);

        forecastDay2.put("date", LocalDate.now().plusDays(2).toString());
        forecastDay2.put("day", day2);

        Map<String, Object> forecast = new HashMap<>();
        forecast.put("forecastday", List.of(forecastDay1, forecastDay2));
        forecastResponse.put("forecast", forecast);

        // Set up WebClient mock
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeaders(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyMap())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void getCurrentWeather_ShouldReturnCurrentWeatherData() {
        // Arrange
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(currentWeatherResponse));

        // Act
        CurrentWeatherResponse result = weatherService.getCurrentWeather(latitude, longitude);

        // Assert
        assertNotNull(result);
        assertEquals(25.5, result.getTemperature());
        assertEquals(65, result.getHumidity());
        assertEquals(10.2, result.getWindSpeed());
        assertEquals(0.0, result.getPrecipitation());
        assertEquals("Clear", result.getCondition());
        assertEquals("sunny", result.getIcon());

        verify(requestHeadersUriSpec, times(1)).uri(anyString(), anyMap());
    }

    @Test
    void getWeatherForecast_ShouldReturnForecastData() {
        // Arrange
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(forecastResponse));

        // Act
        ForecastResponse result = weatherService.getWeatherForecast(latitude, longitude, 2);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getForecast());
        assertEquals(2, result.getForecast().size());

        WeatherDataPoint day1 = result.getForecast().get(0);
        assertEquals(28.0, day1.getTemperatureHigh());
        assertEquals(18.0, day1.getTemperatureLow());
        assertEquals(60, day1.getHumidity());
        assertEquals(10, day1.getPrecipitationChance());
        assertEquals("Partly Cloudy", day1.getCondition());
        assertEquals("partly-cloudy", day1.getIcon());

        WeatherDataPoint day2 = result.getForecast().get(1);
        assertEquals(30.0, day2.getTemperatureHigh());
        assertEquals(20.0, day2.getTemperatureLow());
        assertEquals(55, day2.getHumidity());
        assertEquals(5, day2.getPrecipitationChance());
        assertEquals("Sunny", day2.getCondition());
        assertEquals("sunny", day2.getIcon());

        verify(requestHeadersUriSpec, times(1)).uri(anyString(), anyMap());
    }
}
