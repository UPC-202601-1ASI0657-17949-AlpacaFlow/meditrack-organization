package com.alpacafkow.meditrack.organization.seniorcitizen.interfaces.rest.transform;

import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.aggregates.SeniorCitizen;
import com.alpacafkow.meditrack.organization.seniorcitizen.interfaces.rest.resources.response.SeniorCitizenResponse;

public final class SeniorCitizenResponseFromEntityAssembler {

    private SeniorCitizenResponseFromEntityAssembler() {
    }

    public static SeniorCitizenResponse toResponse(SeniorCitizen seniorCitizen) {
        return new SeniorCitizenResponse(
                seniorCitizen.getId(),
                seniorCitizen.getOrganizationId(),
                seniorCitizen.getFirstName(),
                seniorCitizen.getLastName(),
                seniorCitizen.getBirthDate(),
                seniorCitizen.getAge(),
                seniorCitizen.getGender(),
                seniorCitizen.getWeight(),
                seniorCitizen.getDni(),
                seniorCitizen.getHeight(),
                seniorCitizen.getImageUrl(),
                seniorCitizen.getDeviceId(),
                seniorCitizen.getAssignedDoctorId(),
                seniorCitizen.getAssignedCaregiverId()
        );
    }
}
