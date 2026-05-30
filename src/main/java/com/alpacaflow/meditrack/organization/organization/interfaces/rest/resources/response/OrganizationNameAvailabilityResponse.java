package com.alpacaflow.meditrack.organization.organization.interfaces.rest.resources.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Whether an organization name is available for registration")
public record OrganizationNameAvailabilityResponse(
        @Schema(description = "true if the name is not yet taken (case-insensitive)")
        boolean available
) {
}
