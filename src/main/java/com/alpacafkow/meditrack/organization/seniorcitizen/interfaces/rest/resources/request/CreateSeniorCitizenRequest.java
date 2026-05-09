package com.alpacafkow.meditrack.organization.seniorcitizen.interfaces.rest.resources.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.util.Date;

@Schema(description = "Payload to create a senior citizen within an organization")
public record CreateSeniorCitizenRequest(
        @NotNull(message = "organizationId is required")
        @Positive(message = "organizationId must be positive")
        @Schema(example = "1") Long organizationId,

        @NotBlank(message = "firstName is required")
        @Schema(example = "Maria") String firstName,

        @NotBlank(message = "lastName is required")
        @Schema(example = "Quispe") String lastName,

        @NotNull(message = "birthDate is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(example = "1955-04-12", type = "string", format = "date") Date birthDate,

        @NotBlank(message = "gender is required")
        @Pattern(regexp = "^(?i)(Masculino|Femenino)$", message = "gender must be Masculino or Femenino")
        @Schema(example = "Femenino", allowableValues = {"Masculino", "Femenino"}) String gender,

        @NotNull(message = "weight is required")
        @Schema(example = "62.5") Double weight,

        @NotBlank(message = "dni is required")
        @Pattern(regexp = "^\\d{1,8}$", message = "dni must contain only digits and at most 8 characters")
        @Schema(example = "12345678") String dni,

        @NotNull(message = "height is required")
        @Schema(example = "160.0") Double height,

        @NotBlank(message = "imageUrl is required")
        @Schema(example = "https://example.com/avatar.png") String imageUrl,

        @Schema(example = "0",
                description = "Optional. If null or 0, a device will be auto-created via the Devices context.")
        Long deviceId
) {
}
