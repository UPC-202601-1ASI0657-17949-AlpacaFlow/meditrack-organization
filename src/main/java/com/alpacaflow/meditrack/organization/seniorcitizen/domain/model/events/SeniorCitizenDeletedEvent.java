package com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.events;

import org.springframework.context.ApplicationEvent;

public class SeniorCitizenDeletedEvent extends ApplicationEvent {
    private final Long seniorCitizenId;
    private final Long organizationId;

    public SeniorCitizenDeletedEvent(Object source, Long seniorCitizenId, Long organizationId) {
        super(source);
        this.seniorCitizenId = seniorCitizenId;
        this.organizationId = organizationId;
    }

    public Long getSeniorCitizenId() {
        return seniorCitizenId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }
}
