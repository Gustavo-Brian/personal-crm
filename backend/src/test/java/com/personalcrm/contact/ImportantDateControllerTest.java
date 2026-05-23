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
class ImportantDateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ImportantDateRepository importantDateRepository;

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
    void createsListsUpdatesAndDeletesImportantDateForOwnedContact() throws Exception {
        String token = createTokenFor("Owner User", "important-date-owner@example.com");
        Long contactId = createContact(token);
        Long importantDateId = createImportantDate(token, contactId, """
                {
                  "title": "  Birthday dinner  ",
                  "date": "2026-06-15",
                  "type": "BIRTHDAY",
                  "description": "  Dinner reservation  "
                }
                """);

        mockMvc.perform(get("/contacts/{contactId}/important-dates", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(importantDateId))
                .andExpect(jsonPath("$[0].contactId").value(contactId))
                .andExpect(jsonPath("$[0].title").value("Birthday dinner"))
                .andExpect(jsonPath("$[0].date").value("2026-06-15"))
                .andExpect(jsonPath("$[0].type").value("BIRTHDAY"))
                .andExpect(jsonPath("$[0].description").value("Dinner reservation"))
                .andExpect(jsonPath("$[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$[0].updatedAt", notNullValue()));

        mockMvc.perform(get("/contacts/{contactId}/important-dates/{importantDateId}", contactId, importantDateId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(importantDateId))
                .andExpect(jsonPath("$.title").value("Birthday dinner"));

        mockMvc.perform(put("/contacts/{contactId}/important-dates/{importantDateId}", contactId, importantDateId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Conference follow-up",
                                  "date": "2026-07-20",
                                  "type": "WORK",
                                  "description": ""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(importantDateId))
                .andExpect(jsonPath("$.title").value("Conference follow-up"))
                .andExpect(jsonPath("$.date").value("2026-07-20"))
                .andExpect(jsonPath("$.type").value("WORK"))
                .andExpect(jsonPath("$.description").doesNotExist());

        mockMvc.perform(delete("/contacts/{contactId}/important-dates/{importantDateId}", contactId, importantDateId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(importantDateRepository.existsById(importantDateId)).isFalse();
    }

    @Test
    void rejectsInvalidImportantDatePayload() throws Exception {
        String token = createTokenFor("Validation Owner", "important-date-validation@example.com");
        Long contactId = createContact(token);

        mockMvc.perform(post("/contacts/{contactId}/important-dates", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "date": null,
                                  "type": null,
                                  "description": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").value("Title is required"))
                .andExpect(jsonPath("$.errors.date").value("Date is required"))
                .andExpect(jsonPath("$.errors.type").value("Type is required"));
    }

    @Test
    void preventsImportantDateAccessThroughAnotherUsersContact() throws Exception {
        String ownerToken = createTokenFor("Owner User", "owned-important-date@example.com");
        String otherToken = createTokenFor("Other User", "other-important-date@example.com");
        Long contactId = createContact(ownerToken);
        Long importantDateId = createImportantDate(ownerToken, contactId, """
                {
                  "title": "Private celebration",
                  "date": "2026-08-01",
                  "type": "OTHER",
                  "description": "Private date"
                }
                """);

        mockMvc.perform(get("/contacts/{contactId}/important-dates", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found: " + contactId));

        mockMvc.perform(get("/contacts/{contactId}/important-dates/{importantDateId}", contactId, importantDateId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found: " + contactId));

        mockMvc.perform(put("/contacts/{contactId}/important-dates/{importantDateId}", contactId, importantDateId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Changed date",
                                  "date": "2026-08-02",
                                  "type": "OTHER",
                                  "description": "Should not change"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found: " + contactId));

        mockMvc.perform(delete("/contacts/{contactId}/important-dates/{importantDateId}", contactId, importantDateId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found: " + contactId));

        ImportantDate importantDate = importantDateRepository.findById(importantDateId).orElseThrow();
        assertThat(importantDate.getTitle()).isEqualTo("Private celebration");
    }

    private Long createImportantDate(String token, Long contactId, String payload) throws Exception {
        MvcResult result = mockMvc.perform(post("/contacts/{contactId}/important-dates", contactId)
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
                                  "name": "Important Date Contact",
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
        importantDateRepository.deleteAll();
        contactRepository.deleteAll();
        userRepository.deleteAll();
    }
}
