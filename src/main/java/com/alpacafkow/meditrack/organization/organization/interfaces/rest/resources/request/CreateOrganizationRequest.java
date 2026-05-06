package com.alpacafkow.meditrack.organization.organization.interfaces.rest.resources.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Payload to create an organization")
public record CreateOrganizationRequest(
        @NotBlank(message = "Name is required")
        @Schema(example = "Clínica San José")
        String name,

        @NotBlank(message = "Type is required")
        @Pattern(regexp = "clinic|resident", message = "Type must be either 'clinic' or 'resident'")
        @Schema(example = "clinic", allowableValues = {"clinic", "resident"})
        String type,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Schema(example = "contact@clinic.example")
        String email
) {
}
