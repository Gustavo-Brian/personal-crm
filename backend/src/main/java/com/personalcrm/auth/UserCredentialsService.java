package com.personalcrm.auth;

import com.personalcrm.auth.jwt.JwtTokenService;
import com.personalcrm.user.User;
import com.personalcrm.user.UserRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserCredentialsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public UserCredentialsService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthenticatedUserResponse updateCredentials(String authenticatedEmail, UpdateCredentialsRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(authenticatedEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException();
        }

        String newEmail = normalizeEmail(request.email());
        userRepository.findByEmail(newEmail)
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {
                    throw new DuplicateEmailException(newEmail);
                });

        user.updateName(request.name().trim());
        user.updateEmail(newEmail);

        if (request.newPassword() != null) {
            user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
        }

        return AuthenticatedUserResponse.from(user, jwtTokenService.generateToken(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
