package dev.solace.twiggle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TwiggleApplication {

    /**
     * Serves as the entry point for the Twiggle Spring Boot application.
     *
     * @param args Command-line arguments passed to the application during startup
     * @see SpringApplication#run(Class, String...)
     */
    public static void main(String[] args) {
        SpringApplication.run(TwiggleApplication.class, args);
    }
}
