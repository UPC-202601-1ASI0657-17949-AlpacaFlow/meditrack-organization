package com.alpacaflow.meditrack.organization.caregiver.domain.exceptions;

public class CaregiverDuplicateRegistrationException extends RuntimeException {

    public static final String CODE_DUPLICATE_EMAIL = "DUPLICATE_EMAIL";
    public static final String CODE_DUPLICATE_FULL_NAME = "DUPLICATE_FULL_NAME";

    private final String code;

    public CaregiverDuplicateRegistrationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
