package com.alpacafkow.meditrack.organization.admin.application.internal.queryservices;

import com.alpacafkow.meditrack.organization.admin.domain.model.aggregates.Admin;
import com.alpacafkow.meditrack.organization.admin.domain.model.queries.GetAdminByIdQuery;
import com.alpacafkow.meditrack.organization.admin.domain.model.queries.GetAdminByUserIdAndOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.admin.domain.model.queries.GetAllAdminsByOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.admin.domain.model.queries.GetAllAdminsQuery;
import com.alpacafkow.meditrack.organization.admin.infrastructure.persistence.jpa.repositories.AdminRepository;
import com.alpacafkow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacafkow.meditrack.organization.organization.domain.model.valueobjects.Email;
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
class AdminQueryServiceImplTest {

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AdminQueryServiceImpl queryService;

    private Admin sampleAdmin() {
        var organization = new Organization("Clinica X", "clinic", new Email("x@clinic.example"));
        return new Admin(organization, 1001L, "Ana", "Torres");
    }

    @Test
    void shouldDelegateFindById() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin()));

        var result = queryService.handle(new GetAdminByIdQuery(1L));

        assertTrue(result.isPresent());
        assertEquals("Ana", result.get().getFirstName());
        verify(adminRepository).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenAdminDoesNotExist() {
        when(adminRepository.findById(99L)).thenReturn(Optional.empty());
        var result = queryService.handle(new GetAdminByIdQuery(99L));
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnAllAdmins() {
        when(adminRepository.findAll()).thenReturn(List.of(sampleAdmin(), sampleAdmin()));
        var all = queryService.handle(new GetAllAdminsQuery());
        assertEquals(2, all.size());
    }

    @Test
    void shouldReturnAdminsByOrganization() {
        when(adminRepository.findByOrganization_Id(1L)).thenReturn(List.of(sampleAdmin()));
        var result = queryService.handle(new GetAllAdminsByOrganizationIdQuery(1L));
        assertEquals(1, result.size());
        verify(adminRepository).findByOrganization_Id(1L);
    }

    @Test
    void shouldFindAdminByUserAndOrganization() {
        when(adminRepository.findByUserIdAndOrganization_Id(1001L, 1L))
                .thenReturn(Optional.of(sampleAdmin()));

        var result = queryService.handle(new GetAdminByUserIdAndOrganizationIdQuery(1001L, 1L));
        assertTrue(result.isPresent());
        verify(adminRepository).findByUserIdAndOrganization_Id(1001L, 1L);
    }
}
