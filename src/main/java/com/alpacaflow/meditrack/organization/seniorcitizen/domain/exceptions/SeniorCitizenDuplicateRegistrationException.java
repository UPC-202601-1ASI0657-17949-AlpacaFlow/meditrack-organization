package com.alpacaflow.meditrack.organization.seniorcitizen.domain.exceptions;

public class SeniorCitizenDuplicateRegistrationException extends RuntimeException {

    public static final String CODE_DUPLICATE_DNI = "DUPLICATE_DNI";
    public static final String CODE_DUPLICATE_FULL_NAME = "DUPLICATE_FULL_NAME";
    public static final String CODE_DEVICE_ALREADY_ASSIGNED = "DEVICE_ALREADY_ASSIGNED";

    private final String code;

    public SeniorCitizenDuplicateRegistrationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
