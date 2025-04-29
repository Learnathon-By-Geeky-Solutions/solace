package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.config.WeatherThresholds;
import dev.solace.twiggle.dto.WeatherDTO;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.mapper.WeatherJsonMapper;
import dev.solace.twiggle.service.impl.GardeningAdviceAdvisor;
import dev.solace.twiggle.service.impl.PlantHazardAdvisor;
import dev.solace.twiggle.service.impl.WeatherServiceImpl;
import dev.solace.twiggle.service.impl.WorldWeatherOnlineFacade;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest { // Rename class later if desired (e.g., WeatherServiceImplTest)

    // Mock the dependencies of WeatherServiceImpl
    @Mock
    private WorldWeatherOnlineFacade facade;

    @Mock
    private WeatherJsonMapper mapper;

    @Mock
    private PlantHazardAdvisor plantAdvisor;

    @Mock
    private GardeningAdviceAdvisor gardenAdvisor;

    @Mock
    private WeatherThresholds thresholds;

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

    private WeatherDTO mockWeatherDTO;

    @BeforeEach
    void setUp() {
        // Create a simple mock DTO
        mockWeatherDTO = WeatherDTO.builder()
                .location(location)
                .temperature(15.0)
                .temperatureUnit("Celsius")
                .build();

        // Setup mocks - use lenient() to avoid "unnecessary stubbing" errors
        lenient().when(thresholds.getGardenWeatherForecastDays()).thenReturn(3);
        lenient().when(facade.fetch(anyString(), anyInt())).thenReturn(mockApiResponse);
        lenient()
                .when(facade.fetchByCoordinates(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(mockApiResponse);
        lenient().when(mapper.toDto(anyString(), anyInt(), anyString())).thenReturn(mockWeatherDTO);

        List<String> plantHazards = new ArrayList<>();
        plantHazards.add("Test plant hazard");
        lenient().when(plantAdvisor.hazardsFor(any(WeatherDTO.class))).thenReturn(plantHazards);

        // Mock the enrichment behavior
        lenient()
                .doAnswer(invocation -> {
                    WeatherDTO dto = invocation.getArgument(0);
                    dto.setGardeningAdvice("Test gardening advice");
                    return null;
                })
                .when(gardenAdvisor)
                .enrich(any(WeatherDTO.class));
    }

    @Test
    void getCurrentWeather_ShouldCallApiClientAndParse() {
        // Act
        WeatherDTO result = weatherService.getCurrentWeather(location);

        // Assert
        assertNotNull(result);
        assertEquals(location, result.getLocation());
        verify(facade).fetch(location, 1);
        verify(mapper).toDto(mockApiResponse, 1, location);
    }

    @Test
    void getCurrentWeatherByCoordinates_ShouldCallApiClientAndParse() {
        // Act
        WeatherDTO result = weatherService.getCurrentWeatherByCoordinates(latitude, longitude);

        // Assert
        assertNotNull(result);
        verify(facade).fetchByCoordinates(latitude, longitude, 1);
    }

    @Test
    void getWeatherForecast_ShouldCallApiClientAndParse() {
        // Act
        WeatherDTO result = weatherService.getWeatherForecast(location, days);

        // Assert
        assertNotNull(result);
        verify(facade).fetch(location, days);
        verify(mapper).toDto(mockApiResponse, days, location);
    }

    @Test
    void getWeatherForecastByCoordinates_ShouldCallApiClientAndParse() {
        // Act
        WeatherDTO result = weatherService.getWeatherForecastByCoordinates(latitude, longitude, days);

        // Assert
        assertNotNull(result);
        verify(facade).fetchByCoordinates(latitude, longitude, days);
    }

    @Test
    void getGardenWeather_ShouldCallApiClientAndParse() {
        // Act
        WeatherDTO result = weatherService.getGardenWeather(location, gardenPlanId);

        // Assert
        assertNotNull(result);
        verify(facade).fetch(location, thresholds.getGardenWeatherForecastDays());
        verify(gardenAdvisor).enrich(any(WeatherDTO.class));
    }

    @Test
    void getGardenWeatherByCoordinates_ShouldCallApiClientAndParse() {
        // Act
        WeatherDTO result = weatherService.getGardenWeatherByCoordinates(latitude, longitude, gardenPlanId);

        // Assert
        assertNotNull(result);
        verify(facade).fetchByCoordinates(latitude, longitude, thresholds.getGardenWeatherForecastDays());
        verify(gardenAdvisor).enrich(any(WeatherDTO.class));
    }

    @Test
    void getCurrentWeather_WhenApiThrowsException_ShouldThrowCustomException() {
        // Arrange - must be done at test time, not in setup
        when(facade.fetch(anyString(), anyInt()))
                .thenThrow(new CustomException("API Error", HttpStatus.INTERNAL_SERVER_ERROR));

        // Act & Assert
        assertThrows(CustomException.class, () -> weatherService.getCurrentWeather(location));
    }

    // Add similar exception tests for other methods...
}
