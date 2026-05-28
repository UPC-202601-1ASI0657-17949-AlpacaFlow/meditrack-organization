package com.alpacaflow.meditrack.organization.seniorcitizen.domain.model.queries;

public record GetSeniorCitizenByIdQuery(Long seniorCitizenId) {
    public GetSeniorCitizenByIdQuery {
        if (seniorCitizenId == null || seniorCitizenId <= 0) {
            throw new IllegalArgumentException("seniorCitizenId cannot be null or less than 1");
        }
    }
}
