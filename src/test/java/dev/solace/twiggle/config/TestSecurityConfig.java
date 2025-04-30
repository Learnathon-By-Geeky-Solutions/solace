package dev.solace.twiggle.config;

import dev.solace.twiggle.security.JwtUtils;
import dev.solace.twiggle.service.UserLoadingService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/**")) // Configure CSRF to ignore all test endpoints
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }

    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils();
    }

    @Bean
    public UserLoadingService userLoadingService() {
        return new UserLoadingService(null) {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                // Return a mock user for testing
                return User.builder()
                        .username("test@example.com")
                        .password(new BCryptPasswordEncoder().encode("password"))
                        .roles("USER")
                        .build();
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
