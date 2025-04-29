package dev.solace.twiggle.service;

import dev.solace.twiggle.model.AuthUser;
import dev.solace.twiggle.repository.AuthUserRepository;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserLoadingService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;

    public UserLoadingService(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser user = authUserRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + username));

        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getRole());

        List<GrantedAuthority> authorityList = List.of(grantedAuthority);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getEncryptedPassword(), authorityList);
    }
}
