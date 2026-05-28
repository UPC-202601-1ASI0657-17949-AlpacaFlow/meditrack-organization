package com.alpacaflow.meditrack.organization.admin.interfaces.rest.resources.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Payload to create an admin within an organization")
public record CreateAdminRequest(
        @NotNull(message = "organizationId is required")
        @Positive(message = "organizationId must be positive")
        @Schema(example = "1") Long organizationId,

        @NotNull(message = "userId is required")
        @Positive(message = "userId must be positive")
        @Schema(example = "1001", description = "Identifier of the user inside IAM") Long userId,

        @NotBlank(message = "firstName is required")
        @Schema(example = "Ana") String firstName,

        @NotBlank(message = "lastName is required")
        @Schema(example = "Torres") String lastName
) {
}
