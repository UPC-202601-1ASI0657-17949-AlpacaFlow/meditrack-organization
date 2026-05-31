package com.alpacaflow.meditrack.organization.shared.infrastructure.messaging;

/**
 * Logical JMS type ids shared with IAM (keep in sync manually across repos).
 */
public final class MessagingTypeIds {

    public static final String ADMIN_REGISTRATION_REQUEST = "meditrack.AdminRegistrationRequest";
    public static final String STAFF_PROVISION_REQUEST = "meditrack.StaffProvisionRequest";
    public static final String STAFF_PROVISION_RESPONSE = "meditrack.StaffProvisionResponse";

    private MessagingTypeIds() {
    }
}
