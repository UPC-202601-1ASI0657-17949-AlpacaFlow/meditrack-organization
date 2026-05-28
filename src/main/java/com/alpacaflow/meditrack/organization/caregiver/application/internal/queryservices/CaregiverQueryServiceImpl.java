package com.alpacaflow.meditrack.organization.caregiver.application.internal.queryservices;

import com.alpacaflow.meditrack.organization.caregiver.domain.model.aggregates.Caregiver;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetAllCaregiversByOrganizationIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetAllCaregiversQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetCaregiverByIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetCaregiverByUserIdAndOrganizationIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetCaregiverByUserIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.services.CaregiverQueryService;
import com.alpacaflow.meditrack.organization.caregiver.infrastructure.persistence.jpa.repositories.CaregiverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CaregiverQueryServiceImpl implements CaregiverQueryService {

    private final CaregiverRepository caregiverRepository;

    public CaregiverQueryServiceImpl(CaregiverRepository caregiverRepository) {
        this.caregiverRepository = caregiverRepository;
    }

    @Override
    public Optional<Caregiver> handle(GetCaregiverByIdQuery query) {
        return caregiverRepository.findById(query.caregiverId());
    }

    @Override
    public List<Caregiver> handle(GetAllCaregiversQuery query) {
        return caregiverRepository.findAll();
    }

    @Override
    public List<Caregiver> handle(GetAllCaregiversByOrganizationIdQuery query) {
        return caregiverRepository.findByOrganization_Id(query.organizationId());
    }

    @Override
    public Optional<Caregiver> handle(GetCaregiverByUserIdQuery query) {
        return caregiverRepository.findByUserId(query.userId());
    }

    @Override
    public Optional<Caregiver> handle(GetCaregiverByUserIdAndOrganizationIdQuery query) {
        return caregiverRepository.findByUserIdAndOrganization_Id(query.userId(), query.organizationId());
    }
}
