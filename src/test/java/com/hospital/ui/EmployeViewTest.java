package com.hospital.ui;

import com.hospital.entity.Employe;
import com.hospital.enums.Poste;
import com.hospital.enums.Statut;
import com.hospital.service.EmployeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("EmployeView")
class EmployeViewTest {

    @Autowired
    private EmployeService employeService;

    @Nested
    @DisplayName("Chargement des données du Grid")
    class ChargementGrid {

        @Test
        @DisplayName("findAll() retourne les employés pour alimenter le Grid")
        void findAll_alimenteGrid() {
            employeService.sauvegarder(Employe.builder()
                    .nom("Grey").prenom("Meredith")
                    .email("meredith.ui.test@sg.com")
                    .poste(Poste.MEDECIN)
                    .build());

            var employes = employeService.findAll();
            assertThat(employes).isNotEmpty();
        }

        @Test
        @DisplayName("rechercher() filtre les résultats comme la toolbar de la vue")
        void rechercher_filtrePourToolbar() {
            employeService.sauvegarder(Employe.builder()
                    .nom("Shepherd").prenom("Derek")
                    .email("derek.ui.test@sg.com")
                    .poste(Poste.MEDECIN)
                    .build());

            var resultatNom    = employeService.rechercher("Shepherd");
            var resultatEmail  = employeService.rechercher("derek.ui.test");
            var resultatVide   = employeService.rechercher("xyz_inexistant");

            assertThat(resultatNom).anyMatch(e -> e.getNom().equals("Shepherd"));
            assertThat(resultatEmail).anyMatch(e -> e.getEmail().contains("derek.ui.test"));
            assertThat(resultatVide).isEmpty();
        }
    }

    @Nested
    @DisplayName("Action Sauvegarder")
    class ActionSauvegarder {

        @Test
        @DisplayName("Un nouvel employé est persisté après sauvegarder()")
        void sauvegarder_nouvelEmploye_persiste() {
            var nouvel = new Employe();
            nouvel.setNom("Karev");
            nouvel.setPrenom("Alex");
            nouvel.setEmail("alex.karev.ui@sg.com");
            nouvel.setPoste(Poste.MEDECIN);

            employeService.sauvegarder(nouvel);

            assertThat(employeService.rechercher("alex.karev.ui"))
                    .hasSize(1)
                    .first()
                    .satisfies(e -> {
                        assertThat(e.getNomComplet()).isEqualTo("Alex Karev");
                        assertThat(e.getStatut()).isEqualTo(Statut.ACTIF);
                    });
        }

        @Test
        @DisplayName("Modifier un employé existant met à jour ses données")
        void sauvegarder_employe_existant_miseAJour() {
            // GIVEN
            var employe = employeService.sauvegarder(Employe.builder()
                    .nom("Yang").prenom("Cristina")
                    .email("cristina.yang.ui@sg.com")
                    .poste(Poste.CHIRURGIEN)
                    .build());

            // WHEN
            employe.setStatut(Statut.CONGE);
            employe.setTelephone("+33642533405");
            employeService.sauvegarder(employe);

            // THEN
            var updated = employeService.rechercher("cristina.yang.ui").getFirst();
            assertThat(updated.getStatut()).isEqualTo(Statut.CONGE);
            assertThat(updated.getTelephone()).isEqualTo("+33642533405");
        }
    }

    @Nested
    @DisplayName("Action Supprimer")
    class ActionSupprimer {

        @Test
        @DisplayName("L'employé supprimé n'apparaît plus dans le Grid")
        void supprimer_employeDisparait_duGrid() {
            // GIVEN
            var employe = employeService.sauvegarder(Employe.builder()
                    .nom("O'Malley").prenom("George")
                    .email("george.omalley.ui@sg.com")
                    .poste(Poste.AIDE_SOIGNANT)
                    .build());

            // WHEN
            employeService.supprimer(employe);

            // THEN
            assertThat(employeService.rechercher("george.omalley.ui")).isEmpty();
        }
    }
}