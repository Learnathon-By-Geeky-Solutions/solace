package dev.solace.twiggle.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "supabase")
@ConditionalOnProperty(
        prefix = "supabase",
        name = {"projectId", "serviceRoleKey"})
public class SupabaseConfig {
    @NotBlank
    private String serviceRoleKey;

    @NotBlank
    private String projectId;
}
