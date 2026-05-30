package com.alpacaflow.meditrack.organization.shared.infrastructure.messaging;

/**
 * JMS destination names shared with IAM (keep in sync manually across repos).
 */
public final class MessagingQueueNames {

    public static final String ADMIN_REGISTRATION_REQUESTED = "meditrack.iam.admin-registration.requested";
    public static final String STAFF_PROVISION_REQUESTED = "meditrack.organization.staff-provision.requested";

    private MessagingQueueNames() {
    }
}
