package com.hospital.integration;

import com.hospital.entity.Employe;
import com.hospital.entity.ServiceHospitalier;
import com.hospital.enums.Poste;
import com.hospital.repository.EmployeRepository;
import com.hospital.repository.ServiceHospitalierRepository;
import com.hospital.service.EmployeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("EmployeService — tests d'intégration")
class EmployeServiceIntegrationTest {

    @Autowired
    private EmployeService employeService;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ServiceHospitalierRepository serviceRepo;

    private ServiceHospitalier urgences;

    @BeforeEach
    void setUp() {
        urgences = serviceRepo.save(ServiceHospitalier.builder()
                .nom("Urgences Test")
                .description("Service test")
                .build());
    }

    @Test
    @DisplayName("Sauvegarde et retrouve un employé en base")
    void sauvegarder_puisRechercher() {
        // GIVEN
        var employe = Employe.builder()
                .nom("Grey")
                .prenom("Meredith")
                .email("meredith.grey.test@seattlegrace.com")
                .poste(Poste.MEDECIN)
                .service(urgences)
                .dateEmbauche(LocalDate.of(2020, 1, 1))
                .build();

        // WHEN
        employeService.sauvegarder(employe);
        var resultats = employeService.rechercher("meredith.grey.test");

        // THEN
        assertThat(resultats).hasSize(1);
        assertThat(resultats.getFirst().getNomComplet()).isEqualTo("Meredith Grey");
        assertThat(resultats.getFirst().getService().getNom()).isEqualTo("Urgences Test");
    }

    @Test
    @DisplayName("Supprime un employé de la base")
    void supprimer_ok() {
        // GIVEN
        var employe = employeService.sauvegarder(Employe.builder()
                .nom("Stevens")
                .prenom("Izzie")
                .email("izzie.test@seattlegrace.com")
                .poste(Poste.INFIRMIER)
                .build());

        long countAvant = employeRepository.count();

        // WHEN
        employeService.supprimer(employe);

        // THEN
        assertThat(employeRepository.count()).isEqualTo(countAvant - 1);
    }

    @Test
    @DisplayName("La recherche est insensible à la casse")
    void rechercher_insensibleCasse() {
        employeService.sauvegarder(Employe.builder()
                .nom("Karev")
                .prenom("Alex")
                .email("alex.karev.test@seattlegrace.com")
                .poste(Poste.MEDECIN)
                .build());

        assertThat(employeService.rechercher("karev")).isNotEmpty();
        assertThat(employeService.rechercher("KAREV")).isNotEmpty();
        assertThat(employeService.rechercher("KaReV")).isNotEmpty();
    }
}