package dev.solace.twiggle;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TwiggleApplicationTests {

    @Test
    void contextLoads() {
        /* This test method is intentionally empty because it only verifies that the
         * Spring application context loads successfully without any errors.
         * If the context fails to load, the test will fail automatically.
         */
    }

    @Test
    void mainMethodStartsApplication() {
        try (MockedStatic<SpringApplication> mockedSpringApplication = Mockito.mockStatic(SpringApplication.class)) {
            // Arrange
            String[] args = new String[] {};

            // Act
            TwiggleApplication.main(args);

            // Assert
            mockedSpringApplication.verify(() -> SpringApplication.run(TwiggleApplication.class, args));
        }
    }
}
