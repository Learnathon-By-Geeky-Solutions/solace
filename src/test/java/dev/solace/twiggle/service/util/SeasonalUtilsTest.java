package dev.solace.twiggle.service.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Calendar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeasonalUtilsTest {

    private SeasonalUtils seasonalUtils;

    @BeforeEach
    void setUp() {
        seasonalUtils = new SeasonalUtils();
    }

    @Test
    void getCurrentSeason_ShouldReturnCorrectSeasons() {
        try (MockedStatic<Calendar> calendarMock = mockStatic(Calendar.class)) {
            Calendar mockCalendar = mock(Calendar.class);
            calendarMock.when(Calendar::getInstance).thenReturn(mockCalendar);

            when(mockCalendar.get(Calendar.MONTH)).thenReturn(Calendar.JANUARY);
            assertEquals("winter", seasonalUtils.getCurrentSeason("US"));
            assertEquals("summer", seasonalUtils.getCurrentSeason("Australia"));

            when(mockCalendar.get(Calendar.MONTH)).thenReturn(Calendar.APRIL);
            assertEquals("spring", seasonalUtils.getCurrentSeason("US"));
            assertEquals("autumn", seasonalUtils.getCurrentSeason("Australia"));

            when(mockCalendar.get(Calendar.MONTH)).thenReturn(Calendar.JULY);
            assertEquals("summer", seasonalUtils.getCurrentSeason("US"));
            assertEquals("winter", seasonalUtils.getCurrentSeason("Australia"));

            when(mockCalendar.get(Calendar.MONTH)).thenReturn(Calendar.OCTOBER);
            assertEquals("autumn", seasonalUtils.getCurrentSeason("US"));
            assertEquals("spring", seasonalUtils.getCurrentSeason("Australia"));

            when(mockCalendar.get(Calendar.MONTH)).thenReturn(Calendar.DECEMBER);
            assertEquals("winter", seasonalUtils.getCurrentSeason("US"));
            assertEquals("summer", seasonalUtils.getCurrentSeason("Australia"));
        }
    }

    @Test
    void isSouthernHemisphereCountry_ShouldIdentifyAllSouthernCountries() {
        String[] southernCountries = {"australia", "new zealand", "argentina", "chile", "south africa", "brazil"};
        String[] northernCountries = {"usa", "canada", "uk", "france", "germany", "japan"};

        for (String country : southernCountries) {
            assertTrue(
                    seasonalUtils.isSouthernHemisphereCountry(country),
                    "Should identify " + country + " as southern hemisphere");
        }

        for (String country : northernCountries) {
            assertFalse(
                    seasonalUtils.isSouthernHemisphereCountry(country),
                    "Should not identify " + country + " as southern hemisphere");
        }
    }

    @Test
    void isSouthernHemisphereCountry_ShouldHandleNullLocation() {
        assertFalse(seasonalUtils.isSouthernHemisphereCountry(null));
    }
}
