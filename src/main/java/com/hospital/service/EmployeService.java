package com.hospital.service;

import com.hospital.entity.Employe;
import com.hospital.enums.Statut;
import com.hospital.repository.EmployeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class EmployeService {

    private final EmployeRepository repository;

    @Transactional(readOnly = true)
    public List<Employe> findAll() {
        return repository.findAllWithService();
    }

    @Transactional(readOnly = true)
    public List<Employe> rechercher(String terme) {
        if (terme == null || terme.isBlank()) return findAll();
        return repository.rechercher(terme.trim());
    }

    public Employe sauvegarder(Employe employe) {
        return repository.save(employe);
    }

    public void supprimer(Employe employe) {
        repository.delete(employe);
    }

    @Transactional(readOnly = true)
    public long countActifs() {
        return repository.countByStatut(Statut.ACTIF);
    }

    @Transactional(readOnly = true)
    public long countEnConge() {
        return repository.countByStatut(Statut.CONGE);
    }
}