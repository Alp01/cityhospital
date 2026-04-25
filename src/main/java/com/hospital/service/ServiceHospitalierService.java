package com.hospital.service;

import com.hospital.entity.ServiceHospitalier;
import com.hospital.repository.ServiceHospitalierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServiceHospitalierService {

    private final ServiceHospitalierRepository repository;

    public ServiceHospitalierService(ServiceHospitalierRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ServiceHospitalier> findAllWithEmployes() {
        return repository.findAllWithEmployes();
    }

    public ServiceHospitalier sauvegarder(ServiceHospitalier service) {
        return repository.save(service);
    }

    public void supprimer(ServiceHospitalier service) {
        repository.delete(service);
    }

    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }
}