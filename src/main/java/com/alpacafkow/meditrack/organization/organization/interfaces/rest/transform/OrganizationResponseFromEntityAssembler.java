package com.alpacafkow.meditrack.organization.organization.interfaces.rest.transform;

import com.alpacafkow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacafkow.meditrack.organization.organization.interfaces.rest.resources.response.OrganizationResponse;

public final class OrganizationResponseFromEntityAssembler {

    private OrganizationResponseFromEntityAssembler() {
    }

    public static OrganizationResponse toResponse(Organization entity) {
        return new OrganizationResponse(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getEmail().value(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
