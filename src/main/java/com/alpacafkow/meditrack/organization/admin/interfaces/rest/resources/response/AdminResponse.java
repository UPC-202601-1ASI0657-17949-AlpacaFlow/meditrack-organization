package com.alpacafkow.meditrack.organization.admin.interfaces.rest.resources.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(description = "Admin representation")
public record AdminResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Long organizationId,
        @Schema(example = "1001") Long userId,
        @Schema(example = "Ana") String firstName,
        @Schema(example = "Torres") String lastName,
        @Schema(example = "Ana Torres") String fullName,
        Date createdAt,
        Date updatedAt
) {
}
