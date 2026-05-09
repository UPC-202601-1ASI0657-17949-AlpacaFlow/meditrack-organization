package com.alpacafkow.meditrack.organization.doctor.interfaces.rest.transform;

import com.alpacafkow.meditrack.organization.doctor.domain.model.aggregates.Doctor;
import com.alpacafkow.meditrack.organization.doctor.interfaces.rest.resources.response.DoctorResponse;

public final class DoctorResponseFromEntityAssembler {

    private DoctorResponseFromEntityAssembler() {
    }

    public static DoctorResponse toResponse(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getOrganizationId(),
                doctor.getUserId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getFullName(),
                doctor.getAge(),
                doctor.getEmail(),
                doctor.getSpecialty(),
                doctor.getPhoneNumber(),
                doctor.getImageUrl(),
                doctor.getCreatedAt(),
                doctor.getUpdatedAt()
        );
    }
}
