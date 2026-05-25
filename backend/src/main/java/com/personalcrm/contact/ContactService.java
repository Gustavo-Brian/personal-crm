package com.personalcrm.contact;

import com.personalcrm.user.User;
import com.personalcrm.user.UserRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final BirthdayImportantDateSynchronizer birthdayImportantDateSynchronizer;
    private final UserRepository userRepository;

    public ContactService(
            ContactRepository contactRepository,
            BirthdayImportantDateSynchronizer birthdayImportantDateSynchronizer,
            UserRepository userRepository
    ) {
        this.contactRepository = contactRepository;
        this.birthdayImportantDateSynchronizer = birthdayImportantDateSynchronizer;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> listContacts(String authenticatedEmail) {
        User owner = findAuthenticatedUser(authenticatedEmail);

        return contactRepository.findByOwnerIdOrderByNameAsc(owner.getId())
                .stream()
                .map(ContactResponse::from)
                .toList();
    }

    @Transactional
    public ContactResponse createContact(String authenticatedEmail, ContactRequest request) {
        User owner = findAuthenticatedUser(authenticatedEmail);
        Contact contact = new Contact(
                owner,
                normalizeRequired(request.name()),
                normalizeOptional(request.organization()),
                normalizeOptional(request.jobTitle()),
                request.birthday(),
                toAddress(request.address()),
                normalizeOptional(request.notes()),
                toPhoneNumbers(request.phoneNumbers()),
                toEmailAddresses(request.emailAddresses())
        );

        Contact savedContact = contactRepository.save(contact);
        birthdayImportantDateSynchronizer.syncBirthdayDateFromContact(savedContact);

        return ContactResponse.from(savedContact);
    }

    @Transactional(readOnly = true)
    public ContactResponse getContact(String authenticatedEmail, Long contactId) {
        User owner = findAuthenticatedUser(authenticatedEmail);
        Contact contact = findOwnedContact(contactId, owner.getId());

        return ContactResponse.from(contact);
    }

    @Transactional
    public ContactResponse updateContact(String authenticatedEmail, Long contactId, ContactRequest request) {
        User owner = findAuthenticatedUser(authenticatedEmail);
        Contact contact = findOwnedContact(contactId, owner.getId());
        contact.updateBasicInfo(
                normalizeRequired(request.name()),
                normalizeOptional(request.organization()),
                normalizeOptional(request.jobTitle()),
                request.birthday()
        );
        contact.updateDetails(
                toAddress(request.address()),
                normalizeOptional(request.notes()),
                toPhoneNumbers(request.phoneNumbers()),
                toEmailAddresses(request.emailAddresses())
        );
        birthdayImportantDateSynchronizer.syncBirthdayDateFromContact(contact);

        return ContactResponse.from(contact);
    }

    @Transactional
    public void deleteContact(String authenticatedEmail, Long contactId) {
        User owner = findAuthenticatedUser(authenticatedEmail);
        Contact contact = findOwnedContact(contactId, owner.getId());

        contactRepository.delete(contact);
    }

    private User findAuthenticatedUser(String authenticatedEmail) {
        return userRepository.findByEmail(normalizeEmail(authenticatedEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private Contact findOwnedContact(Long contactId, Long ownerId) {
        return contactRepository.findByIdAndOwnerId(contactId, ownerId)
                .orElseThrow(() -> new ContactNotFoundException(contactId));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private ContactAddress toAddress(ContactAddressRequest request) {
        if (request == null) {
            return null;
        }

        ContactAddress address = new ContactAddress(
                normalizeOptional(request.street()),
                normalizeOptional(request.city()),
                normalizeOptional(request.state()),
                normalizeOptional(request.postalCode()),
                normalizeOptional(request.country())
        );

        if (address.isEmpty()) {
            return null;
        }
        return address;
    }

    private List<ContactPhoneNumber> toPhoneNumbers(List<ContactPhoneRequest> requests) {
        if (requests == null) {
            return List.of();
        }

        return requests.stream()
                .map(request -> new ContactPhoneNumber(
                        normalizeOptional(request.label()),
                        normalizeRequired(request.number())
                ))
                .toList();
    }

    private List<ContactEmailAddress> toEmailAddresses(List<ContactEmailRequest> requests) {
        if (requests == null) {
            return List.of();
        }

        return requests.stream()
                .map(request -> new ContactEmailAddress(
                        normalizeOptional(request.label()),
                        normalizeEmail(request.email())
                ))
                .toList();
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
