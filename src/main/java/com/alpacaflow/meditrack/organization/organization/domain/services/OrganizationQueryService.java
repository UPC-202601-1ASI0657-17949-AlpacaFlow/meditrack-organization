package com.alpacaflow.meditrack.organization.organization.domain.services;

import com.alpacaflow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacaflow.meditrack.organization.organization.domain.model.queries.GetAllOrganizationsQuery;
import com.alpacaflow.meditrack.organization.organization.domain.model.queries.GetOrganizationByIdQuery;

import java.util.List;
import java.util.Optional;

public interface OrganizationQueryService {
    Optional<Organization> handle(GetOrganizationByIdQuery query);

    List<Organization> handle(GetAllOrganizationsQuery query);

    boolean isOrganizationNameAvailable(String name);
}
