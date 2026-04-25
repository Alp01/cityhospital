package com.hospital.service;

import com.hospital.entity.Conge;
import com.hospital.entity.Employe;
import com.hospital.enums.Statut;
import com.hospital.repository.CongeRepository;
import com.hospital.repository.EmployeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PlanningService {

    private final CongeRepository    congeRepository;
    private final EmployeRepository  employeRepository;

    public PlanningService(CongeRepository congeRepository,
                           EmployeRepository employeRepository) {
        this.congeRepository   = congeRepository;
        this.employeRepository = employeRepository;
    }

    @Transactional(readOnly = true)
    public List<Conge> findAll() {
        return congeRepository.findAllWithEmploye();
    }

    @Transactional(readOnly = true)
    public List<Conge> findByStatut(Conge.StatutConge statut) {
        return congeRepository.findByStatutWithEmploye(statut);
    }

    @Transactional(readOnly = true)
    public List<Conge> findByEmploye(Long employeId) {
        return congeRepository.findByEmployeId(employeId);
    }

    @Transactional(readOnly = true)
    public Optional<Conge> findById(Long id) {
        return congeRepository.findById(id);
    }

    public Conge sauvegarder(Conge conge) {
        valider(conge);
        return congeRepository.save(conge);
    }

    public Conge approuver(Long congeId, String commentaire) {
        var conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new IllegalArgumentException("Congé introuvable : " + congeId));
        conge.setStatut(Conge.StatutConge.APPROUVE);
        conge.setCommentaireRh(commentaire);

        if (conge.estEnCours()) {
            conge.getEmploye().setStatut(Statut.CONGE);
            employeRepository.save(conge.getEmploye());
        }
        return congeRepository.save(conge);
    }

    public Conge refuser(Long congeId, String commentaire) {
        var conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new IllegalArgumentException("Congé introuvable : " + congeId));
        conge.setStatut(Conge.StatutConge.REFUSE);
        conge.setCommentaireRh(commentaire);
        return congeRepository.save(conge);
    }

    public void supprimer(Conge conge) {
        congeRepository.delete(conge);
    }


    @Transactional(readOnly = true)
    public long countEnAttente() {
        return congeRepository.countByStatut(Conge.StatutConge.EN_ATTENTE);
    }

    @Transactional(readOnly = true)
    public long countApprouves() {
        return congeRepository.countByStatut(Conge.StatutConge.APPROUVE);
    }


    private void valider(Conge conge) {
        if (conge.getDateDebut() == null || conge.getDateFin() == null) {
            throw new IllegalArgumentException("Les dates de début et de fin sont obligatoires.");
        }
        if (conge.getDateFin().isBefore(conge.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début.");
        }
        var chevauchements = congeRepository.findChevauchements(
                conge.getEmploye().getId(),
                conge.getDateDebut(),
                conge.getDateFin(),
                conge.getId()
        );
        if (!chevauchements.isEmpty()) {
            throw new IllegalArgumentException(
                "Cet employé a déjà un congé sur cette période.");
        }
    }
}