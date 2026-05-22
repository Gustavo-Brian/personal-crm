package com.personalcrm.common;

import com.personalcrm.auth.DuplicateEmailException;
import com.personalcrm.auth.InvalidCurrentPasswordException;
import com.personalcrm.contact.AcademicFormationNotFoundException;
import com.personalcrm.contact.ContactNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicateEmail(DuplicateEmailException exception) {
        return ApiError.of(exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleAuthentication(AuthenticationException exception) {
        return ApiError.of("Invalid email or password");
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleInvalidCurrentPassword(InvalidCurrentPasswordException exception) {
        return ApiError.of(exception.getMessage());
    }

    @ExceptionHandler(ContactNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleContactNotFound(ContactNotFoundException exception) {
        return ApiError.of(exception.getMessage());
    }

    @ExceptionHandler(AcademicFormationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleAcademicFormationNotFound(AcademicFormationNotFoundException exception) {
        return ApiError.of(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ApiError.validation(errors);
    }
}
