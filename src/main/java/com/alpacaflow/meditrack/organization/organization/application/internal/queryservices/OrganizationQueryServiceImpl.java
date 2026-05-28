package com.alpacaflow.meditrack.organization.organization.application.internal.queryservices;

import com.alpacaflow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacaflow.meditrack.organization.organization.domain.model.queries.GetAllOrganizationsQuery;
import com.alpacaflow.meditrack.organization.organization.domain.model.queries.GetOrganizationByIdQuery;
import com.alpacaflow.meditrack.organization.organization.domain.services.OrganizationQueryService;
import com.alpacaflow.meditrack.organization.organization.infrastructure.persistence.jpa.repositories.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationQueryServiceImpl implements OrganizationQueryService {

    private final OrganizationRepository organizationRepository;

    public OrganizationQueryServiceImpl(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> handle(GetOrganizationByIdQuery query) {
        return organizationRepository.findById(query.organizationId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organization> handle(GetAllOrganizationsQuery query) {
        return organizationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrganizationNameAvailable(String name) {
        if (name == null || name.isBlank()) {
            return true;
        }
        return !organizationRepository.existsByNameIgnoreCase(name.trim());
    }
}
