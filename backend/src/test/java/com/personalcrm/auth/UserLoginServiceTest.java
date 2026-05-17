package com.personalcrm.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.personalcrm.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserLoginServiceTest {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void logsInWithValidCredentials() {
        userRegistrationService.register(new RegisterUserRequest(
                "Mary Jackson",
                "mary@example.com",
                "password123"
        ));

        AuthenticatedUserResponse response = userLoginService.login(new LoginRequest(
                "MARY@example.com",
                "password123"
        ));

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Mary Jackson");
        assertThat(response.email()).isEqualTo("mary@example.com");
    }

    @Test
    void rejectsInvalidPassword() {
        userRegistrationService.register(new RegisterUserRequest(
                "Dorothy Vaughan",
                "dorothy@example.com",
                "password123"
        ));

        assertThatThrownBy(() -> userLoginService.login(new LoginRequest(
                "dorothy@example.com",
                "wrong-password"
        )))
                .isInstanceOf(AuthenticationException.class);
    }
}
