package com.personalcrm.appointment;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<AppointmentResponse> listAppointments(Principal principal) {
        return appointmentService.listAppointments(principal.getName());
    }

    @GetMapping("/upcoming")
    public List<AppointmentResponse> listUpcomingAppointments(Principal principal) {
        return appointmentService.listUpcomingAppointments(principal.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse createAppointment(
            Principal principal,
            @Valid @RequestBody AppointmentRequest request
    ) {
        return appointmentService.createAppointment(principal.getName(), request);
    }

    @GetMapping("/{id}")
    public AppointmentResponse getAppointment(Principal principal, @PathVariable Long id) {
        return appointmentService.getAppointment(principal.getName(), id);
    }

    @PutMapping("/{id}")
    public AppointmentResponse updateAppointment(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request
    ) {
        return appointmentService.updateAppointment(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAppointment(Principal principal, @PathVariable Long id) {
        appointmentService.deleteAppointment(principal.getName(), id);
    }
}
