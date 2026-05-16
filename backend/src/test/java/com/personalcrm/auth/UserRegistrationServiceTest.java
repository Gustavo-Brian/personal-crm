package com.personalcrm.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.personalcrm.user.User;
import com.personalcrm.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserRegistrationServiceTest {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registersUserWithEncryptedPassword() {
        RegisterUserRequest request = new RegisterUserRequest(
                "Grace Hopper",
                "Grace@example.com",
                "password123"
        );

        RegisteredUserResponse response = userRegistrationService.register(request);

        User user = userRepository.findByEmail("grace@example.com").orElseThrow();
        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.email()).isEqualTo("grace@example.com");
        assertThat(user.getPasswordHash()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", user.getPasswordHash())).isTrue();
    }

    @Test
    void rejectsDuplicateEmailRegistration() {
        RegisterUserRequest firstRequest = new RegisterUserRequest(
                "First User",
                "duplicate@example.com",
                "password123"
        );
        RegisterUserRequest duplicateRequest = new RegisterUserRequest(
                "Second User",
                "DUPLICATE@example.com",
                "password456"
        );

        userRegistrationService.register(firstRequest);

        assertThatThrownBy(() -> userRegistrationService.register(duplicateRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email already registered: duplicate@example.com");
        assertThat(userRepository.count()).isEqualTo(1);
    }
}
