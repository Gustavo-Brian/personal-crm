package com.personalcrm.contact;

import com.personalcrm.user.User;
import com.personalcrm.user.UserRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImportantDateService {

    private final ImportantDateRepository importantDateRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public ImportantDateService(
            ImportantDateRepository importantDateRepository,
            ContactRepository contactRepository,
            UserRepository userRepository
    ) {
        this.importantDateRepository = importantDateRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ImportantDateResponse> listImportantDates(String authenticatedEmail, Long contactId) {
        Contact contact = findOwnedContact(authenticatedEmail, contactId);

        return importantDateRepository.findByContactIdOrderByDateAscTitleAsc(contact.getId())
                .stream()
                .map(ImportantDateResponse::from)
                .toList();
    }

    @Transactional
    public ImportantDateResponse createImportantDate(
            String authenticatedEmail,
            Long contactId,
            ImportantDateRequest request
    ) {
        Contact contact = findOwnedContact(authenticatedEmail, contactId);
        ImportantDate importantDate = new ImportantDate(
                contact,
                normalizeRequired(request.title()),
                request.date(),
                request.type(),
                normalizeOptional(request.description())
        );

        return ImportantDateResponse.from(importantDateRepository.save(importantDate));
    }

    @Transactional(readOnly = true)
    public ImportantDateResponse getImportantDate(String authenticatedEmail, Long contactId, Long importantDateId) {
        findOwnedContact(authenticatedEmail, contactId);
        ImportantDate importantDate = findImportantDate(contactId, importantDateId);

        return ImportantDateResponse.from(importantDate);
    }

    @Transactional
    public ImportantDateResponse updateImportantDate(
            String authenticatedEmail,
            Long contactId,
            Long importantDateId,
            ImportantDateRequest request
    ) {
        findOwnedContact(authenticatedEmail, contactId);
        ImportantDate importantDate = findImportantDate(contactId, importantDateId);
        importantDate.updateDetails(
                normalizeRequired(request.title()),
                request.date(),
                request.type(),
                normalizeOptional(request.description())
        );

        return ImportantDateResponse.from(importantDate);
    }

    @Transactional
    public void deleteImportantDate(String authenticatedEmail, Long contactId, Long importantDateId) {
        findOwnedContact(authenticatedEmail, contactId);
        ImportantDate importantDate = findImportantDate(contactId, importantDateId);

        importantDateRepository.delete(importantDate);
    }

    private Contact findOwnedContact(String authenticatedEmail, Long contactId) {
        User owner = userRepository.findByEmail(normalizeEmail(authenticatedEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return contactRepository.findByIdAndOwnerId(contactId, owner.getId())
                .orElseThrow(() -> new ContactNotFoundException(contactId));
    }

    private ImportantDate findImportantDate(Long contactId, Long importantDateId) {
        return importantDateRepository.findByIdAndContactId(importantDateId, contactId)
                .orElseThrow(() -> new ImportantDateNotFoundException(importantDateId));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
