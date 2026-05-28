package com.alpacaflow.meditrack.organization.caregiver.interfaces.rest.resources.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Caregiver representation exposed by the organization microservice")
public record CaregiverResponse(
        Long id,
        Long organizationId,
        Long userId,
        String firstName,
        String lastName,
        Integer age,
        String email,
        String phoneNumber,
        String imageUrl
) {
}
