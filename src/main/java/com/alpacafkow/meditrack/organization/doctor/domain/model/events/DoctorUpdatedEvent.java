package com.alpacafkow.meditrack.organization.doctor.domain.model.events;

import org.springframework.context.ApplicationEvent;

public class DoctorUpdatedEvent extends ApplicationEvent {
    private final Long doctorId;
    private final Long organizationId;

    public DoctorUpdatedEvent(Object source, Long doctorId, Long organizationId) {
        super(source);
        this.doctorId = doctorId;
        this.organizationId = organizationId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }
}
