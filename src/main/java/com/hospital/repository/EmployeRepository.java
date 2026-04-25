package com.hospital.repository;

import com.hospital.entity.Employe;
import com.hospital.enums.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {

    @Query("SELECT e FROM Employe e LEFT JOIN FETCH e.service")
    List<Employe> findAllWithService();

    List<Employe> findByStatut(Statut statut);

    List<Employe> findByServiceId(Long serviceId);

    @Query("""
        SELECT e FROM Employe e
        WHERE LOWER(e.nom) LIKE LOWER(CONCAT('%', :terme, '%'))
           OR LOWER(e.prenom) LIKE LOWER(CONCAT('%', :terme, '%'))
           OR LOWER(e.email) LIKE LOWER(CONCAT('%', :terme, '%'))
        """)
    List<Employe> rechercher(@Param("terme") String terme);

    long countByStatut(Statut statut);
}