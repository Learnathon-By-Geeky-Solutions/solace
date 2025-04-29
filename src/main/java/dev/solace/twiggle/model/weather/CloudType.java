package dev.solace.twiggle.model.weather;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Enum representing cloud types based on cloud cover percentage.
 */
public enum CloudType {
    CLEAR("Clear", cloudCover -> cloudCover < 20),
    CUMULUS("Cumulus", cloudCover -> cloudCover >= 20 && cloudCover < 50),
    STRATOCUMULUS("Stratocumulus", cloudCover -> cloudCover >= 50 && cloudCover < 80),
    STRATUS("Stratus", cloudCover -> cloudCover >= 80);

    private final String displayName;
    private final Predicate<Integer> matcher;

    CloudType(String displayName, Predicate<Integer> matcher) {
        this.displayName = displayName;
        this.matcher = matcher;
    }

    /**
     * Get the cloud type based on cloud cover percentage
     *
     * @param cloudCover The cloud cover percentage (0-100)
     * @return The corresponding CloudType
     */
    public static CloudType fromCloudCover(int cloudCover) {
        return Arrays.stream(values())
                .filter(type -> type.matcher.test(cloudCover))
                .findFirst()
                .orElse(CLEAR);
    }

    /**
     * Get the display name for this cloud type
     *
     * @return The display name (e.g., "Clear", "Cumulus", etc.)
     */
    public String getDisplayName() {
        return displayName;
    }
}
