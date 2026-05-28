package com.alpacaflow.meditrack.organization.caregiver.domain.model.aggregates;

import com.alpacaflow.meditrack.organization.caregiver.domain.model.CaregiverInputRules;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.events.CaregiverCreatedEvent;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.events.CaregiverDeletedEvent;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.events.CaregiverUpdatedEvent;
import com.alpacaflow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacaflow.meditrack.organization.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * Caregiver aggregate within the organization microservice.
 * <p>
 * Represents a caregiver that belongs to one {@link Organization} and is linked to a user
 * account managed by the IAM bounded context (referenced through {@code userId}).
 * <p>
 * Like {@code Doctor}, assignments to senior citizens are not modeled here in Sprint 1: the
 * relationship will be owned by the {@code SeniorCitizen} aggregate, which will publish the
 * domain events that this aggregate may subscribe to in a later iteration.
 */
@Entity
@Table(name = "caregivers")
@Getter
public class Caregiver extends AuditableAbstractAggregateRoot<Caregiver> {

    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    protected Caregiver() {
    }

    public Caregiver(Organization organization, Long userId, String firstName, String lastName, Integer age,
                     String email, String phoneNumber, String imageUrl) {
        this.organization = validateOrganization(organization);
        this.userId = validateUserId(userId);
        this.firstName = validateRequired(firstName, "firstName");
        this.lastName = validateRequired(lastName, "lastName");
        CaregiverInputRules.assertCaregiverAge(age);
        this.age = age;
        this.email = validateRequired(email, "email");
        CaregiverInputRules.assertCaregiverPhoneDigitsOnly(phoneNumber);
        this.phoneNumber = phoneNumber.trim();
        this.imageUrl = validateRequired(imageUrl, "imageUrl");
    }

    public void publishCreatedEvent() {
        this.addDomainEvent(new CaregiverCreatedEvent(this, this.getId(), this.getOrganizationId()));
    }

    public Caregiver updatePersonalInformation(String firstName, String lastName, Integer age,
                                                String email, String phoneNumber) {
        this.firstName = validateRequired(firstName, "firstName");
        this.lastName = validateRequired(lastName, "lastName");
        CaregiverInputRules.assertCaregiverAge(age);
        this.age = age;
        this.email = validateRequired(email, "email");
        CaregiverInputRules.assertCaregiverPhoneDigitsOnly(phoneNumber);
        this.phoneNumber = phoneNumber.trim();
        this.addDomainEvent(new CaregiverUpdatedEvent(this, this.getId(), this.getOrganizationId()));
        return this;
    }

    public Caregiver updateImageUrl(String imageUrl) {
        this.imageUrl = validateRequired(imageUrl, "imageUrl");
        this.addDomainEvent(new CaregiverUpdatedEvent(this, this.getId(), this.getOrganizationId()));
        return this;
    }

    public void markForDeletion() {
        this.addDomainEvent(new CaregiverDeletedEvent(this, this.getId(), this.getOrganizationId()));
    }

    public Long getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }

    public boolean belongsToOrganization(Long organizationId) {
        return this.organization != null && this.organization.getId() != null
                && this.organization.getId().equals(organizationId);
    }

    private Organization validateOrganization(Organization value) {
        if (value == null) {
            throw new IllegalArgumentException("organization cannot be null");
        }
        return value;
    }

    private Long validateUserId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("userId must be a positive number");
        }
        return value;
    }

    private String validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        return value.trim();
    }
}
