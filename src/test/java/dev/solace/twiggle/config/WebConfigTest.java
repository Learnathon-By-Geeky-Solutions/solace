package dev.solace.twiggle.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private CorsRegistry corsRegistry;

    @Mock
    private CorsRegistration corsRegistration;

    @InjectMocks
    private WebConfig webConfig;

    @Captor
    private ArgumentCaptor<String> pathPatternCaptor;

    @Captor
    private ArgumentCaptor<String[]> originCaptor;

    @Captor
    private ArgumentCaptor<String[]> methodCaptor;

    @Test
    void addCorsMappings_shouldConfigureCorsCorrectly() {
        // Given
        String[] testOrigins = new String[] {"http://localhost:3000", "https://twiggle.tech", "https://*.vercel.app"};
        ReflectionTestUtils.setField(webConfig, "allowedOrigins", testOrigins);

        when(corsRegistry.addMapping(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.allowedOriginPatterns(testOrigins)).thenReturn(corsRegistration);
        when(corsRegistration.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS"))
                .thenReturn(corsRegistration);
        when(corsRegistration.allowedHeaders("*")).thenReturn(corsRegistration);
        when(corsRegistration.allowCredentials(true)).thenReturn(corsRegistration);
        when(corsRegistration.maxAge(3600)).thenReturn(corsRegistration);

        // When
        webConfig.addCorsMappings(corsRegistry);

        // Then
        verify(corsRegistry).addMapping("/api/**");
        verify(corsRegistration).allowedOriginPatterns(testOrigins);
        verify(corsRegistration).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
        verify(corsRegistration).allowedHeaders("*");
        verify(corsRegistration).allowCredentials(true);
        verify(corsRegistration).maxAge(3600);
    }
}
