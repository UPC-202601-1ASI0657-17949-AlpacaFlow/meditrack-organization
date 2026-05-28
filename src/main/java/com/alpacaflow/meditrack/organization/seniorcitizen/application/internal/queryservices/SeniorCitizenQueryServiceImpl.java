package com.alpacaflow.meditrack.organization.seniorcitizen.application.internal.queryservices;

import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.aggregates.SeniorCitizen;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.queries.GetAllSeniorCitizensByOrganizationIdQuery;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.queries.GetAllSeniorCitizensQuery;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.queries.GetSeniorCitizenByIdQuery;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.queries.GetSeniorCitizensByAssignedCaregiverIdQuery;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.queries.GetSeniorCitizensByAssignedDoctorIdQuery;
import com.alpacaflow.meditrack.organization.seniorcitizen.domain.services.SeniorCitizenQueryService;
import com.alpacaflow.meditrack.organization.seniorcitizen.infrastructure.persistence.jpa.repositories.SeniorCitizenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SeniorCitizenQueryServiceImpl implements SeniorCitizenQueryService {

    private final SeniorCitizenRepository seniorCitizenRepository;

    public SeniorCitizenQueryServiceImpl(SeniorCitizenRepository seniorCitizenRepository) {
        this.seniorCitizenRepository = seniorCitizenRepository;
    }

    @Override
    public Optional<SeniorCitizen> handle(GetSeniorCitizenByIdQuery query) {
        return seniorCitizenRepository.findById(query.seniorCitizenId());
    }

    @Override
    public List<SeniorCitizen> handle(GetAllSeniorCitizensQuery query) {
        return seniorCitizenRepository.findAll();
    }

    @Override
    public List<SeniorCitizen> handle(GetAllSeniorCitizensByOrganizationIdQuery query) {
        return seniorCitizenRepository.findByOrganization_Id(query.organizationId());
    }

    @Override
    public List<SeniorCitizen> handle(GetSeniorCitizensByAssignedDoctorIdQuery query) {
        return seniorCitizenRepository.findByAssignedDoctorId(query.doctorId());
    }

    @Override
    public List<SeniorCitizen> handle(GetSeniorCitizensByAssignedCaregiverIdQuery query) {
        return seniorCitizenRepository.findByAssignedCaregiverId(query.caregiverId());
    }
}
