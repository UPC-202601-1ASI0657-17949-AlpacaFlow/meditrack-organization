package com.alpacafkow.meditrack.organization.doctor.interfaces.rest.resources.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Payload to create a doctor within an organization")
public record CreateDoctorRequest(
        @NotNull(message = "organizationId is required")
        @Positive(message = "organizationId must be positive")
        @Schema(example = "1") Long organizationId,

        @Schema(example = "1002", description = "Optional. If null or 0, a user will be autoprovisioned in IAM with role 'doctor'.")
        Long userId,

        @NotBlank(message = "firstName is required")
        @Schema(example = "Carlos") String firstName,

        @NotBlank(message = "lastName is required")
        @Schema(example = "Mendoza") String lastName,

        @NotBlank(message = "specialty is required")
        @Schema(example = "Cardiology") String specialty,

        @NotNull(message = "age is required")
        @Positive(message = "age must be positive")
        @Schema(example = "45") Integer age,

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Schema(example = "carlos.mendoza@clinic.example") String email,

        @NotBlank(message = "phoneNumber is required")
        @Schema(example = "+51999999999") String phoneNumber,

        @NotBlank(message = "imageUrl is required")
        @Schema(example = "https://example.com/avatar.png") String imageUrl
) {
}
