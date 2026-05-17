package com.personalcrm.auth;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRegistrationService userRegistrationService;
    private final UserLoginService userLoginService;
    private final UserCredentialsService userCredentialsService;

    public AuthController(
            UserRegistrationService userRegistrationService,
            UserLoginService userLoginService,
            UserCredentialsService userCredentialsService
    ) {
        this.userRegistrationService = userRegistrationService;
        this.userLoginService = userLoginService;
        this.userCredentialsService = userCredentialsService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisteredUserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        return userRegistrationService.register(request);
    }

    @PostMapping("/login")
    public AuthenticatedUserResponse login(@Valid @RequestBody LoginRequest request) {
        return userLoginService.login(request);
    }

    @PutMapping("/credentials")
    public AuthenticatedUserResponse updateCredentials(
            Principal principal,
            @Valid @RequestBody UpdateCredentialsRequest request
    ) {
        return userCredentialsService.updateCredentials(principal.getName(), request);
    }
}
