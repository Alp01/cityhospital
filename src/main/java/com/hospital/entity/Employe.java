package com.hospital.entity;

import com.hospital.enums.Poste;
import com.hospital.enums.Statut;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "employe")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    @Column(nullable = false)
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(unique = true, nullable = false)
    private String email;

    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{7,15}$",
            message = "Format de téléphone invalide")
    private String telephone;

    @PastOrPresent(message = "La date d'embauche ne peut pas être dans le futur")
    @Column(name = "date_embauche")
    private LocalDate dateEmbauche;

    @NotNull(message = "Le poste est obligatoire")
    @Enumerated(EnumType.STRING)
    private Poste poste;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Statut statut = Statut.ACTIF;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceHospitalier service;

    public Employe() {
        this.statut = Statut.ACTIF;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }
}