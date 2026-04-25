package com.hospital.entity;

import com.hospital.enums.Poste;
import com.hospital.enums.Statut;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "employe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    private String telephone;

    @Column(name = "date_embauche")
    private LocalDate dateEmbauche;

    @Enumerated(EnumType.STRING)
    private Poste poste;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Statut statut = Statut.ACTIF;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceHospitalier service;

    public String getNomComplet() {
        return prenom + " " + nom;
    }
}