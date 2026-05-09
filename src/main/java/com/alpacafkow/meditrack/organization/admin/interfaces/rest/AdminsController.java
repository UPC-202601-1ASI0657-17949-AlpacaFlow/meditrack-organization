package com.alpacafkow.meditrack.organization.admin.interfaces.rest;

import com.alpacafkow.meditrack.organization.admin.domain.model.commands.DeleteAdminCommand;
import com.alpacafkow.meditrack.organization.admin.domain.model.queries.GetAdminByIdQuery;
import com.alpacafkow.meditrack.organization.admin.domain.model.queries.GetAdminByUserIdAndOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.admin.domain.model.queries.GetAllAdminsByOrganizationIdQuery;
import com.alpacafkow.meditrack.organization.admin.domain.model.queries.GetAllAdminsQuery;
import com.alpacafkow.meditrack.organization.admin.domain.services.AdminCommandService;
import com.alpacafkow.meditrack.organization.admin.domain.services.AdminQueryService;
import com.alpacafkow.meditrack.organization.admin.interfaces.rest.resources.request.CreateAdminRequest;
import com.alpacafkow.meditrack.organization.admin.interfaces.rest.resources.request.UpdateAdminRequest;
import com.alpacafkow.meditrack.organization.admin.interfaces.rest.resources.response.AdminResponse;
import com.alpacafkow.meditrack.organization.admin.interfaces.rest.transform.AdminResponseFromEntityAssembler;
import com.alpacafkow.meditrack.organization.admin.interfaces.rest.transform.CreateAdminCommandFromRequestAssembler;
import com.alpacafkow.meditrack.organization.admin.interfaces.rest.transform.UpdateAdminCommandFromRequestAssembler;
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
@RequestMapping(value = "/api/v1/admins", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Admins", description = "Admin entity within the organization bounded context")
public class AdminsController {

    private final AdminCommandService adminCommandService;
    private final AdminQueryService adminQueryService;

    public AdminsController(AdminCommandService adminCommandService,
                            AdminQueryService adminQueryService) {
        this.adminCommandService = adminCommandService;
        this.adminQueryService = adminQueryService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Organization or user not found"),
            @ApiResponse(responseCode = "409", description = "Admin already exists for this user/organization")
    })
    public ResponseEntity<AdminResponse> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        var command = CreateAdminCommandFromRequestAssembler.toCommand(request);
        var id = adminCommandService.handle(command);
        var admin = adminQueryService.handle(new GetAdminByIdQuery(id)).orElseThrow();
        return new ResponseEntity<>(AdminResponseFromEntityAssembler.toResponse(admin), HttpStatus.CREATED);
    }

    @GetMapping("/{adminId}")
    @Operation(summary = "Get admin by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<AdminResponse> getAdminById(@PathVariable Long adminId) {
        return adminQueryService.handle(new GetAdminByIdQuery(adminId))
                .map(AdminResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all admins")
    @ApiResponse(responseCode = "200", description = "List (unpaginated in sprint 1)")
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {
        var admins = adminQueryService.handle(new GetAllAdminsQuery());
        var body = admins.stream()
                .map(AdminResponseFromEntityAssembler::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "List admins by organization")
    public ResponseEntity<List<AdminResponse>> getAdminsByOrganization(@PathVariable Long organizationId) {
        var admins = adminQueryService.handle(new GetAllAdminsByOrganizationIdQuery(organizationId));
        var body = admins.stream()
                .map(AdminResponseFromEntityAssembler::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/user/{userId}/organization/{organizationId}")
    @Operation(summary = "Get admin by user and organization (multi-tenant lookup)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<AdminResponse> getAdminByUserAndOrganization(@PathVariable Long userId,
                                                                        @PathVariable Long organizationId) {
        return adminQueryService.handle(new GetAdminByUserIdAndOrganizationIdQuery(userId, organizationId))
                .map(AdminResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/{adminId}", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update admin's personal information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<AdminResponse> updateAdmin(@PathVariable Long adminId,
                                                     @Valid @RequestBody UpdateAdminRequest request) {
        var command = UpdateAdminCommandFromRequestAssembler.toCommand(adminId, request);
        return adminCommandService.handle(command)
                .map(AdminResponseFromEntityAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{adminId}")
    @Operation(summary = "Delete admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long adminId) {
        adminCommandService.handle(new DeleteAdminCommand(adminId));
        return ResponseEntity.noContent().build();
    }
}
