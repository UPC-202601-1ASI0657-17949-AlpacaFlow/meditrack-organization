package com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.events;

import org.springframework.context.ApplicationEvent;

public class SeniorCitizenAssignedToCaregiverEvent extends ApplicationEvent {
    private final Long seniorCitizenId;
    private final Long caregiverId;
    private final Long organizationId;

    public SeniorCitizenAssignedToCaregiverEvent(Object source, Long seniorCitizenId, Long caregiverId, Long organizationId) {
        super(source);
        this.seniorCitizenId = seniorCitizenId;
        this.caregiverId = caregiverId;
        this.organizationId = organizationId;
    }

    public Long getSeniorCitizenId() {
        return seniorCitizenId;
    }

    public Long getCaregiverId() {
        return caregiverId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }
}
