package com.alpacaflow.meditrack.organization.caregiver.domain.model.events;

import org.springframework.context.ApplicationEvent;

public class CaregiverCreatedEvent extends ApplicationEvent {
    private final Long caregiverId;
    private final Long organizationId;

    public CaregiverCreatedEvent(Object source, Long caregiverId, Long organizationId) {
        super(source);
        this.caregiverId = caregiverId;
        this.organizationId = organizationId;
    }

    public Long getCaregiverId() {
        return caregiverId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }
}
