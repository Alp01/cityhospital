package com.hospital.repository;

import com.hospital.entity.Conge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CongeRepository extends JpaRepository<Conge, Long> {

    @Query("SELECT c FROM Conge c LEFT JOIN FETCH c.employe e LEFT JOIN FETCH e.service")
    List<Conge> findAllWithEmploye();

    @Query("SELECT c FROM Conge c LEFT JOIN FETCH c.employe WHERE c.employe.id = :employeId")
    List<Conge> findByEmployeId(@Param("employeId") Long employeId);

    @Query("SELECT c FROM Conge c LEFT JOIN FETCH c.employe e LEFT JOIN FETCH e.service WHERE c.statut = :statut")
    List<Conge> findByStatutWithEmploye(@Param("statut") Conge.StatutConge statut);

    @Query("""
        SELECT c FROM Conge c
        WHERE c.employe.id = :employeId
          AND c.statut <> com.hospital.entity.Conge$StatutConge.REFUSE
          AND c.statut <> com.hospital.entity.Conge$StatutConge.ANNULE
          AND c.dateDebut <= :dateFin
          AND c.dateFin   >= :dateDebut
          AND (:excludeId IS NULL OR c.id <> :excludeId)
        """)
    List<Conge> findChevauchements(@Param("employeId") Long employeId,
                                   @Param("dateDebut")  LocalDate dateDebut,
                                   @Param("dateFin")    LocalDate dateFin,
                                   @Param("excludeId")  Long excludeId);

    long countByStatut(Conge.StatutConge statut);
}