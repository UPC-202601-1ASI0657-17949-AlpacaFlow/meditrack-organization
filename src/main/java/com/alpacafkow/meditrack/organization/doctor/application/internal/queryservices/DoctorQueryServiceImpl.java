package com.alpacafkow.meditrack.organization.doctor.application.internal.queryservices;

import com.alpacafkow.meditrack.organization.doctor.domain.model.aggregates.Doctor;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetAllDoctorsByOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetAllDoctorsQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetDoctorByIdQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetDoctorByUserIdAndOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetDoctorByUserIdQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.services.DoctorQueryService;
import com.alpacafkow.meditrack.organization.doctor.infrastructure.persistence.jpa.repositories.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorQueryServiceImpl implements DoctorQueryService {

    private final DoctorRepository doctorRepository;

    public DoctorQueryServiceImpl(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Doctor> handle(GetDoctorByIdQuery query) {
        return doctorRepository.findById(query.doctorId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> handle(GetAllDoctorsQuery query) {
        return doctorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> handle(GetAllDoctorsByOrganizationIdQuery query) {
        return doctorRepository.findByOrganization_Id(query.organizationId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Doctor> handle(GetDoctorByUserIdQuery query) {
        return doctorRepository.findByUserId(query.userId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Doctor> handle(GetDoctorByUserIdAndOrganizationIdQuery query) {
        return doctorRepository.findByUserIdAndOrganization_Id(query.userId(), query.organizationId());
    }
}
