package dev.solace.twiggle.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = AppConfig.class)
class AppConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void restTemplate_shouldBeCreated() {
        // When
        RestTemplate restTemplate = context.getBean(RestTemplate.class);

        // Then
        assertThat(restTemplate).isNotNull();
    }
}
