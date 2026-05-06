package com.alpacafkow.meditrack.organization.organization.interfaces.rest.resources.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(description = "Organization representation")
public record OrganizationResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Clínica San José") String name,
        @Schema(example = "clinic") String type,
        @Schema(example = "contact@clinic.example") String email,
        Date createdAt,
        Date updatedAt
) {
}
