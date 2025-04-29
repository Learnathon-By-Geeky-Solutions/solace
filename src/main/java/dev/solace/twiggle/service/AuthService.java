package dev.solace.twiggle.service;

import dev.solace.twiggle.dto.AuthResponse;
import dev.solace.twiggle.model.AuthUser;
import dev.solace.twiggle.repository.AuthUserRepository;
import dev.solace.twiggle.security.JwtUtils;
import dev.solace.twiggle.supabase.SupabaseAdminClient;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SupabaseAdminClient supabaseAdminClient;
    private final AuthUserRepository authUserRepository;
    private final UserLoadingService userLoadingService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public void registerUser(String email, String password) {
        supabaseAdminClient.createUser(email, password);
    }

    public Authentication authentication(String email, String password) {

        UserDetails userDetails = userLoadingService.loadUserByUsername(email);

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid email ...");
        }
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid Password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public ResponseEntity<AuthResponse> loginUser(String email, String password) {
        Authentication authentication = authentication(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);
        String name = authentication.getName();
        AuthUser loggedUser = authUserRepository
                .findByEmail(name)
                .orElseThrow(() -> new BadCredentialsException("Invalid email ..."));
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        AuthResponse loginResponse = new AuthResponse(jwtToken, userDetails.getUsername(), loggedUser.getId(), roles);
        return ResponseEntity.ok(loginResponse);
    }

    public Optional<AuthUser> getUserByEmail(String email) {
        return authUserRepository.findByEmail(email);
    }
}
