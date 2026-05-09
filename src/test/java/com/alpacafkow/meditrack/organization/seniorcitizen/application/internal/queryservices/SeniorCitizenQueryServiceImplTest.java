package com.alpacafkow.meditrack.organization.seniorcitizen.application.internal.queryservices;

import com.alpacafkow.meditrack.organization.organization.domain.model.aggregates.Organization;
import com.alpacafkow.meditrack.organization.organization.domain.model.valueobjects.Email;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.aggregates.SeniorCitizen;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.queries.GetAllSeniorCitizensByOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.queries.GetAllSeniorCitizensQuery;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.queries.GetSeniorCitizenByIdQuery;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.queries.GetSeniorCitizensByAssignedCaregiverIdQuery;
import com.alpacafkow.meditrack.organization.seniorcitizen.domain.model.queries.GetSeniorCitizensByAssignedDoctorIdQuery;
import com.alpacafkow.meditrack.organization.seniorcitizen.infrastructure.persistence.jpa.repositories.SeniorCitizenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeniorCitizenQueryServiceImplTest {

    @Mock
    private SeniorCitizenRepository seniorCitizenRepository;

    @InjectMocks
    private SeniorCitizenQueryServiceImpl queryService;

    private static Date birthDateForAge(int years) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -years);
        return cal.getTime();
    }

    private SeniorCitizen sampleSenior() {
        var organization = new Organization("Clinica X", "clinic", new Email("x@clinic.example"));
        return new SeniorCitizen(organization, "Maria", "Quispe", birthDateForAge(70),
                "Femenino", 62.5, "12345678", 160.0, "https://x/y.png", 5001L);
    }

    @Test
    void shouldDelegateFindById() {
        when(seniorCitizenRepository.findById(1L)).thenReturn(Optional.of(sampleSenior()));
        var result = queryService.handle(new GetSeniorCitizenByIdQuery(1L));
        assertTrue(result.isPresent());
        assertEquals("Maria", result.get().getFirstName());
        verify(seniorCitizenRepository).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenSeniorDoesNotExist() {
        when(seniorCitizenRepository.findById(99L)).thenReturn(Optional.empty());
        assertTrue(queryService.handle(new GetSeniorCitizenByIdQuery(99L)).isEmpty());
    }

    @Test
    void shouldReturnAllSeniors() {
        when(seniorCitizenRepository.findAll()).thenReturn(List.of(sampleSenior(), sampleSenior()));
        assertEquals(2, queryService.handle(new GetAllSeniorCitizensQuery()).size());
    }

    @Test
    void shouldReturnSeniorsByOrganization() {
        when(seniorCitizenRepository.findByOrganization_Id(1L)).thenReturn(List.of(sampleSenior()));
        var result = queryService.handle(new GetAllSeniorCitizensByOrganizationIdQuery(1L));
        assertEquals(1, result.size());
        verify(seniorCitizenRepository).findByOrganization_Id(1L);
    }

    @Test
    void shouldReturnSeniorsAssignedToDoctor() {
        when(seniorCitizenRepository.findByAssignedDoctorId(20L)).thenReturn(List.of(sampleSenior()));
        var result = queryService.handle(new GetSeniorCitizensByAssignedDoctorIdQuery(20L));
        assertEquals(1, result.size());
        verify(seniorCitizenRepository).findByAssignedDoctorId(20L);
    }

    @Test
    void shouldReturnSeniorsAssignedToCaregiver() {
        when(seniorCitizenRepository.findByAssignedCaregiverId(30L)).thenReturn(List.of(sampleSenior()));
        var result = queryService.handle(new GetSeniorCitizensByAssignedCaregiverIdQuery(30L));
        assertEquals(1, result.size());
        verify(seniorCitizenRepository).findByAssignedCaregiverId(30L);
    }
}
