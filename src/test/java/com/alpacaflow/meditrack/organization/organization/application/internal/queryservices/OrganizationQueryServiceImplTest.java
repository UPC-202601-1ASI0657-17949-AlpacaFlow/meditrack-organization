package com.alpacaflow.meditrack.organization.organization.application.internal.queryservices;

import com.alpacaflow.meditrack.organization.organization.domain.model.queries.GetOrganizationByIdQuery;
import com.alpacaflow.meditrack.organization.organization.infrastructure.persistence.jpa.repositories.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationQueryServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationQueryServiceImpl queryService;

    @Test
    void shouldReturnTrueWhenNameIsBlankInAvailabilityCheck() {
        var result = queryService.isOrganizationNameAvailable("  ");
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenNameAlreadyExists() {
        when(organizationRepository.existsByNameIgnoreCase("clinica a")).thenReturn(true);
        var result = queryService.isOrganizationNameAvailable("clinica a");
        assertFalse(result);
    }

    @Test
    void shouldDelegateFindByIdToRepository() {
        var query = new GetOrganizationByIdQuery(1L);
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());
        queryService.handle(query);
        verify(organizationRepository).findById(1L);
    }
}
