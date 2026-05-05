package com.hospital.security;

import com.hospital.repository.UtilisateurRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityHospitalUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository repository;

    public CityHospitalUserDetailsService(UtilisateurRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        var utilisateur = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur introuvable : " + username));

        if (!utilisateur.isActif()) {
            throw new UsernameNotFoundException("Compte désactivé : " + username);
        }

        return new User(
                utilisateur.getUsername(),
                utilisateur.getPassword(),
                List.of(new SimpleGrantedAuthority(utilisateur.getRole().name()))
        );
    }
}