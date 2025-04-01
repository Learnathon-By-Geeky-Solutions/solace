package dev.solace.twiggle;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(properties = {
    "spring.data.mongodb.uri=mongodb://localhost:27017/test",
    "spring.mongodb.embedded.version=4.0.21"
})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
    "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration"
})
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
