package com.hospital.repository;

import com.hospital.entity.ServiceHospitalier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceHospitalierRepository extends JpaRepository<ServiceHospitalier, Long> {
    Optional<ServiceHospitalier> findByNom(String nom);

    @Query("SELECT s FROM ServiceHospitalier s LEFT JOIN FETCH s.employes")
    List<ServiceHospitalier> findAllWithEmployes();
}