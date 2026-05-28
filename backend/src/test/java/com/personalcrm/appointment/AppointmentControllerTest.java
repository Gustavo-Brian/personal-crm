package com.personalcrm.appointment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalcrm.auth.RegisterUserRequest;
import com.personalcrm.auth.UserRegistrationService;
import com.personalcrm.auth.jwt.JwtTokenService;
import com.personalcrm.contact.ContactRepository;
import com.personalcrm.user.User;
import com.personalcrm.user.UserRepository;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void createsListsUpdatesAndDeletesOwnedAppointment() throws Exception {
        String token = createTokenFor("Appointment Owner", "appointment-owner@example.com");
        Long contactId = createContact(token);
        Long appointmentId = createAppointment(token, """
                {
                  "title": "  Coffee chat  ",
                  "startsAt": "2030-02-10T09:30:00",
                  "endsAt": "2030-02-10T10:30:00",
                  "location": "  Downtown Cafe  ",
                  "description": "  Monthly relationship check-in.  ",
                  "contactId": %d
                }
                """.formatted(contactId));

        mockMvc.perform(get("/appointments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(appointmentId))
                .andExpect(jsonPath("$[0].contactId").value(contactId))
                .andExpect(jsonPath("$[0].title").value("Coffee chat"))
                .andExpect(jsonPath("$[0].startsAt").value("2030-02-10T09:30:00"))
                .andExpect(jsonPath("$[0].endsAt").value("2030-02-10T10:30:00"))
                .andExpect(jsonPath("$[0].location").value("Downtown Cafe"))
                .andExpect(jsonPath("$[0].description").value("Monthly relationship check-in."))
                .andExpect(jsonPath("$[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$[0].updatedAt", notNullValue()));

        mockMvc.perform(get("/appointments/{id}", appointmentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId))
                .andExpect(jsonPath("$.title").value("Coffee chat"));

        mockMvc.perform(put("/appointments/{id}", appointmentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Follow-up call",
                                  "startsAt": "2030-02-12T14:00:00",
                                  "endsAt": null,
                                  "location": "",
                                  "description": "",
                                  "contactId": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId))
                .andExpect(jsonPath("$.contactId").doesNotExist())
                .andExpect(jsonPath("$.title").value("Follow-up call"))
                .andExpect(jsonPath("$.startsAt").value("2030-02-12T14:00:00"))
                .andExpect(jsonPath("$.endsAt").doesNotExist())
                .andExpect(jsonPath("$.location").doesNotExist())
                .andExpect(jsonPath("$.description").doesNotExist());

        mockMvc.perform(delete("/appointments/{id}", appointmentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(appointmentRepository.existsById(appointmentId)).isFalse();
    }

    @Test
    void listsOnlyUpcomingOwnedAppointments() throws Exception {
        String token = createTokenFor("Upcoming Owner", "upcoming-appointment@example.com");
        Long pastAppointmentId = createAppointment(token, """
                {
                  "title": "Past meeting",
                  "startsAt": "2000-01-10T09:00:00",
                  "endsAt": "2000-01-10T10:00:00",
                  "location": "Office",
                  "description": "Already happened.",
                  "contactId": null
                }
                """);
        Long futureAppointmentId = createAppointment(token, """
                {
                  "title": "Future meeting",
                  "startsAt": "2030-01-10T09:00:00",
                  "endsAt": "2030-01-10T10:00:00",
                  "location": "Office",
                  "description": "Upcoming check-in.",
                  "contactId": null
                }
                """);

        mockMvc.perform(get("/appointments/upcoming")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(futureAppointmentId))
                .andExpect(jsonPath("$[0].title").value("Future meeting"));

        mockMvc.perform(get("/appointments/{id}", pastAppointmentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Past meeting"));
    }

    @Test
    void rejectsInvalidAppointmentPayload() throws Exception {
        String token = createTokenFor("Validation Owner", "appointment-validation@example.com");

        mockMvc.perform(post("/appointments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "startsAt": null,
                                  "endsAt": null,
                                  "location": null,
                                  "description": null,
                                  "contactId": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").value("Title is required"))
                .andExpect(jsonPath("$.errors.startsAt").value("Start time is required"));

        mockMvc.perform(post("/appointments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Invalid time range",
                                  "startsAt": "2030-02-10T10:30:00",
                                  "endsAt": "2030-02-10T09:30:00",
                                  "location": "Office",
                                  "description": "End time cannot come first.",
                                  "contactId": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void isolatesAppointmentsByAuthenticatedUser() throws Exception {
        String ownerToken = createTokenFor("Owner User", "owned-appointment@example.com");
        String otherToken = createTokenFor("Other User", "other-appointment@example.com");
        Long appointmentId = createAppointment(ownerToken, """
                {
                  "title": "Private appointment",
                  "startsAt": "2030-01-11T09:00:00",
                  "endsAt": "2030-01-11T10:00:00",
                  "location": "Private office",
                  "description": "Private details.",
                  "contactId": null
                }
                """);

        mockMvc.perform(get("/appointments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/appointments/{id}", appointmentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Appointment not found: " + appointmentId));

        mockMvc.perform(put("/appointments/{id}", appointmentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Changed appointment",
                                  "startsAt": "2030-01-12T09:00:00",
                                  "endsAt": "2030-01-12T10:00:00",
                                  "location": "Nope",
                                  "description": "Should not change.",
                                  "contactId": null
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Appointment not found: " + appointmentId));

        mockMvc.perform(delete("/appointments/{id}", appointmentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Appointment not found: " + appointmentId));

        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        assertThat(appointment.getTitle()).isEqualTo("Private appointment");
    }

    @Test
    void rejectsAppointmentLinkedToContactOwnedByAnotherUser() throws Exception {
        String ownerToken = createTokenFor("Appointment Owner", "appointment-contact-owner@example.com");
        String otherToken = createTokenFor("Contact Owner", "appointment-contact-other@example.com");
        Long ownedContactId = createContact(ownerToken);
        Long foreignContactId = createContact(otherToken);

        mockMvc.perform(post("/appointments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Cross-owner appointment",
                                  "startsAt": "2030-03-10T09:00:00",
                                  "endsAt": "2030-03-10T10:00:00",
                                  "location": "Office",
                                  "description": "Should not be created.",
                                  "contactId": %d
                                }
                                """.formatted(foreignContactId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found: " + foreignContactId));

        assertThat(appointmentRepository.count()).isZero();

        Long appointmentId = createAppointment(ownerToken, """
                {
                  "title": "Owned appointment",
                  "startsAt": "2030-03-11T09:00:00",
                  "endsAt": "2030-03-11T10:00:00",
                  "location": "Office",
                  "description": "Valid owner contact.",
                  "contactId": %d
                }
                """.formatted(ownedContactId));

        mockMvc.perform(put("/appointments/{id}", appointmentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Hijacked appointment",
                                  "startsAt": "2030-03-12T09:00:00",
                                  "endsAt": "2030-03-12T10:00:00",
                                  "location": "Office",
                                  "description": "Should not change the appointment.",
                                  "contactId": %d
                                }
                                """.formatted(foreignContactId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found: " + foreignContactId));

        mockMvc.perform(get("/appointments/{id}", appointmentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Owned appointment"))
                .andExpect(jsonPath("$.contactId").value(ownedContactId));
    }

    private Long createAppointment(String token, String payload) throws Exception {
        MvcResult result = mockMvc.perform(post("/appointments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn();

        return extractId(result);
    }

    private Long createContact(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/contacts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Appointment Contact",
                                  "organization": "Relationship Network",
                                  "jobTitle": "Advisor",
                                  "birthday": null
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn();

        return extractId(result);
    }

    private Long extractId(MvcResult result) throws Exception {
        Map<String, Object> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );
        return ((Number) response.get("id")).longValue();
    }

    private String createTokenFor(String name, String email) {
        userRegistrationService.register(new RegisterUserRequest(name, email, "password123"));
        User user = userRepository.findByEmail(email).orElseThrow();
        return jwtTokenService.generateToken(user);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private void cleanDatabase() {
        appointmentRepository.deleteAll();
        contactRepository.deleteAll();
        userRepository.deleteAll();
    }
}
