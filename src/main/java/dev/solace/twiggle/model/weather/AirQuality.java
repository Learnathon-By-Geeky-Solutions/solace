package dev.solace.twiggle.model.weather;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enum representing air quality levels.
 */
public enum AirQuality {
    GOOD(1, "Good"),
    MODERATE(2, "Moderate"),
    UNHEALTHY_SENSITIVE(3, "Unhealthy for Sensitive Groups"),
    UNHEALTHY(4, "Unhealthy"),
    VERY_UNHEALTHY(5, "Very Unhealthy"),
    HAZARDOUS(6, "Hazardous");

    private final int epaIndex;
    private final String displayName;

    private static final Map<Integer, AirQuality> BY_INDEX =
            Arrays.stream(values()).collect(Collectors.toMap(a -> a.epaIndex, a -> a));

    private static final Map<String, AirQuality> BY_NAME =
            Arrays.stream(values()).collect(Collectors.toMap(a -> a.displayName, a -> a));

    AirQuality(int epaIndex, String displayName) {
        this.epaIndex = epaIndex;
        this.displayName = displayName;
    }

    /**
     * Create an AirQuality enum from an EPA index
     *
     * @param index The EPA air quality index (1-6)
     * @return The corresponding AirQuality enum value, defaults to MODERATE if not found
     */
    public static AirQuality fromEpa(int index) {
        return BY_INDEX.getOrDefault(index, MODERATE);
    }

    /**
     * Get the EPA index for this air quality level
     *
     * @return The EPA index (1-6)
     */
    public int getEpaIndex() {
        return epaIndex;
    }

    /**
     * Get the display name for this air quality level
     *
     * @return The display name (e.g., "Good", "Moderate", etc.)
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if this air quality level is unhealthy
     *
     * @return true if the air quality is UNHEALTHY or worse
     */
    public boolean isUnhealthy() {
        return this.epaIndex >= UNHEALTHY.epaIndex;
    }

    /**
     * Check if this air quality level is unhealthy for sensitive groups
     *
     * @return true if the air quality is UNHEALTHY_SENSITIVE or worse
     */
    public boolean isUnhealthyForSensitive() {
        return this.epaIndex >= UNHEALTHY_SENSITIVE.epaIndex;
    }
}
