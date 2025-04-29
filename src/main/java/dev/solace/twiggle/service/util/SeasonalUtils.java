package dev.solace.twiggle.service.util;

import java.util.Calendar;
import org.springframework.stereotype.Component;

@Component
public class SeasonalUtils {
    private static final String SUMMER = "summer";
    private static final String WINTER = "winter";
    private static final String SPRING = "spring";
    private static final String AUTUMN = "autumn";

    public String getCurrentSeason(String location) {
        int month = Calendar.getInstance().get(Calendar.MONTH);
        boolean isSouthern = location != null && isSouthernHemisphereCountry(location.toLowerCase());

        return switch (month) {
            case 0, 1 -> isSouthern ? SUMMER : WINTER; // January and February
            case 2, 3, 4 -> isSouthern ? AUTUMN : SPRING;
            case 5, 6, 7 -> isSouthern ? WINTER : SUMMER;
            case 8, 9, 10 -> isSouthern ? SPRING : AUTUMN;
            default -> isSouthern ? SUMMER : WINTER; // December (month 11)
        };
    }

    public boolean isSouthernHemisphereCountry(String location) {
        if (location == null) {
            return false;
        }

        String[] southernCountries = {"australia", "new zealand", "argentina", "chile", "south africa", "brazil"};

        for (String country : southernCountries) {
            if (location.contains(country)) {
                return true;
            }
        }

        return false;
    }
}
