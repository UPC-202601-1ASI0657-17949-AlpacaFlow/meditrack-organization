package com.alpacaflow.meditrack.organization.doctor.interfaces.rest.resources.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Payload to update doctor information")
public record UpdateDoctorRequest(
        @NotBlank(message = "firstName is required")
        @Schema(example = "Carlos") String firstName,

        @NotBlank(message = "lastName is required")
        @Schema(example = "Mendoza Soto") String lastName,

        @NotBlank(message = "specialty is required")
        @Schema(example = "Cardiology") String specialty,

        @NotNull(message = "age is required")
        @Positive(message = "age must be positive")
        @Schema(example = "46") Integer age,

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Schema(example = "carlos.mendoza@clinic.example") String email,

        @NotBlank(message = "phoneNumber is required")
        @Schema(example = "+51999999999") String phoneNumber,

        @Schema(example = "https://example.com/avatar.png", description = "Optional") String imageUrl
) {
}
