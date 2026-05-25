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
import java.time.LocalDate;
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
class ContactBirthdaySyncTest {

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
    void createsAndUpdatesBirthdayImportantDateFromContactBirthday() throws Exception {
        String token = createTokenFor("Birthday Owner", "birthday-owner@example.com");
        Long contactId = createContact(token, "1990-05-20");

        mockMvc.perform(get("/contacts/{contactId}/important-dates", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].contactId").value(contactId))
                .andExpect(jsonPath("$[0].title").value("Birthday"))
                .andExpect(jsonPath("$[0].date").value("1990-05-20"))
                .andExpect(jsonPath("$[0].type").value("BIRTHDAY"))
                .andExpect(jsonPath("$[0].createdAt", notNullValue()));

        updateContact(token, contactId, "1991-06-21");

        mockMvc.perform(get("/contacts/{contactId}/important-dates", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].date").value("1991-06-21"))
                .andExpect(jsonPath("$[0].type").value("BIRTHDAY"));

        assertThat(importantDateRepository.count()).isEqualTo(1);
    }

    @Test
    void clearsBirthdayImportantDateWhenContactBirthdayIsRemoved() throws Exception {
        String token = createTokenFor("Birthday Owner", "birthday-clear@example.com");
        Long contactId = createContact(token, "1990-05-20");

        updateContact(token, contactId, null);

        mockMvc.perform(get("/contacts/{contactId}/important-dates", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        Contact contact = contactRepository.findById(contactId).orElseThrow();
        assertThat(contact.getBirthday()).isNull();
    }

    @Test
    void syncsContactBirthdayFromBirthdayImportantDateChanges() throws Exception {
        String token = createTokenFor("Birthday Owner", "birthday-important-date@example.com");
        Long contactId = createContact(token, null);
        Long importantDateId = createImportantDate(token, contactId, """
                {
                  "title": "Birthday dinner",
                  "date": "1990-05-20",
                  "type": "BIRTHDAY",
                  "description": "Dinner reservation"
                }
                """);

        assertThat(contactRepository.findById(contactId).orElseThrow().getBirthday())
                .isEqualTo(LocalDate.parse("1990-05-20"));

        mockMvc.perform(put("/contacts/{contactId}/important-dates/{importantDateId}", contactId, importantDateId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Birthday dinner",
                                  "date": "1991-06-21",
                                  "type": "BIRTHDAY",
                                  "description": "Updated reservation"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("1991-06-21"));

        assertThat(contactRepository.findById(contactId).orElseThrow().getBirthday())
                .isEqualTo(LocalDate.parse("1991-06-21"));

        mockMvc.perform(delete("/contacts/{contactId}/important-dates/{importantDateId}", contactId, importantDateId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(contactRepository.findById(contactId).orElseThrow().getBirthday()).isNull();
    }

    private Long createContact(String token, String birthday) throws Exception {
        MvcResult result = mockMvc.perform(post("/contacts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Birthday Contact",
                                  "organization": "Relationship Network",
                                  "jobTitle": "Advisor",
                                  "birthday": %s
                                }
                                """.formatted(jsonDate(birthday))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn();

        return extractId(result);
    }

    private void updateContact(String token, Long contactId, String birthday) throws Exception {
        mockMvc.perform(put("/contacts/{contactId}", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Birthday Contact",
                                  "organization": "Relationship Network",
                                  "jobTitle": "Advisor",
                                  "birthday": %s
                                }
                                """.formatted(jsonDate(birthday))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contactId));
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

    private Long extractId(MvcResult result) throws Exception {
        Map<String, Object> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );
        return ((Number) response.get("id")).longValue();
    }

    private String jsonDate(String date) {
        if (date == null) {
            return "null";
        }
        return "\"" + date + "\"";
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
