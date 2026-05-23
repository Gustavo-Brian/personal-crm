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
@RequestMapping("/contacts/{contactId}/important-dates")
public class ImportantDateController {

    private final ImportantDateService importantDateService;

    public ImportantDateController(ImportantDateService importantDateService) {
        this.importantDateService = importantDateService;
    }

    @GetMapping
    public List<ImportantDateResponse> listImportantDates(
            Principal principal,
            @PathVariable Long contactId
    ) {
        return importantDateService.listImportantDates(principal.getName(), contactId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImportantDateResponse createImportantDate(
            Principal principal,
            @PathVariable Long contactId,
            @Valid @RequestBody ImportantDateRequest request
    ) {
        return importantDateService.createImportantDate(principal.getName(), contactId, request);
    }

    @GetMapping("/{importantDateId}")
    public ImportantDateResponse getImportantDate(
            Principal principal,
            @PathVariable Long contactId,
            @PathVariable Long importantDateId
    ) {
        return importantDateService.getImportantDate(principal.getName(), contactId, importantDateId);
    }

    @PutMapping("/{importantDateId}")
    public ImportantDateResponse updateImportantDate(
            Principal principal,
            @PathVariable Long contactId,
            @PathVariable Long importantDateId,
            @Valid @RequestBody ImportantDateRequest request
    ) {
        return importantDateService.updateImportantDate(
                principal.getName(),
                contactId,
                importantDateId,
                request
        );
    }

    @DeleteMapping("/{importantDateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImportantDate(
            Principal principal,
            @PathVariable Long contactId,
            @PathVariable Long importantDateId
    ) {
        importantDateService.deleteImportantDate(principal.getName(), contactId, importantDateId);
    }
}
