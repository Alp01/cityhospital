package com.hospital.service;

import com.hospital.entity.Conge;
import com.hospital.entity.Employe;
import com.hospital.enums.Statut;
import com.hospital.repository.CongeRepository;
import com.hospital.repository.EmployeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanningService — tests unitaires")
class PlanningServiceTest {

    @Mock
    private CongeRepository congeRepository;

    @Mock
    private EmployeRepository employeRepository;

    @InjectMocks
    private PlanningService planningService;

    private Employe employe;
    private Conge conge;

    @BeforeEach
    void setUp() {
        employe = new Employe();
        employe.setId(1L);
        employe.setNom("Grey");
        employe.setPrenom("Meredith");
        employe.setEmail("meredith.grey@seattlegrace.com");
        employe.setStatut(Statut.ACTIF);

        conge = new Conge();
        conge.setEmploye(employe);
        conge.setType(Conge.TypeConge.CONGE_ANNUEL);
        conge.setDateDebut(LocalDate.of(2026, 6, 1));
        conge.setDateFin(LocalDate.of(2026, 6, 15));
        conge.setStatut(Conge.StatutConge.EN_ATTENTE);
    }

    // ──────────────────────────────────────────────────────────
    // Sauvegarde
    // ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("sauvegarder()")
    class Sauvegarder {

        @Test
        @DisplayName("Sauvegarde un congé valide sans chevauchement")
        void sauvegarder_congeValide_ok() {
            // GIVEN — aucun chevauchement trouvé
            when(congeRepository.findChevauchements(
                    eq(1L), any(), any(), isNull()))
                .thenReturn(Collections.emptyList());
            when(congeRepository.save(conge)).thenReturn(conge);

            // WHEN
            Conge result = planningService.sauvegarder(conge);

            // THEN
            assertThat(result).isNotNull();
            verify(congeRepository, times(1)).save(conge);
        }

        @Test
        @DisplayName("Lève une exception si les dates se chevauchent")
        void sauvegarder_avecChevauchement_leveException() {
            // GIVEN — un congé existant chevauche les dates
            var congeExistant = new Conge();
            congeExistant.setId(99L);
            when(congeRepository.findChevauchements(
                    eq(1L), any(), any(), isNull()))
                .thenReturn(List.of(congeExistant));

            // WHEN + THEN
            assertThatThrownBy(() -> planningService.sauvegarder(conge))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("déjà un congé sur cette période");

            verify(congeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lève une exception si date de fin avant date de début")
        void sauvegarder_dateFin_avantDebut_leveException() {
            // GIVEN
            conge.setDateDebut(LocalDate.of(2026, 6, 15));
            conge.setDateFin(LocalDate.of(2026, 6, 1));   // fin avant début

            // WHEN + THEN
            assertThatThrownBy(() -> planningService.sauvegarder(conge))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("date de fin doit être après");

            verify(congeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lève une exception si dates nulles")
        void sauvegarder_datesNulles_leveException() {
            // GIVEN
            conge.setDateDebut(null);
            conge.setDateFin(null);

            // WHEN + THEN
            assertThatThrownBy(() -> planningService.sauvegarder(conge))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatoires");
        }
    }

    // ──────────────────────────────────────────────────────────
    // Approbation
    // ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("approuver()")
    class Approuver {

        @Test
        @DisplayName("Approuve un congé EN_ATTENTE")
        void approuver_congeEnAttente_ok() {
            // GIVEN
            conge.setId(1L);
            when(congeRepository.findById(1L)).thenReturn(Optional.of(conge));
            when(congeRepository.save(any())).thenReturn(conge);

            // WHEN
            Conge result = planningService.approuver(1L, "OK validé par RH");

            // THEN
            assertThat(result.getStatut()).isEqualTo(Conge.StatutConge.APPROUVE);
            assertThat(result.getCommentaireRh()).isEqualTo("OK validé par RH");
            verify(congeRepository).save(conge);
        }

        @Test
        @DisplayName("Lève une exception si le congé est introuvable")
        void approuver_congeIntrouvable_leveException() {
            // GIVEN
            when(congeRepository.findById(999L)).thenReturn(Optional.empty());

            // WHEN + THEN
            assertThatThrownBy(() -> planningService.approuver(999L, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Congé introuvable");
        }
    }

    // ──────────────────────────────────────────────────────────
    // Refus
    // ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("refuser()")
    class Refuser {

        @Test
        @DisplayName("Refuse un congé avec commentaire")
        void refuser_conge_ok() {
            // GIVEN
            conge.setId(1L);
            when(congeRepository.findById(1L)).thenReturn(Optional.of(conge));
            when(congeRepository.save(any())).thenReturn(conge);

            // WHEN
            Conge result = planningService.refuser(1L, "Effectifs insuffisants");

            // THEN
            assertThat(result.getStatut()).isEqualTo(Conge.StatutConge.REFUSE);
            assertThat(result.getCommentaireRh()).isEqualTo("Effectifs insuffisants");
        }
    }

    @Nested
    @DisplayName("Conge — méthodes métier")
    class CongeMetier {

        @Test
        @DisplayName("getNombreJours() calcule correctement la durée")
        void getNombreJours_calcul_ok() {
            assertThat(conge.getNombreJours()).isEqualTo(15); // 1er au 15 juin inclus
        }

        @Test
        @DisplayName("getNombreJours() retourne 0 si dates nulles")
        void getNombreJours_datesNulles_retourne0() {
            var congeVide = new Conge();
            assertThat(congeVide.getNombreJours()).isEqualTo(0);
        }
    }
}