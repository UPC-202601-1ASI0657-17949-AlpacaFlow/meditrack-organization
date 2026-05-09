package com.alpacafkow.meditrack.organization.seniorcitizen.interfaces.rest.resources.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(description = "Senior citizen representation exposed by the organization microservice")
public record SeniorCitizenResponse(
        Long id,
        Long organizationId,
        String firstName,
        String lastName,
        @JsonFormat(pattern = "yyyy-MM-dd") Date birthDate,
        Integer age,
        String gender,
        Double weight,
        String dni,
        Double height,
        String imageUrl,
        Long deviceId,
        Long assignedDoctorId,
        Long assignedCaregiverId
) {
}
