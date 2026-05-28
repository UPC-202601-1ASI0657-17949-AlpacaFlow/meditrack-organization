package com.alpacaflow.meditrack.organization.caregiver.application.internal.queryservices;

import com.alpacaflow.meditrack.organization.caregiver.domain.model.aggregates.Caregiver;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetAllCaregiversByOrganizationIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetAllCaregiversQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetCaregiverByIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetCaregiverByUserIdAndOrganizationIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.domain.model.queries.GetCaregiverByUserIdQuery;
import com.alpacaflow.meditrack.organization.caregiver.infrastructure.persistence.jpa.repositories.CaregiverRepository;
import com.alpacaflow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacaflow.meditrack.organization.organization.domain.model.valueobjects.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaregiverQueryServiceImplTest {

    @Mock
    private CaregiverRepository caregiverRepository;

    @InjectMocks
    private CaregiverQueryServiceImpl queryService;

    private Caregiver sampleCaregiver() {
        var organization = new Organization("Clinica X", "clinic", new Email("x@clinic.example"));
        return new Caregiver(organization, 1003L, "Lucia", "Torres", 32,
                "lucia@clinic.example", "999111222", "https://x/y.png");
    }

    @Test
    void shouldDelegateFindById() {
        when(caregiverRepository.findById(1L)).thenReturn(Optional.of(sampleCaregiver()));
        var result = queryService.handle(new GetCaregiverByIdQuery(1L));
        assertTrue(result.isPresent());
        assertEquals("Lucia", result.get().getFirstName());
        verify(caregiverRepository).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenCaregiverDoesNotExist() {
        when(caregiverRepository.findById(99L)).thenReturn(Optional.empty());
        assertTrue(queryService.handle(new GetCaregiverByIdQuery(99L)).isEmpty());
    }

    @Test
    void shouldReturnAllCaregivers() {
        when(caregiverRepository.findAll()).thenReturn(List.of(sampleCaregiver(), sampleCaregiver()));
        assertEquals(2, queryService.handle(new GetAllCaregiversQuery()).size());
    }

    @Test
    void shouldReturnCaregiversByOrganization() {
        when(caregiverRepository.findByOrganization_Id(1L)).thenReturn(List.of(sampleCaregiver()));
        var result = queryService.handle(new GetAllCaregiversByOrganizationIdQuery(1L));
        assertEquals(1, result.size());
        verify(caregiverRepository).findByOrganization_Id(1L);
    }

    @Test
    void shouldFindCaregiverByUserId() {
        when(caregiverRepository.findByUserId(1003L)).thenReturn(Optional.of(sampleCaregiver()));
        assertTrue(queryService.handle(new GetCaregiverByUserIdQuery(1003L)).isPresent());
    }

    @Test
    void shouldFindCaregiverByUserAndOrganization() {
        when(caregiverRepository.findByUserIdAndOrganization_Id(1003L, 1L))
                .thenReturn(Optional.of(sampleCaregiver()));
        assertTrue(queryService.handle(new GetCaregiverByUserIdAndOrganizationIdQuery(1003L, 1L)).isPresent());
        verify(caregiverRepository).findByUserIdAndOrganization_Id(1003L, 1L);
    }
}
