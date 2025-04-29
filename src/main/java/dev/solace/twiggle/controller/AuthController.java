package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.AuthDTO;
import dev.solace.twiggle.dto.AuthResponse;
import dev.solace.twiggle.service.AuthService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RateLimiter(name = "standard-api")
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthDTO authDTO) {
        try {
            return authService.loginUser(authDTO.getEmail(), authDTO.getPassword());
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody AuthDTO authDTO) {
        authService.registerUser(authDTO.getEmail(), authDTO.getPassword());
        return ResponseEntity.ok("Registration successful");
    }
}
