package com.personalcrm.appointment;

import com.personalcrm.contact.Contact;
import com.personalcrm.contact.ContactNotFoundException;
import com.personalcrm.contact.ContactRepository;
import com.personalcrm.user.User;
import com.personalcrm.user.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            ContactRepository contactRepository,
            UserRepository userRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.clock = Clock.systemDefaultZone();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listAppointments(String authenticatedEmail) {
        User owner = findAuthenticatedUser(authenticatedEmail);

        return appointmentRepository.findByOwnerIdOrderByStartsAtAscTitleAsc(owner.getId())
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listUpcomingAppointments(String authenticatedEmail) {
        User owner = findAuthenticatedUser(authenticatedEmail);
        LocalDateTime now = LocalDateTime.now(clock);

        return appointmentRepository.findByOwnerIdAndStartsAtGreaterThanEqualOrderByStartsAtAscTitleAsc(
                        owner.getId(),
                        now
                )
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @Transactional
    public AppointmentResponse createAppointment(String authenticatedEmail, AppointmentRequest request) {
        User owner = findAuthenticatedUser(authenticatedEmail);
        Contact contact = findOptionalOwnedContact(request.contactId(), owner.getId());
        Appointment appointment = new Appointment(
                owner,
                contact,
                normalizeRequired(request.title()),
                request.startsAt(),
                request.endsAt(),
                normalizeOptional(request.location()),
                normalizeOptional(request.description())
        );

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(String authenticatedEmail, Long appointmentId) {
        User owner = findAuthenticatedUser(authenticatedEmail);
        Appointment appointment = findOwnedAppointment(appointmentId, owner.getId());

        return AppointmentResponse.from(appointment);
    }

    @Transactional
    public AppointmentResponse updateAppointment(
            String authenticatedEmail,
            Long appointmentId,
            AppointmentRequest request
    ) {
        User owner = findAuthenticatedUser(authenticatedEmail);
        Appointment appointment = findOwnedAppointment(appointmentId, owner.getId());
        Contact contact = findOptionalOwnedContact(request.contactId(), owner.getId());
        appointment.updateDetails(
                contact,
                normalizeRequired(request.title()),
                request.startsAt(),
                request.endsAt(),
                normalizeOptional(request.location()),
                normalizeOptional(request.description())
        );

        return AppointmentResponse.from(appointment);
    }

    @Transactional
    public void deleteAppointment(String authenticatedEmail, Long appointmentId) {
        User owner = findAuthenticatedUser(authenticatedEmail);
        Appointment appointment = findOwnedAppointment(appointmentId, owner.getId());

        appointmentRepository.delete(appointment);
    }

    private User findAuthenticatedUser(String authenticatedEmail) {
        return userRepository.findByEmail(normalizeEmail(authenticatedEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private Appointment findOwnedAppointment(Long appointmentId, Long ownerId) {
        return appointmentRepository.findByIdAndOwnerId(appointmentId, ownerId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
    }

    private Contact findOptionalOwnedContact(Long contactId, Long ownerId) {
        if (contactId == null) {
            return null;
        }

        return contactRepository.findByIdAndOwnerId(contactId, ownerId)
                .orElseThrow(() -> new ContactNotFoundException(contactId));
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
