package com.alpacafkow.meditrack.organization.admin.domain.model.events;

import org.springframework.context.ApplicationEvent;

public class AdminUpdatedEvent extends ApplicationEvent {
    private final Long adminId;
    private final Long organizationId;

    public AdminUpdatedEvent(Object source, Long adminId, Long organizationId) {
        super(source);
        this.adminId = adminId;
        this.organizationId = organizationId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }
}
