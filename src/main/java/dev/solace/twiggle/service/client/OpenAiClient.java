package dev.solace.twiggle.service.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OpenAiClient {
    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);
    private static final String CONTENT = "content";

    private final WebClient openaiWebClient;

    public OpenAiClient(WebClient openaiWebClient) {
        this.openaiWebClient = openaiWebClient;
    }

    public String fetchRecommendations(String systemPrompt, String userPrompt) {
        Map<String, Object> openAiRequest = new HashMap<>();
        openAiRequest.put("model", "gpt-4o-mini");

        List<Map<String, Object>> messages =
                List.of(Map.of("role", "system", CONTENT, systemPrompt), Map.of("role", "user", CONTENT, userPrompt));

        openAiRequest.put("messages", messages);

        log.debug("Sending request to OpenAI API");
        return openaiWebClient
                .post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(openAiRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
