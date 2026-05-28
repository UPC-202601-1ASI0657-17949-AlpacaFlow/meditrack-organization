package com.alpacaflow.meditrack.organization.caregiver.interfaces.rest.transform;

import com.alpacaflow.meditrack.organization.caregiver.domain.model.aggregates.Caregiver;
import com.alpacaflow.meditrack.organization.caregiver.interfaces.rest.resources.response.CaregiverResponse;

public final class CaregiverResponseFromEntityAssembler {

    private CaregiverResponseFromEntityAssembler() {
    }

    public static CaregiverResponse toResponse(Caregiver caregiver) {
        return new CaregiverResponse(
                caregiver.getId(),
                caregiver.getOrganizationId(),
                caregiver.getUserId(),
                caregiver.getFirstName(),
                caregiver.getLastName(),
                caregiver.getAge(),
                caregiver.getEmail(),
                caregiver.getPhoneNumber(),
                caregiver.getImageUrl()
        );
    }
}
