package com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.events;

import org.springframework.context.ApplicationEvent;

public class SeniorCitizenAssignedToDoctorEvent extends ApplicationEvent {
    private final Long seniorCitizenId;
    private final Long doctorId;
    private final Long organizationId;

    public SeniorCitizenAssignedToDoctorEvent(Object source, Long seniorCitizenId, Long doctorId, Long organizationId) {
        super(source);
        this.seniorCitizenId = seniorCitizenId;
        this.doctorId = doctorId;
        this.organizationId = organizationId;
    }

    public Long getSeniorCitizenId() {
        return seniorCitizenId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }
}
