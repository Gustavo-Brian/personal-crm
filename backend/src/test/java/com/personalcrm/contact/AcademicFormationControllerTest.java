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
class AcademicFormationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AcademicFormationRepository academicFormationRepository;

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
    void createsListsUpdatesAndDeletesAcademicFormationForOwnedContact() throws Exception {
        String token = createTokenFor("Owner User", "formation-owner@example.com");
        Long contactId = createContact(token);
        Long formationId = createFormation(token, contactId, """
                {
                  "institution": "  Massachusetts Institute of Technology  ",
                  "degree": "BSc",
                  "fieldOfStudy": "Computer Science",
                  "startDate": "2010-02-01",
                  "endDate": "2014-12-15",
                  "description": "  Undergraduate studies  "
                }
                """);

        mockMvc.perform(get("/contacts/{contactId}/academic-formations", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(formationId))
                .andExpect(jsonPath("$[0].contactId").value(contactId))
                .andExpect(jsonPath("$[0].institution").value("Massachusetts Institute of Technology"))
                .andExpect(jsonPath("$[0].degree").value("BSc"))
                .andExpect(jsonPath("$[0].fieldOfStudy").value("Computer Science"))
                .andExpect(jsonPath("$[0].startDate").value("2010-02-01"))
                .andExpect(jsonPath("$[0].endDate").value("2014-12-15"))
                .andExpect(jsonPath("$[0].description").value("Undergraduate studies"))
                .andExpect(jsonPath("$[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$[0].updatedAt", notNullValue()));

        mockMvc.perform(get("/contacts/{contactId}/academic-formations/{formationId}", contactId, formationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(formationId))
                .andExpect(jsonPath("$.institution").value("Massachusetts Institute of Technology"));

        mockMvc.perform(put("/contacts/{contactId}/academic-formations/{formationId}", contactId, formationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "institution": "MIT",
                                  "degree": "MSc",
                                  "fieldOfStudy": "Artificial Intelligence",
                                  "startDate": "2015-01-10",
                                  "endDate": null,
                                  "description": ""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(formationId))
                .andExpect(jsonPath("$.institution").value("MIT"))
                .andExpect(jsonPath("$.degree").value("MSc"))
                .andExpect(jsonPath("$.fieldOfStudy").value("Artificial Intelligence"))
                .andExpect(jsonPath("$.startDate").value("2015-01-10"))
                .andExpect(jsonPath("$.endDate").doesNotExist())
                .andExpect(jsonPath("$.description").doesNotExist());

        mockMvc.perform(delete("/contacts/{contactId}/academic-formations/{formationId}", contactId, formationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(academicFormationRepository.existsById(formationId)).isFalse();
    }

    @Test
    void rejectsInvalidAcademicFormationPayload() throws Exception {
        String token = createTokenFor("Validation Owner", "formation-validation@example.com");
        Long contactId = createContact(token);

        mockMvc.perform(post("/contacts/{contactId}/academic-formations", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "institution": "   ",
                                  "degree": "BSc",
                                  "fieldOfStudy": "Computer Science",
                                  "startDate": null,
                                  "endDate": null,
                                  "description": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.institution").value("Institution is required"));
    }

    @Test
    void preventsAcademicFormationAccessThroughAnotherUsersContact() throws Exception {
        String ownerToken = createTokenFor("Owner User", "owned-formation@example.com");
        String otherToken = createTokenFor("Other User", "other-formation@example.com");
        Long contactId = createContact(ownerToken);
        Long formationId = createFormation(ownerToken, contactId, """
                {
                  "institution": "Private University",
                  "degree": "MBA",
                  "fieldOfStudy": "Business",
                  "startDate": "2018-01-01",
                  "endDate": "2020-01-01",
                  "description": "Private formation"
                }
                """);

        mockMvc.perform(get("/contacts/{contactId}/academic-formations", contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found: " + contactId));

        mockMvc.perform(get("/contacts/{contactId}/academic-formations/{formationId}", contactId, formationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found: " + contactId));

        mockMvc.perform(put("/contacts/{contactId}/academic-formations/{formationId}", contactId, formationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "institution": "Changed University",
                                  "degree": "MBA",
                                  "fieldOfStudy": "Business",
                                  "startDate": "2018-01-01",
                                  "endDate": "2020-01-01",
                                  "description": "Should not change"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found: " + contactId));

        mockMvc.perform(delete("/contacts/{contactId}/academic-formations/{formationId}", contactId, formationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found: " + contactId));

        AcademicFormation formation = academicFormationRepository.findById(formationId).orElseThrow();
        assertThat(formation.getInstitution()).isEqualTo("Private University");
    }

    private Long createFormation(String token, Long contactId, String payload) throws Exception {
        MvcResult result = mockMvc.perform(post("/contacts/{contactId}/academic-formations", contactId)
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
                                  "name": "Academic Contact",
                                  "organization": "University Network",
                                  "jobTitle": "Researcher",
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
        academicFormationRepository.deleteAll();
        contactRepository.deleteAll();
        userRepository.deleteAll();
    }
}
