package com.personalcrm.contact;

import com.personalcrm.user.User;
import com.personalcrm.user.UserRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademicFormationService {

    private final AcademicFormationRepository academicFormationRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public AcademicFormationService(
            AcademicFormationRepository academicFormationRepository,
            ContactRepository contactRepository,
            UserRepository userRepository
    ) {
        this.academicFormationRepository = academicFormationRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AcademicFormationResponse> listFormations(String authenticatedEmail, Long contactId) {
        Contact contact = findOwnedContact(authenticatedEmail, contactId);

        return academicFormationRepository.findByContactIdOrderByStartDateDescInstitutionAsc(contact.getId())
                .stream()
                .map(AcademicFormationResponse::from)
                .toList();
    }

    @Transactional
    public AcademicFormationResponse createFormation(
            String authenticatedEmail,
            Long contactId,
            AcademicFormationRequest request
    ) {
        Contact contact = findOwnedContact(authenticatedEmail, contactId);
        AcademicFormation formation = new AcademicFormation(
                contact,
                normalizeRequired(request.institution()),
                normalizeOptional(request.degree()),
                normalizeOptional(request.fieldOfStudy()),
                request.startDate(),
                request.endDate(),
                normalizeOptional(request.description())
        );

        return AcademicFormationResponse.from(academicFormationRepository.save(formation));
    }

    @Transactional(readOnly = true)
    public AcademicFormationResponse getFormation(String authenticatedEmail, Long contactId, Long formationId) {
        findOwnedContact(authenticatedEmail, contactId);
        AcademicFormation formation = findFormation(contactId, formationId);

        return AcademicFormationResponse.from(formation);
    }

    @Transactional
    public AcademicFormationResponse updateFormation(
            String authenticatedEmail,
            Long contactId,
            Long formationId,
            AcademicFormationRequest request
    ) {
        findOwnedContact(authenticatedEmail, contactId);
        AcademicFormation formation = findFormation(contactId, formationId);
        formation.updateDetails(
                normalizeRequired(request.institution()),
                normalizeOptional(request.degree()),
                normalizeOptional(request.fieldOfStudy()),
                request.startDate(),
                request.endDate(),
                normalizeOptional(request.description())
        );

        return AcademicFormationResponse.from(formation);
    }

    @Transactional
    public void deleteFormation(String authenticatedEmail, Long contactId, Long formationId) {
        findOwnedContact(authenticatedEmail, contactId);
        AcademicFormation formation = findFormation(contactId, formationId);

        academicFormationRepository.delete(formation);
    }

    private Contact findOwnedContact(String authenticatedEmail, Long contactId) {
        User owner = userRepository.findByEmail(normalizeEmail(authenticatedEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return contactRepository.findByIdAndOwnerId(contactId, owner.getId())
                .orElseThrow(() -> new ContactNotFoundException(contactId));
    }

    private AcademicFormation findFormation(Long contactId, Long formationId) {
        return academicFormationRepository.findByIdAndContactId(formationId, contactId)
                .orElseThrow(() -> new AcademicFormationNotFoundException(formationId));
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
