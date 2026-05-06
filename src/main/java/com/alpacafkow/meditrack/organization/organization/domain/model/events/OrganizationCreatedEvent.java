package com.alpacafkow.meditrack.organization.organization.domain.model.events;

import org.springframework.context.ApplicationEvent;

public class OrganizationCreatedEvent extends ApplicationEvent {
    private final Long organizationId;

    public OrganizationCreatedEvent(Object source, Long organizationId) {
        super(source);
        this.organizationId = organizationId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }
}
