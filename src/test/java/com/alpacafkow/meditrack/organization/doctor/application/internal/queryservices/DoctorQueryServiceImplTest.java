package com.alpacafkow.meditrack.organization.doctor.application.internal.queryservices;

import com.alpacafkow.meditrack.organization.doctor.domain.model.aggregates.Doctor;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetAllDoctorsByOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetAllDoctorsQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetDoctorByIdQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetDoctorByUserIdAndOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetDoctorByUserIdQuery;
import com.alpacafkow.meditrack.organization.doctor.infrastructure.persistence.jpa.repositories.DoctorRepository;
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
class DoctorQueryServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorQueryServiceImpl queryService;

    private Doctor sampleDoctor() {
        var organization = new Organization("Clinica X", "clinic", new Email("x@clinic.example"));
        return new Doctor(organization, 1002L, "Carlos", "Mendoza", 45,
                "carlos@clinic.example", "Cardiology", "+51999", "https://x/y.png");
    }

    @Test
    void shouldDelegateFindById() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(sampleDoctor()));
        var result = queryService.handle(new GetDoctorByIdQuery(1L));
        assertTrue(result.isPresent());
        assertEquals("Carlos", result.get().getFirstName());
        verify(doctorRepository).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenDoctorDoesNotExist() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());
        assertTrue(queryService.handle(new GetDoctorByIdQuery(99L)).isEmpty());
    }

    @Test
    void shouldReturnAllDoctors() {
        when(doctorRepository.findAll()).thenReturn(List.of(sampleDoctor(), sampleDoctor()));
        assertEquals(2, queryService.handle(new GetAllDoctorsQuery()).size());
    }

    @Test
    void shouldReturnDoctorsByOrganization() {
        when(doctorRepository.findByOrganization_Id(1L)).thenReturn(List.of(sampleDoctor()));
        var result = queryService.handle(new GetAllDoctorsByOrganizationIdQuery(1L));
        assertEquals(1, result.size());
        verify(doctorRepository).findByOrganization_Id(1L);
    }

    @Test
    void shouldFindDoctorByUserId() {
        when(doctorRepository.findByUserId(1002L)).thenReturn(Optional.of(sampleDoctor()));
        assertTrue(queryService.handle(new GetDoctorByUserIdQuery(1002L)).isPresent());
    }

    @Test
    void shouldFindDoctorByUserAndOrganization() {
        when(doctorRepository.findByUserIdAndOrganization_Id(1002L, 1L))
                .thenReturn(Optional.of(sampleDoctor()));
        assertTrue(queryService.handle(new GetDoctorByUserIdAndOrganizationIdQuery(1002L, 1L)).isPresent());
        verify(doctorRepository).findByUserIdAndOrganization_Id(1002L, 1L);
    }
}
