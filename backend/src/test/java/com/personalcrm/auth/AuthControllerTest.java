package com.personalcrm.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.personalcrm.auth.jwt.JwtTokenService;
import com.personalcrm.user.User;
import com.personalcrm.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registersUser() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Katherine Johnson",
                                  "email": "katherine@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Katherine Johnson"))
                .andExpect(jsonPath("$.email").value("katherine@example.com"));
    }

    @Test
    void rejectsDuplicateEmailRegistration() throws Exception {
        String firstRequest = """
                {
                  "name": "First User",
                  "email": "duplicate@example.com",
                  "password": "password123"
                }
                """;
        String duplicateRequest = """
                {
                  "name": "Second User",
                  "email": "DUPLICATE@example.com",
                  "password": "password456"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already registered: duplicate@example.com"));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidRegistrationPayload() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " ",
                                  "email": "invalid-email",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    void logsInUser() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Margaret Hamilton",
                                  "email": "margaret@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "MARGARET@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Margaret Hamilton"))
                .andExpect(jsonPath("$.email").value("margaret@example.com"))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void rejectsLoginWithInvalidPassword() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Invalid Login",
                                  "email": "invalid-login@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-login@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void updatesAuthenticatedUserCredentials() throws Exception {
        String token = createTokenFor("Original User", "original@example.com", "password123");

        mockMvc.perform(put("/auth/credentials")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated User",
                                  "email": "UPDATED@example.com",
                                  "currentPassword": "password123",
                                  "newPassword": "new-password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "updated@example.com",
                                  "password": "new-password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        assertThat(userRepository.findByEmail("original@example.com")).isEmpty();
    }

    @Test
    void updatesCredentialsWithoutChangingPassword() throws Exception {
        String token = createTokenFor("Same Password User", "same-password@example.com", "password123");

        mockMvc.perform(put("/auth/credentials")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Same Password Updated",
                                  "email": "same-password-updated@example.com",
                                  "currentPassword": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Same Password Updated"))
                .andExpect(jsonPath("$.email").value("same-password-updated@example.com"))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "same-password-updated@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("same-password-updated@example.com"));
    }

    @Test
    void rejectsCredentialsUpdateWithInvalidCurrentPassword() throws Exception {
        String token = createTokenFor("Protected User", "protected@example.com", "password123");

        mockMvc.perform(put("/auth/credentials")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Should Not Update",
                                  "email": "should-not-update@example.com",
                                  "currentPassword": "wrong-password",
                                  "newPassword": "new-password123"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Current password is invalid"));

        assertThat(userRepository.findByEmail("protected@example.com")).isPresent();
        assertThat(userRepository.findByEmail("should-not-update@example.com")).isEmpty();
    }

    @Test
    void rejectsCredentialsUpdateWithDuplicateEmailFromAnotherUser() throws Exception {
        String token = createTokenFor("Owner User", "owner@example.com", "password123");
        userRegistrationService.register(new RegisterUserRequest(
                "Existing User",
                "existing@example.com",
                "password456"
        ));

        mockMvc.perform(put("/auth/credentials")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Owner Renamed",
                                  "email": "EXISTING@example.com",
                                  "currentPassword": "password123",
                                  "newPassword": "new-password123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already registered: existing@example.com"));

        User owner = userRepository.findByEmail("owner@example.com").orElseThrow();
        assertThat(owner.getName()).isEqualTo("Owner User");
        assertThat(userRepository.findByEmail("existing@example.com")).isPresent();
    }

    @Test
    void rejectsInvalidCredentialsUpdatePayload() throws Exception {
        String token = createTokenFor("Invalid Update", "invalid-update@example.com", "password123");

        mockMvc.perform(put("/auth/credentials")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " ",
                                  "email": "not-an-email",
                                  "currentPassword": "short",
                                  "newPassword": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.currentPassword").exists())
                .andExpect(jsonPath("$.errors.newPassword").exists());

        assertThat(userRepository.findByEmail("invalid-update@example.com")).isPresent();
    }

    @Test
    void rejectsInvalidLoginPayload() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    private String createTokenFor(String name, String email, String password) {
        userRegistrationService.register(new RegisterUserRequest(name, email, password));
        User user = userRepository.findByEmail(email).orElseThrow();
        return jwtTokenService.generateToken(user);
    }
}
