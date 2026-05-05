# 🏥 City Hospital — Gestion du Personnel Hospitalier

Application de gestion des ressources humaines d'un hôpital, développée avec **Vaadin 25** et **Spring Boot 4**.

![Java](https://img.shields.io/badge/Java-21-orange?logo=java)
![Vaadin](https://img.shields.io/badge/Vaadin-25.1.3-blue?logo=vaadin)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-green?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-7.0.4-green?logo=springsecurity)
![H2](https://img.shields.io/badge/Database-H2%20%28dev%29-lightgrey)
![Lombok](https://img.shields.io/badge/Lombok-1.18-red)

---

## 📋 Fonctionnalités

- **Tableau de bord** — KPIs en temps réel (effectifs, congés en attente, taux d'actifs par service)
- **Gestion des employés** — CRUD complet avec recherche, filtrage et validation
- **Gestion des services** — Services hospitaliers avec effectifs associés
- **Planning & Congés** — Demandes de congés avec workflow d'approbation (EN_ATTENTE → APPROUVÉ/REFUSÉ) et détection de chevauchements
- **Sécurité** — Authentification Spring Security avec rôles `ADMIN` et `RH`

---

## 🚀 Lancement rapide

### Prérequis

- Java 21+
- Maven 3.9+

### Démarrer l'application

```bash
git clone https://github.com/Alp01/cityhospital.git
cd cityhospital
./mvnw spring-boot:run
```

L'application est accessible sur **http://localhost:8080**

### Comptes de démo

| Identifiant | Mot de passe | Rôle         | Accès                          |
|-------------|--------------|--------------|--------------------------------|
| `admin`     | `admin123`   | `ROLE_ADMIN` | Toutes les vues                |
| `rh`        | `rh123`      | `ROLE_RH`    | Dashboard, Employés, Planning  |

---

## 🏗️ Architecture

```
src/main/java/com/hospital/
│
├── entity/                         
│   ├── Employe.java                
│   ├── ServiceHospitalier.java
│   ├── Conge.java                  
│   └── Utilisateur.java            
│
├── repository/                     
│   ├── EmployeRepository.java      
│   ├── ServiceHospitalierRepository.java
│   ├── CongeRepository.java        
│   └── UtilisateurRepository.java
│
├── service/                        
│   ├── EmployeService.java
│   ├── ServiceHospitalierService.java
│   └── PlanningService.java        
│
├── security/                       
│   ├── SecurityConfig.java         
│   ├── CityHospitalUserDetailsService.java  
│   ├── SecurityService.java        
│   └── SecurityDataInitializer.java
│
└── ui/
    ├── layout/
    │   └── MainLayout.java         
    └── views/
        ├── LoginView.java      
        ├── DashboardView.java   
        ├── EmployeView.java
        ├── ServiceView.java
        └── PlanningView.java
```

---

## 🛠️ Stack technique

| Technologie              | Version  |
|--------------------------|----------|
| Java                     | 21       |
| Vaadin Flow              | 25.1.3   |
| Spring Boot              | 4.0.5    |
| Spring Security          | 7.0.4    |
| Spring Data JPA          | 4.0.4    |
| Hibernate                | 7.2.7    |
| H2                       | 2.4      |
| Lombok                   | 1.18.44  |
| Jakarta Validation       | 3.1      |
| JUnit 5                  | 6.0.3    |
| Mockito                  | 5.20     |

---
👤 Auteur

Développé par **Erhan Dincer** — projet de démonstration Vaadin 25 / Spring Boot 4.