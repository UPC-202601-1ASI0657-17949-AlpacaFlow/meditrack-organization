package com.alpacaflow.meditrack.organization.caregiver.domain.services;

import com.alpacaflow.meditrack.organization.caregiver.domain.model.aggregates.Caregiver;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetAllCaregiversByOrganizationIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetAllCaregiversQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetCaregiverByIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetCaregiverByUserIdAndOrganizationIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetCaregiverByUserIdQuery;

import java.util.List;
import java.util.Optional;

public interface CaregiverQueryService {
    Optional<Caregiver> handle(GetCaregiverByIdQuery query);
    List<Caregiver> handle(GetAllCaregiversQuery query);
    List<Caregiver> handle(GetAllCaregiversByOrganizationIdQuery query);
    Optional<Caregiver> handle(GetCaregiverByUserIdQuery query);
    Optional<Caregiver> handle(GetCaregiverByUserIdAndOrganizationIdQuery query);
}
