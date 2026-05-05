package com.hospital.security;

import com.hospital.entity.Utilisateur;
import com.hospital.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class SecurityDataInitializer implements CommandLineRunner {

    private final UtilisateurRepository repository;
    private final PasswordEncoder        passwordEncoder;

    public SecurityDataInitializer(UtilisateurRepository repository,
                                   PasswordEncoder passwordEncoder) {
        this.repository      = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        // ── Admin : accès total ──
        repository.save(Utilisateur.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(Utilisateur.Role.ROLE_ADMIN)
                .actif(true)
                .build());

        // ── RH : accès planning + lecture employés ──
        repository.save(Utilisateur.builder()
                .username("rh")
                .password(passwordEncoder.encode("rh123"))
                .role(Utilisateur.Role.ROLE_RH)
                .actif(true)
                .build());

        System.out.println("""
            ╔══════════════════════════════════════╗
            ║   Comptes de démo créés              ║
            ║   admin / admin123  → ROLE_ADMIN     ║
            ║   rh    / rh123     → ROLE_RH        ║
            ╚══════════════════════════════════════╝
            """);
    }
}