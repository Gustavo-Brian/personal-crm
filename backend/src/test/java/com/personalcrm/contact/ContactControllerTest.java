package com.personalcrm.contact;

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
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void createsListsUpdatesAndDeletesOwnedContact() throws Exception {
        String token = createTokenFor("Owner User", "owner@example.com");
        Long contactId = createContact(token, """
                {
                  "name": "  Grace Hopper  ",
                  "organization": "  US Navy  ",
                  "jobTitle": "Computer Scientist",
                  "birthday": "1906-12-09",
                  "phoneNumbers": [
                    {
                      "label": " work ",
                      "number": " +1-555-0100 "
                    }
                  ],
                  "emailAddresses": [
                    {
                      "label": "Primary",
                      "email": "GRACE.HOPPER@EXAMPLE.COM"
                    }
                  ],
                  "address": {
                    "street": "  1 Navy Way  ",
                    "city": "Arlington",
                    "state": "VA",
                    "postalCode": "22201",
                    "country": "USA"
                  },
                  "notes": "  COBOL pioneer  "
                }
                """);

        mockMvc.perform(get("/contacts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(contactId))
                .andExpect(jsonPath("$[0].name").value("Grace Hopper"))
                .andExpect(jsonPath("$[0].organization").value("US Navy"))
                .andExpect(jsonPath("$[0].jobTitle").value("Computer Scientist"))
                .andExpect(jsonPath("$[0].birthday").value("1906-12-09"))
                .andExpect(jsonPath("$[0].phoneNumbers", hasSize(1)))
                .andExpect(jsonPath("$[0].phoneNumbers[0].label").value("work"))
                .andExpect(jsonPath("$[0].phoneNumbers[0].number").value("+1-555-0100"))
                .andExpect(jsonPath("$[0].emailAddresses", hasSize(1)))
                .andExpect(jsonPath("$[0].emailAddresses[0].label").value("Primary"))
                .andExpect(jsonPath("$[0].emailAddresses[0].email").value("grace.hopper@example.com"))
                .andExpect(jsonPath("$[0].address.street").value("1 Navy Way"))
                .andExpect(jsonPath("$[0].address.city").value("Arlington"))
                .andExpect(jsonPath("$[0].address.state").value("VA"))
                .andExpect(jsonPath("$[0].address.postalCode").value("22201"))
                .andExpect(jsonPath("$[0].address.country").value("USA"))
                .andExpect(jsonPath("$[0].notes").value("COBOL pioneer"))
                .andExpect(jsonPath("$[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$[0].updatedAt", notNullValue()));

        mockMvc.perform(get("/contacts/{id}", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contactId))
                .andExpect(jsonPath("$.name").value("Grace Hopper"));

        mockMvc.perform(put("/contacts/{id}", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Grace Brewster Hopper",
                                  "organization": "",
                                  "jobTitle": "Rear Admiral",
                                  "birthday": "1906-12-09",
                                  "phoneNumbers": [
                                    {
                                      "label": "mobile",
                                      "number": "+1-555-0111"
                                    },
                                    {
                                      "label": "office",
                                      "number": "+1-555-0112"
                                    }
                                  ],
                                  "emailAddresses": [
                                    {
                                      "label": "primary",
                                      "email": "GRACE.BREWSTER@EXAMPLE.COM"
                                    }
                                  ],
                                  "address": {
                                    "street": "",
                                    "city": "",
                                    "state": "",
                                    "postalCode": "",
                                    "country": ""
                                  },
                                  "notes": ""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contactId))
                .andExpect(jsonPath("$.name").value("Grace Brewster Hopper"))
                .andExpect(jsonPath("$.organization").doesNotExist())
                .andExpect(jsonPath("$.jobTitle").value("Rear Admiral"))
                .andExpect(jsonPath("$.phoneNumbers", hasSize(2)))
                .andExpect(jsonPath("$.phoneNumbers[0].label").value("mobile"))
                .andExpect(jsonPath("$.phoneNumbers[0].number").value("+1-555-0111"))
                .andExpect(jsonPath("$.phoneNumbers[1].label").value("office"))
                .andExpect(jsonPath("$.phoneNumbers[1].number").value("+1-555-0112"))
                .andExpect(jsonPath("$.emailAddresses", hasSize(1)))
                .andExpect(jsonPath("$.emailAddresses[0].label").value("primary"))
                .andExpect(jsonPath("$.emailAddresses[0].email").value("grace.brewster@example.com"))
                .andExpect(jsonPath("$.address").doesNotExist())
                .andExpect(jsonPath("$.notes").doesNotExist());

        mockMvc.perform(delete("/contacts/{id}", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(contactRepository.existsById(contactId)).isFalse();
    }

    @Test
    void rejectsInvalidContactPayloads() throws Exception {
        String token = createTokenFor("Validation Owner", "validation-owner@example.com");
        Long contactId = createContact(token, """
                {
                  "name": "Valid Contact",
                  "organization": "NASA",
                  "jobTitle": "Engineer",
                  "birthday": null
                }
                """);

        mockMvc.perform(post("/contacts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   ",
                                  "organization": "NASA",
                                  "jobTitle": "Engineer",
                                  "birthday": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").value("Name is required"));

        mockMvc.perform(put("/contacts/{id}", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Valid Contact",
                                  "organization": "An organization name that is deliberately longer than one hundred and sixty characters so the API contract rejects it before touching the persisted contact record with extra validation text.",
                                  "jobTitle": "Engineer",
                                  "birthday": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.organization").value("Organization must have at most 160 characters"));

        mockMvc.perform(post("/contacts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Invalid Details",
                                  "organization": "NASA",
                                  "jobTitle": "Engineer",
                                  "birthday": null,
                                  "phoneNumbers": [
                                    {
                                      "label": "work",
                                      "number": "   "
                                    }
                                  ],
                                  "emailAddresses": [
                                    {
                                      "label": "primary",
                                      "email": "not-an-email"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors['phoneNumbers[0].number']").value("Phone number is required"))
                .andExpect(jsonPath("$.errors['emailAddresses[0].email']").value("Email address must be valid"));

        Contact contact = contactRepository.findById(contactId).orElseThrow();
        assertThat(contact.getOrganization()).isEqualTo("NASA");
    }

    @Test
    void isolatesContactsByAuthenticatedUser() throws Exception {
        String ownerToken = createTokenFor("Owner User", "contact-owner@example.com");
        String otherToken = createTokenFor("Other User", "other-user@example.com");
        Long contactId = createContact(ownerToken, """
                {
                  "name": "Private Contact",
                  "organization": "Private Org",
                  "jobTitle": "Advisor",
                  "birthday": null
                }
                """);

        mockMvc.perform(get("/contacts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/contacts/{id}", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found: " + contactId));

        mockMvc.perform(put("/contacts/{id}", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Stolen Contact",
                                  "organization": "Nope",
                                  "jobTitle": "Nope",
                                  "birthday": null
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found: " + contactId));

        mockMvc.perform(delete("/contacts/{id}", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found: " + contactId));

        Contact contact = contactRepository.findById(contactId).orElseThrow();
        assertThat(contact.getName()).isEqualTo("Private Contact");
    }

    private Long createContact(String token, String payload) throws Exception {
        MvcResult result = mockMvc.perform(post("/contacts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn();

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
        contactRepository.deleteAll();
        userRepository.deleteAll();
    }
}
