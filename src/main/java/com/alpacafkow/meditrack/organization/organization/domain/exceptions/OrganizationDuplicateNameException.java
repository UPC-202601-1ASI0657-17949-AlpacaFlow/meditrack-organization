package com.alpacafkow.meditrack.organization.organization.domain.exceptions;

public class OrganizationDuplicateNameException extends IllegalStateException {
    public static final String CODE_DUPLICATE_NAME = "ORGANIZATION_DUPLICATE_NAME";

    private final String code;

    public OrganizationDuplicateNameException(String message) {
        super(message);
        this.code = CODE_DUPLICATE_NAME;
    }

    public String getCode() {
        return code;
    }
}
