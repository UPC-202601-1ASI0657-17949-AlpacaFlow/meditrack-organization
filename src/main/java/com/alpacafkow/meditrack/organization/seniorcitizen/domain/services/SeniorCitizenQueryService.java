package com.alpacafkow.meditrack.organization.seniorcitizen.domain.services;

import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.aggregates.SeniorCitizen;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.queries.GetAllSeniorCitizensByOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.queries.GetAllSeniorCitizensQuery;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.queries.GetSeniorCitizenByIdQuery;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.queries.GetSeniorCitizensByAssignedCaregiverIdQuery;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.queries.GetSeniorCitizensByAssignedDoctorIdQuery;

import java.util.List;
import java.util.Optional;

public interface SeniorCitizenQueryService {

    Optional<SeniorCitizen> handle(GetSeniorCitizenByIdQuery query);

    List<SeniorCitizen> handle(GetAllSeniorCitizensQuery query);

    List<SeniorCitizen> handle(GetAllSeniorCitizensByOrganizationIdQuery query);

    List<SeniorCitizen> handle(GetSeniorCitizensByAssignedDoctorIdQuery query);

    List<SeniorCitizen> handle(GetSeniorCitizensByAssignedCaregiverIdQuery query);
}
