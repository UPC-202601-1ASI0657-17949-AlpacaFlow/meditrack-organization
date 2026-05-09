package com.alpacafkow.meditrack.organization.seniorcitizen.domain.exceptions;

public class SeniorCitizenNotFoundException extends RuntimeException {
    public SeniorCitizenNotFoundException(Long seniorCitizenId) {
        super("Senior citizen with ID " + seniorCitizenId + " not found.");
    }
}
