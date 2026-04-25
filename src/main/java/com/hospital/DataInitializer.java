package com.hospital;


import com.hospital.entity.Employe;
import com.hospital.entity.ServiceHospitalier;
import com.hospital.enums.Poste;
import com.hospital.enums.Statut;
import com.hospital.repository.EmployeRepository;
import com.hospital.repository.ServiceHospitalierRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EmployeRepository employeRepo;
    private final ServiceHospitalierRepository serviceRepo;

    public DataInitializer(EmployeRepository employeRepo,
                           ServiceHospitalierRepository serviceRepo) {
        this.employeRepo = employeRepo;
        this.serviceRepo = serviceRepo;
    }

    @Override
    public void run(String... args) {
        if (serviceRepo.count() > 0) return;

        var urgences    = sauvegarderService("Urgences",    "Service des urgences 24h/24");
        var cardiologie = sauvegarderService("Cardiologie", "Maladies cardiovasculaires");
        var pediatrie   = sauvegarderService("Pédiatrie",   "Soins aux enfants");
        var chirurgie   = sauvegarderService("Chirurgie",   "Blocs opératoires");
        var neurologie  = sauvegarderService("Neurologie",  "Maladies du système nerveux");

        creerEmploye("Shepherd", "Derek",    "derek.shepherd@cityhospital.com",  Poste.MEDECIN,       urgences,    LocalDate.of(2010, 3, 15));
        creerEmploye("Yang",     "Cristina", "cristina.yang@cityhospital.com",   Poste.CHIRURGIEN,    chirurgie,   LocalDate.of(2012, 7, 1));
        creerEmploye("Grey",     "Meredith", "meredith.grey@cityhospital.com",   Poste.MEDECIN,       neurologie,  LocalDate.of(2011, 9, 5));
        creerEmploye("Stevens",  "Izzie",    "izzie.stevens@cityhospital.com",   Poste.INFIRMIER,     cardiologie, LocalDate.of(2013, 2, 20));
        creerEmploye("O'Malley", "George",   "george.omalley@cityhospital.com",  Poste.AIDE_SOIGNANT, urgences,    LocalDate.of(2014, 6, 10));
        creerEmploye("Torres",   "Callie",   "callie.torres@cityhospital.com",   Poste.CHIRURGIEN,    chirurgie,   LocalDate.of(2009, 11, 3));
        creerEmploye("Karev",    "Alex",     "alex.karev@cityhospital.com",      Poste.MEDECIN,       pediatrie,   LocalDate.of(2015, 4, 18));
        creerEmploye("Avery",    "Jackson",  "jackson.avery@cityhospital.com",   Poste.CHIRURGIEN,    chirurgie,   LocalDate.of(2016, 8, 22));
        creerEmploye("Kepner",   "April",    "april.kepner@cityhospital.com",    Poste.INFIRMIER,     urgences,    LocalDate.of(2017, 1, 7));
        creerEmploye("Pierce",   "Maggie",   "maggie.pierce@cityhospital.com",   Poste.MEDECIN,       cardiologie, LocalDate.of(2018, 5, 14));
    }

    private ServiceHospitalier sauvegarderService(String nom, String description) {
        return serviceRepo.save(ServiceHospitalier.builder()
                .nom(nom)
                .description(description)
                .build());
    }

    private void creerEmploye(String nom, String prenom, String email,
                              Poste poste, ServiceHospitalier service,
                              LocalDate embauche) {
        employeRepo.save(Employe.builder()
                .nom(nom)
                .prenom(prenom)
                .email(email)
                .poste(poste)
                .service(service)
                .dateEmbauche(embauche)
                .statut(Statut.ACTIF)
                .build());
    }
}