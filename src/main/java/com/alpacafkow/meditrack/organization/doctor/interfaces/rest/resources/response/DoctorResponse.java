package com.alpacafkow.meditrack.organization.doctor.interfaces.rest.resources.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(description = "Doctor representation")
public record DoctorResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Long organizationId,
        @Schema(example = "1002") Long userId,
        @Schema(example = "Carlos") String firstName,
        @Schema(example = "Mendoza") String lastName,
        @Schema(example = "Carlos Mendoza") String fullName,
        @Schema(example = "45") Integer age,
        @Schema(example = "carlos.mendoza@clinic.example") String email,
        @Schema(example = "Cardiology") String specialty,
        @Schema(example = "+51999999999") String phoneNumber,
        @Schema(example = "https://example.com/avatar.png") String imageUrl,
        Date createdAt,
        Date updatedAt
) {
}
