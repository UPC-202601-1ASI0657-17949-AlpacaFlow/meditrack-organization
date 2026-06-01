package com.alpacaflow.meditrack.organization.admin.interfaces.rest;

import com.alpacaflow.meditrack.organization.admin.domain.model.queries.GetAdminByIdQuery;
import com.alpacaflow.meditrack.organization.admin.domain.services.AdminCommandService;
import com.alpacaflow.meditrack.organization.admin.domain.services.AdminQueryService;
import com.alpacaflow.meditrack.organization.admin.interfaces.rest.resources.request.CreateAdminRequest;
import com.alpacaflow.meditrack.organization.admin.interfaces.rest.resources.response.AdminResponse;
import com.alpacaflow.meditrack.organization.admin.interfaces.rest.transform.AdminResponseFromEntityAssembler;
import com.alpacaflow.meditrack.organization.admin.interfaces.rest.transform.CreateAdminCommandFromRequestAssembler;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Server-to-server ACL used by IAM when {@code app.organization.adapter=rest}.
 */
@Hidden
@RestController
@RequestMapping(value = "/api/v1/internal/admins", produces = APPLICATION_JSON_VALUE)
public class InternalAdminsController {

    private final AdminCommandService adminCommandService;
    private final AdminQueryService adminQueryService;

    public InternalAdminsController(AdminCommandService adminCommandService,
                                    AdminQueryService adminQueryService) {
        this.adminCommandService = adminCommandService;
        this.adminQueryService = adminQueryService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminResponse> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        var command = CreateAdminCommandFromRequestAssembler.toCommand(request);
        var id = adminCommandService.handle(command);
        var admin = adminQueryService.handle(new GetAdminByIdQuery(id)).orElseThrow();
        return new ResponseEntity<>(AdminResponseFromEntityAssembler.toResponse(admin), HttpStatus.CREATED);
    }
}
