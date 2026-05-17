package com.personalcrm.auth;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.personalcrm.PersonalCrmApplication;
import com.personalcrm.auth.jwt.JwtTokenService;
import com.personalcrm.user.User;
import com.personalcrm.user.UserRepository;
import java.security.Principal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(classes = {PersonalCrmApplication.class, SecurityRoutesTest.ProtectedTestController.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityRoutesTest {

    private static final String PROTECTED_ROUTE = "/test/protected";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerRouteIsPublic() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Public Register",
                                  "email": "public-register@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email").value("public-register@example.com"));
    }

    @Test
    void loginRouteIsPublic() throws Exception {
        userRegistrationService.register(new RegisterUserRequest(
                "Public Login",
                "public-login@example.com",
                "password123"
        ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "public-login@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void protectedRouteRejectsMissingToken() throws Exception {
        mockMvc.perform(get(PROTECTED_ROUTE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRouteRejectsInvalidToken() throws Exception {
        mockMvc.perform(get(PROTECTED_ROUTE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRouteRejectsTokenForMissingUser() throws Exception {
        String token = createTokenFor("deleted-user@example.com");
        userRepository.deleteAll();

        mockMvc.perform(get(PROTECTED_ROUTE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRouteAcceptsValidToken() throws Exception {
        String token = createTokenFor("protected-user@example.com");

        mockMvc.perform(get(PROTECTED_ROUTE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("protected-user@example.com"));
    }

    private String createTokenFor(String email) {
        userRegistrationService.register(new RegisterUserRequest(
                "Protected User",
                email,
                "password123"
        ));
        User user = userRepository.findByEmail(email).orElseThrow();
        return jwtTokenService.generateToken(user);
    }

    @RestController
    static class ProtectedTestController {

        @GetMapping(PROTECTED_ROUTE)
        Map<String, String> protectedRoute(Principal principal) {
            return Map.of("email", principal.getName());
        }
    }
}
