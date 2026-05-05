package com.hospital.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class SecurityService {

    private final AuthenticationContext authContext;

    public SecurityService(AuthenticationContext authContext) {
        this.authContext = authContext;
    }

    public Optional<UserDetails> getUtilisateurConnecte() {
        return authContext.getAuthenticatedUser(UserDetails.class);
    }

    public String getUsername() {
        return getUtilisateurConnecte()
                .map(UserDetails::getUsername)
                .orElse("Inconnu");
    }

    public boolean isAdmin() {
        return getUtilisateurConnecte()
                .map(u -> u.getAuthorities().stream()
                        .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN")))
                .orElse(false);
    }

    public void logout() {
        authContext.logout();
    }
}