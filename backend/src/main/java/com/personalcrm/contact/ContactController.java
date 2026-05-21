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
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public List<ContactResponse> listContacts(Principal principal) {
        return contactService.listContacts(principal.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse createContact(Principal principal, @Valid @RequestBody ContactRequest request) {
        return contactService.createContact(principal.getName(), request);
    }

    @GetMapping("/{id}")
    public ContactResponse getContact(Principal principal, @PathVariable Long id) {
        return contactService.getContact(principal.getName(), id);
    }

    @PutMapping("/{id}")
    public ContactResponse updateContact(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest request
    ) {
        return contactService.updateContact(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContact(Principal principal, @PathVariable Long id) {
        contactService.deleteContact(principal.getName(), id);
    }
}
