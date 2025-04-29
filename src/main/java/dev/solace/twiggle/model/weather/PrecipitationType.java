package dev.solace.twiggle.model.weather;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Enum representing precipitation types based on temperature.
 */
public enum PrecipitationType {
    SNOW("Snow", temp -> temp < 0),
    SLEET("Sleet", temp -> temp >= 0 && temp < 4),
    RAIN("Rain", temp -> temp >= 4);

    private final String displayName;
    private final Predicate<Double> matcher;

    PrecipitationType(String displayName, Predicate<Double> matcher) {
        this.displayName = displayName;
        this.matcher = matcher;
    }

    /**
     * Get the precipitation type based on temperature
     *
     * @param temperature The temperature in Celsius
     * @return The corresponding PrecipitationType
     */
    public static PrecipitationType fromTemperature(double temperature) {
        return Arrays.stream(values())
                .filter(type -> type.matcher.test(temperature))
                .findFirst()
                .orElse(RAIN);
    }

    /**
     * Get the display name for this precipitation type
     *
     * @return The display name (e.g., "Snow", "Sleet", etc.)
     */
    public String getDisplayName() {
        return displayName;
    }
}
