package com.alpacafkow.meditrack.organization.organization.domain.model.aggregates;

import com.alpacafkow.meditrack.organization.organization.domain.model.events.OrganizationCreatedEvent;
import com.alpacafkow.meditrack.organization.organization.domain.model.events.OrganizationUpdatedEvent;
import com.alpacafkow.meditrack.organization.organization.domain.model.valueobjects.Email;
import com.alpacafkow.meditrack.organization.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Table(
        name = "organizations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_organization_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_organization_email", columnNames = "email")
        }
)
@Getter
public class Organization extends AuditableAbstractAggregateRoot<Organization> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false))
    private Email email;

    protected Organization() {
    }

    public Organization(String name, String type, Email email) {
        this.name = validateName(name);
        this.type = validateType(type);
        this.email = email;
    }

    public void publishCreatedEvent() {
        this.addDomainEvent(new OrganizationCreatedEvent(this, this.getId()));
    }

    public Organization updateInformation(String name, String type, Email email) {
        this.name = validateName(name);
        this.type = validateType(type);
        this.email = email;
        this.addDomainEvent(new OrganizationUpdatedEvent(this, this.getId()));
        return this;
    }

    private String validateName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        return value.trim();
    }

    private String validateType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Type cannot be null or blank");
        }
        var normalized = value.toLowerCase().trim();
        if (!"clinic".equals(normalized) && !"resident".equals(normalized)) {
            throw new IllegalArgumentException("Type must be either 'clinic' or 'resident'");
        }
        return normalized;
    }
}
