package com.alpacafkow.meditrack.organization.admin.interfaces.rest.resources.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload to update admin's personal information")
public record UpdateAdminRequest(
        @NotBlank(message = "firstName is required")
        @Schema(example = "Ana María") String firstName,

        @NotBlank(message = "lastName is required")
        @Schema(example = "Torres Soto") String lastName
) {
}
