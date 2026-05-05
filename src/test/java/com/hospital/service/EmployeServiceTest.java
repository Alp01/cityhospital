package com.hospital.service;

import com.hospital.entity.Employe;
import com.hospital.enums.Poste;
import com.hospital.enums.Statut;
import com.hospital.repository.EmployeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeService — tests unitaires")
class EmployeServiceTest {

    @Mock
    private EmployeRepository repository;

    @InjectMocks
    private EmployeService employeService;

    private Employe employe;

    @BeforeEach
    void setUp() {
        employe = new Employe();
        employe.setId(1L);
        employe.setNom("Shepherd");
        employe.setPrenom("Derek");
        employe.setEmail("derek.shepherd@seattlegrace.com");
        employe.setPoste(Poste.MEDECIN);
        employe.setStatut(Statut.ACTIF);
    }

    // ──────────────────────────────────────────────────────────
    // findAll
    // ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("Retourne tous les employés")
        void findAll_retourneListe() {
            when(repository.findAllWithService()).thenReturn(List.of(employe));

            List<Employe> result = employeService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getNom()).isEqualTo("Shepherd");
            verify(repository).findAllWithService();
        }

        @Test
        @DisplayName("Retourne une liste vide si aucun employé")
        void findAll_listeVide() {
            when(repository.findAllWithService()).thenReturn(List.of());

            assertThat(employeService.findAll()).isEmpty();
        }
    }

    // ──────────────────────────────────────────────────────────
    // rechercher
    // ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("rechercher()")
    class Rechercher {

        @Test
        @DisplayName("Retourne tous les employés si terme vide")
        void rechercher_termeVide_retourneTous() {
            when(repository.findAllWithService()).thenReturn(List.of(employe));

            assertThat(employeService.rechercher("")).hasSize(1);
            assertThat(employeService.rechercher(null)).hasSize(1);
            assertThat(employeService.rechercher("   ")).hasSize(1);
        }

        @Test
        @DisplayName("Délègue au repository si terme non vide")
        void rechercher_avecTerme_delegueRepository() {
            when(repository.rechercher("derek")).thenReturn(List.of(employe));

            List<Employe> result = employeService.rechercher("derek");

            assertThat(result).hasSize(1);
            verify(repository).rechercher("derek");
        }
    }

    // ──────────────────────────────────────────────────────────
    // sauvegarder / supprimer
    // ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("sauvegarder() / supprimer()")
    class SauvegarderSupprimer {

        @Test
        @DisplayName("Sauvegarde et retourne l'employé")
        void sauvegarder_ok() {
            when(repository.save(employe)).thenReturn(employe);

            Employe result = employeService.sauvegarder(employe);

            assertThat(result.getEmail()).isEqualTo("derek.shepherd@seattlegrace.com");
            verify(repository).save(employe);
        }

        @Test
        @DisplayName("Supprime l'employé")
        void supprimer_ok() {
            employeService.supprimer(employe);
            verify(repository, times(1)).delete(employe);
        }
    }

    // ──────────────────────────────────────────────────────────
    // countActifs / countEnConge
    // ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Comptages")
    class Comptages {

        @Test
        @DisplayName("countActifs() délègue au repository")
        void countActifs_ok() {
            when(repository.countByStatut(Statut.ACTIF)).thenReturn(8L);
            assertThat(employeService.countActifs()).isEqualTo(8L);
        }

        @Test
        @DisplayName("countEnConge() délègue au repository")
        void countEnConge_ok() {
            when(repository.countByStatut(Statut.CONGE)).thenReturn(2L);
            assertThat(employeService.countEnConge()).isEqualTo(2L);
        }
    }

    // ──────────────────────────────────────────────────────────
    // Méthode métier de l'entité
    // ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Employe — méthodes métier")
    class EmployeMetier {

        @Test
        @DisplayName("getNomComplet() concatène prénom + nom")
        void getNomComplet_ok() {
            assertThat(employe.getNomComplet()).isEqualTo("Derek Shepherd");
        }
    }
}