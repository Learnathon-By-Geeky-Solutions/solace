package dev.solace.twiggle.service.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class OpenAiClientTest {

    @Mock
    private WebClient openaiWebClient;

    private OpenAiClient openAiClient;

    @BeforeEach
    void setUp() {
        openAiClient = new OpenAiClient(openaiWebClient);
    }

    @Test
    void fetchRecommendations_ShouldMakeCorrectApiCall() {
        // Arrange
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(openaiWebClient.post()).thenReturn(requestBodyUriSpec);
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri("/chat/completions");
        doReturn(requestBodySpec).when(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{\"result\":\"success\"}"));

        String systemPrompt = "You are a gardening assistant";
        String userPrompt = "What should I plant?";

        // Act
        String result = openAiClient.fetchRecommendations(systemPrompt, userPrompt);

        // Assert
        assertNotNull(result);
        assertEquals("{\"result\":\"success\"}", result);

        // Verify
        verify(openaiWebClient).post();
        verify(requestBodyUriSpec).uri("/chat/completions");
        verify(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
        verify(requestBodySpec).bodyValue(any());
        verify(requestBodySpec).retrieve();
        verify(responseSpec).bodyToMono(String.class);
    }
}
