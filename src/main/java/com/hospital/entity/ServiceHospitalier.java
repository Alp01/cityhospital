package com.hospital.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_hospitalier")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class ServiceHospitalier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du service est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Column(nullable = false, unique = true)
    private String nom;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    @Size(max = 100, message = "Le nom du chef de service ne peut pas dépasser 100 caractères")
    @Column(name = "chef_service")
    private String chefService;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Employe> employes = new ArrayList<>();

    public ServiceHospitalier() {
        this.employes = new ArrayList<>();
    }

    @Override
    public String toString() { return nom; }
}