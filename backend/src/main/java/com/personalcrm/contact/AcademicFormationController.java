package com.personalcrm.contact;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contacts/{contactId}/academic-formations")
public class AcademicFormationController {

    private final AcademicFormationService academicFormationService;

    public AcademicFormationController(AcademicFormationService academicFormationService) {
        this.academicFormationService = academicFormationService;
    }

    @GetMapping
    public List<AcademicFormationResponse> listFormations(
            Principal principal,
            @PathVariable Long contactId
    ) {
        return academicFormationService.listFormations(principal.getName(), contactId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AcademicFormationResponse createFormation(
            Principal principal,
            @PathVariable Long contactId,
            @Valid @RequestBody AcademicFormationRequest request
    ) {
        return academicFormationService.createFormation(principal.getName(), contactId, request);
    }

    @GetMapping("/{formationId}")
    public AcademicFormationResponse getFormation(
            Principal principal,
            @PathVariable Long contactId,
            @PathVariable Long formationId
    ) {
        return academicFormationService.getFormation(principal.getName(), contactId, formationId);
    }

    @PutMapping("/{formationId}")
    public AcademicFormationResponse updateFormation(
            Principal principal,
            @PathVariable Long contactId,
            @PathVariable Long formationId,
            @Valid @RequestBody AcademicFormationRequest request
    ) {
        return academicFormationService.updateFormation(principal.getName(), contactId, formationId, request);
    }

    @DeleteMapping("/{formationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFormation(
            Principal principal,
            @PathVariable Long contactId,
            @PathVariable Long formationId
    ) {
        academicFormationService.deleteFormation(principal.getName(), contactId, formationId);
    }
}
