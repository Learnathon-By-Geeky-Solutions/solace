package dev.solace.twiggle.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "supabase")
public class SupabaseConfig {
    @NotBlank
    private String serviceRoleKey;

    @NotBlank
    private String projectId;
}
