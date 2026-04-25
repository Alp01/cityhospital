package com.hospital.entity;

import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeConge type;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutConge statut = StatutConge.EN_ATTENTE;

    private String motif;

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