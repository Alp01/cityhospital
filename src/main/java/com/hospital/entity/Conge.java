package com.hospital.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "conge")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Conge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "L'employé est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @NotNull(message = "Le type de congé est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeConge type;

    @NotNull(message = "La date de début est obligatoire")
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutConge statut = StatutConge.EN_ATTENTE;

    @Size(max = 500, message = "Le motif ne peut pas dépasser 500 caractères")
    private String motif;

    @Size(max = 500, message = "Le commentaire ne peut pas dépasser 500 caractères")
    @Column(name = "commentaire_rh")
    private String commentaireRh;

    public Conge() {
        this.statut = StatutConge.EN_ATTENTE;
    }

    public enum TypeConge {
        CONGE_ANNUEL, MALADIE, MATERNITE, PATERNITE, FORMATION, SANS_SOLDE, AUTRE
    }

    public enum StatutConge {
        EN_ATTENTE, APPROUVE, REFUSE, ANNULE
    }

    public long getNombreJours() {
        if (dateDebut == null || dateFin == null) return 0;
        return ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
    }

    public boolean estEnCours() {
        var today = LocalDate.now();
        return statut == StatutConge.APPROUVE
                && !today.isBefore(dateDebut)
                && !today.isAfter(dateFin);
    }
}