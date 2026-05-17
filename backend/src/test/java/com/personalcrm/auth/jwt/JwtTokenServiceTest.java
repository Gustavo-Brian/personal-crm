package com.personalcrm.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.personalcrm.user.User;
import com.personalcrm.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JwtTokenServiceTest {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void generatesValidTokenAndReadsClaims() {
        User user = userRepository.saveAndFlush(new User(
                "Token User",
                "token@example.com",
                "encoded-password"
        ));

        String token = jwtTokenService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenService.isValid(token)).isTrue();
        assertThat(jwtTokenService.getSubject(token)).isEqualTo("token@example.com");
        assertThat(jwtTokenService.getUserId(token)).isEqualTo(user.getId());
        assertThat(jwtTokenService.getExpiresAt(token)).isNotNull();
    }

    @Test
    void rejectsMalformedToken() {
        String malformedToken = "not-a-jwt";

        assertThat(jwtTokenService.isValid(malformedToken)).isFalse();
        assertThatThrownBy(() -> jwtTokenService.getSubject(malformedToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsTamperedTokenSignature() {
        User user = userRepository.saveAndFlush(new User(
                "Tampered Token User",
                "tampered-token@example.com",
                "encoded-password"
        ));
        String token = jwtTokenService.generateToken(user);
        String tamperedToken = token.substring(0, token.length() - 1)
                + (token.endsWith("a") ? "b" : "a");

        assertThat(jwtTokenService.isValid(tamperedToken)).isFalse();
        assertThatThrownBy(() -> jwtTokenService.getUserId(tamperedToken))
                .isInstanceOf(JwtException.class);
    }
}
