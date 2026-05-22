package com.personalcrm.group;

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
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContactGroupRepository groupRepository;

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
    void createsListsUpdatesAndDeletesOwnedGroup() throws Exception {
        String token = createTokenFor("Group Owner", "group-owner@example.com");
        Long groupId = createGroup(token, """
                {
                  "name": "  Mentors  ",
                  "description": "  People who help with career decisions  ",
                  "colorHex": "#0ea5e9"
                }
                """);

        mockMvc.perform(get("/groups")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(groupId))
                .andExpect(jsonPath("$[0].name").value("Mentors"))
                .andExpect(jsonPath("$[0].description").value("People who help with career decisions"))
                .andExpect(jsonPath("$[0].colorHex").value("#0EA5E9"))
                .andExpect(jsonPath("$[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$[0].updatedAt", notNullValue()));

        mockMvc.perform(get("/groups/{id}", groupId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(groupId))
                .andExpect(jsonPath("$.name").value("Mentors"));

        mockMvc.perform(put("/groups/{id}", groupId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Inner Circle",
                                  "description": "",
                                  "colorHex": ""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(groupId))
                .andExpect(jsonPath("$.name").value("Inner Circle"))
                .andExpect(jsonPath("$.description").doesNotExist())
                .andExpect(jsonPath("$.colorHex").doesNotExist());

        mockMvc.perform(delete("/groups/{id}", groupId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(groupRepository.existsById(groupId)).isFalse();
    }

    @Test
    void rejectsInvalidGroupPayloads() throws Exception {
        String token = createTokenFor("Validation Owner", "group-validation@example.com");
        Long groupId = createGroup(token, """
                {
                  "name": "Valid Group",
                  "description": "A useful group",
                  "colorHex": "#22c55e"
                }
                """);

        mockMvc.perform(post("/groups")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   ",
                                  "description": "Invalid",
                                  "colorHex": "#22c55e"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").value("Group name is required"));

        mockMvc.perform(post("/groups")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Invalid Color",
                                  "description": "Invalid",
                                  "colorHex": "blue"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.colorHex").value("Color must use #RRGGBB format"));

        mockMvc.perform(put("/groups/{id}", groupId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Valid Group",
                                  "description": "%s",
                                  "colorHex": "#22c55e"
                                }
                                """.formatted("x".repeat(501))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.description").value("Description must have at most 500 characters"));

        ContactGroup group = groupRepository.findById(groupId).orElseThrow();
        assertThat(group.getDescription()).isEqualTo("A useful group");
    }

    @Test
    void isolatesGroupsByAuthenticatedUser() throws Exception {
        String ownerToken = createTokenFor("Group Owner", "owned-group@example.com");
        String otherToken = createTokenFor("Other User", "other-group-user@example.com");
        Long groupId = createGroup(ownerToken, """
                {
                  "name": "Private Group",
                  "description": "Owner-only group",
                  "colorHex": "#f97316"
                }
                """);

        mockMvc.perform(get("/groups")
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/groups/{id}", groupId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Group not found: " + groupId));

        mockMvc.perform(put("/groups/{id}", groupId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Stolen Group",
                                  "description": "Nope",
                                  "colorHex": "#111827"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Group not found: " + groupId));

        mockMvc.perform(delete("/groups/{id}", groupId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Group not found: " + groupId));

        ContactGroup group = groupRepository.findById(groupId).orElseThrow();
        assertThat(group.getName()).isEqualTo("Private Group");
    }

    private Long createGroup(String token, String payload) throws Exception {
        MvcResult result = mockMvc.perform(post("/groups")
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
        groupRepository.deleteAll();
        userRepository.deleteAll();
    }
}
