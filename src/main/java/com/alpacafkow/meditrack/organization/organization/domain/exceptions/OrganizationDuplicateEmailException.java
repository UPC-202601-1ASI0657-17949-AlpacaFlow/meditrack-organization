package com.alpacafkow.meditrack.organization.organization.domain.exceptions;

public class OrganizationDuplicateEmailException extends IllegalStateException {
    public static final String CODE_DUPLICATE_EMAIL = "ORGANIZATION_DUPLICATE_EMAIL";

    private final String code;

    public OrganizationDuplicateEmailException(String message) {
        super(message);
        this.code = CODE_DUPLICATE_EMAIL;
    }

    public String getCode() {
        return code;
    }
}
