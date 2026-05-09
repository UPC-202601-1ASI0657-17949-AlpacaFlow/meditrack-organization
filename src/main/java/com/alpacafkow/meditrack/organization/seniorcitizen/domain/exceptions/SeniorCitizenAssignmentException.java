package com.alpacafkow.meditrack.organization.seniorcitizen.domain.exceptions;

public class SeniorCitizenAssignmentException extends RuntimeException {

    public static final String CODE_ALREADY_ASSIGNED_TO_OTHER_TYPE = "ALREADY_ASSIGNED_TO_OTHER_TYPE";
    public static final String CODE_DIFFERENT_ORGANIZATION = "DIFFERENT_ORGANIZATION";

    private final String code;

    public SeniorCitizenAssignmentException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
