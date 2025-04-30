package dev.solace.twiggle.supabase;

import dev.solace.twiggle.config.SupabaseConfig;
import dev.solace.twiggle.dto.supabase.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@ConditionalOnBean(SupabaseConfig.class)
public class SupabaseAdminClient {
    private final Logger log = LoggerFactory.getLogger(SupabaseAdminClient.class);
    private final WebClient webClient;

    public SupabaseAdminClient(SupabaseConfig supabaseConfig) {
        String baseUrl = String.format("https://%s.supabase.co/auth/v1/admin", supabaseConfig.getProjectId());
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("apikey", supabaseConfig.getServiceRoleKey())
                .defaultHeader("Authorization", "Bearer " + supabaseConfig.getServiceRoleKey())
                .build();
    }

    public void createUser(String email, String password) {
        webClient
                .post()
                .uri("/users")
                .bodyValue(new UserRequest(email, password))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("Error creating user: {}", error.getMessage(), error))
                .subscribe(response -> log.info("User created: {}", response));
    }

    public void updateUser(String userId, String newEmail, String newPassword) {
        webClient
                .put()
                .uri("/users/" + userId)
                .bodyValue(new UserRequest(newEmail, newPassword))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("Error updating user: {}", error.getMessage(), error))
                .subscribe(response -> log.info("User updated: {}", response));
    }
}
