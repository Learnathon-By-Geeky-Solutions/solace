package dev.solace.twiggle;

import dev.solace.twiggle.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
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
