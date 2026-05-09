package com.alpacafkow.meditrack.organization.doctor.interfaces.rest;

import com.alpacafkow.meditrack.organization.doctor.domain.model.commands.DeleteDoctorCommand;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetAllDoctorsByOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetAllDoctorsQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetDoctorByIdQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetDoctorByUserIdAndOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.model.queries.GetDoctorByUserIdQuery;
import com.alpacafkow.meditrack.organization.doctor.domain.services.DoctorCommandService;
import com.alpacafkow.meditrack.organization.doctor.domain.services.DoctorQueryService;
import com.alpacafkow.meditrack.organization.doctor.interfaces.rest.resources.request.CreateDoctorRequest;
import com.alpacafkow.meditrack.organization.doctor.interfaces.rest.resources.request.UpdateDoctorRequest;
import com.alpacafkow.meditrack.organization.doctor.interfaces.rest.resources.response.DoctorResponse;
import com.alpacafkow.meditrack.organization.doctor.interfaces.rest.transform.CreateDoctorCommandFromRequestAssembler;
import com.alpacafkow.meditrack.organization.doctor.interfaces.rest.transform.DoctorResponseFromEntityAssembler;
import com.alpacafkow.meditrack.organization.doctor.interfaces.rest.transform.UpdateDoctorCommandFromRequestAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/doctors", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Doctors", description = "Doctor entity within the organization bounded context")
public class DoctorsController {

    private final DoctorCommandService doctorCommandService;
    private final DoctorQueryService doctorQueryService;

    public DoctorsController(DoctorCommandService doctorCommandService,
                             DoctorQueryService doctorQueryService) {
        this.doctorCommandService = doctorCommandService;
        this.doctorQueryService = doctorQueryService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Organization or user not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate doctor (email or full name) within organization")
    })
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        var command = CreateDoctorCommandFromRequestAssembler.toCommand(request);
        var id = doctorCommandService.handle(command);
        var doctor = doctorQueryService.handle(new GetDoctorByIdQuery(id)).orElseThrow();
        return new ResponseEntity<>(DoctorResponseFromEntityAssembler.toResponse(doctor), HttpStatus.CREATED);
    }

    @GetMapping("/{doctorId}")
    @Operation(summary = "Get doctor by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable Long doctorId) {
        return doctorQueryService.handle(new GetDoctorByIdQuery(doctorId))
                .map(DoctorResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all doctors")
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        var doctors = doctorQueryService.handle(new GetAllDoctorsQuery());
        var body = doctors.stream()
                .map(DoctorResponseFromEntityAssembler::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "List doctors by organization")
    public ResponseEntity<List<DoctorResponse>> getDoctorsByOrganization(@PathVariable Long organizationId) {
        var doctors = doctorQueryService.handle(new GetAllDoctorsByOrganizationIdQuery(organizationId));
        var body = doctors.stream()
                .map(DoctorResponseFromEntityAssembler::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get doctor by IAM user id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<DoctorResponse> getDoctorByUserId(@PathVariable Long userId) {
        return doctorQueryService.handle(new GetDoctorByUserIdQuery(userId))
                .map(DoctorResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/organization/{organizationId}")
    @Operation(summary = "Get doctor by user and organization (multi-tenant lookup)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<DoctorResponse> getDoctorByUserIdAndOrganizationId(@PathVariable Long userId,
                                                                              @PathVariable Long organizationId) {
        return doctorQueryService.handle(new GetDoctorByUserIdAndOrganizationIdQuery(userId, organizationId))
                .map(DoctorResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/{doctorId}", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate doctor in organization")
    })
    public ResponseEntity<DoctorResponse> updateDoctor(@PathVariable Long doctorId,
                                                       @Valid @RequestBody UpdateDoctorRequest request) {
        var command = UpdateDoctorCommandFromRequestAssembler.toCommand(doctorId, request);
        return doctorCommandService.handle(command)
                .map(DoctorResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{doctorId}")
    @Operation(summary = "Delete doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long doctorId) {
        doctorCommandService.handle(new DeleteDoctorCommand(doctorId));
        return ResponseEntity.noContent().build();
    }
}
