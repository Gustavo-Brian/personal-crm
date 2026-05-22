package com.personalcrm.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class GroupMembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContactGroupMembershipRepository membershipRepository;

    @Autowired
    private ContactGroupRepository groupRepository;

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
    void addsListsPreventsDuplicateAndRemovesContactMembership() throws Exception {
        String token = createTokenFor("Membership Owner", "membership-owner@example.com");
        Long groupId = createGroup(token, """
                {
                  "name": "Professional Network",
                  "description": "People connected to career opportunities",
                  "colorHex": "#2563EB"
                }
                """);
        Long contactId = createContact(token, """
                {
                  "name": "Grace Hopper",
                  "organization": "US Navy",
                  "jobTitle": "Computer Scientist",
                  "birthday": null
                }
                """);

        mockMvc.perform(post("/groups/{groupId}/contacts/{contactId}", groupId, contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(contactId))
                .andExpect(jsonPath("$.name").value("Grace Hopper"))
                .andExpect(jsonPath("$.organization").value("US Navy"));

        mockMvc.perform(post("/groups/{groupId}/contacts/{contactId}", groupId, contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(contactId));

        assertThat(membershipRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/groups/{groupId}/contacts", groupId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(contactId))
                .andExpect(jsonPath("$[0].name").value("Grace Hopper"));

        mockMvc.perform(delete("/groups/{groupId}/contacts/{contactId}", groupId, contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(membershipRepository.count()).isZero();

        mockMvc.perform(get("/groups/{groupId}/contacts", groupId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void preventsAddingContactOrGroupFromAnotherUser() throws Exception {
        String ownerToken = createTokenFor("Membership Owner", "membership-access-owner@example.com");
        String otherToken = createTokenFor("Other User", "membership-other@example.com");
        Long ownerGroupId = createGroup(ownerToken, """
                {
                  "name": "Referral Network",
                  "description": "Trusted professional contacts",
                  "colorHex": "#16A34A"
                }
                """);
        Long ownerContactId = createContact(ownerToken, """
                {
                  "name": "Private Contact",
                  "organization": "Private Org",
                  "jobTitle": "Advisor",
                  "birthday": null
                }
                """);
        Long otherGroupId = createGroup(otherToken, """
                {
                  "name": "External Network",
                  "description": "Owned by another account",
                  "colorHex": "#9333EA"
                }
                """);
        Long otherContactId = createContact(otherToken, """
                {
                  "name": "External Contact",
                  "organization": "External Org",
                  "jobTitle": "Manager",
                  "birthday": null
                }
                """);

        mockMvc.perform(post("/groups/{groupId}/contacts/{contactId}", ownerGroupId, otherContactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found: " + otherContactId));

        mockMvc.perform(post("/groups/{groupId}/contacts/{contactId}", otherGroupId, ownerContactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Group not found: " + otherGroupId));

        assertThat(membershipRepository.count()).isZero();
    }

    @Test
    void preventsRemovingMembershipThroughAnotherUser() throws Exception {
        String ownerToken = createTokenFor("Membership Owner", "membership-remove-owner@example.com");
        String otherToken = createTokenFor("Other User", "membership-remove-other@example.com");
        Long groupId = createGroup(ownerToken, """
                {
                  "name": "Hiring Contacts",
                  "description": "People related to hiring conversations",
                  "colorHex": "#EA580C"
                }
                """);
        Long contactId = createContact(ownerToken, """
                {
                  "name": "Owner Contact",
                  "organization": "Owner Org",
                  "jobTitle": "Coordinator",
                  "birthday": null
                }
                """);

        mockMvc.perform(post("/groups/{groupId}/contacts/{contactId}", groupId, contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/groups/{groupId}/contacts/{contactId}", groupId, contactId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Group not found: " + groupId));

        assertThat(membershipRepository.count()).isEqualTo(1);
    }

    private Long createGroup(String token, String payload) throws Exception {
        MvcResult result = mockMvc.perform(post("/groups")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn();

        return extractId(result);
    }

    private Long createContact(String token, String payload) throws Exception {
        MvcResult result = mockMvc.perform(post("/contacts")
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

    private String createTokenFor(String name, String email) {
        userRegistrationService.register(new RegisterUserRequest(name, email, "password123"));
        User user = userRepository.findByEmail(email).orElseThrow();
        return jwtTokenService.generateToken(user);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private void cleanDatabase() {
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
        contactRepository.deleteAll();
        userRepository.deleteAll();
    }
}
