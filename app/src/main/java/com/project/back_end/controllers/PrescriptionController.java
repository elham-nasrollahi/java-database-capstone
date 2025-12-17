package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // Assuming Spring Boot 3 / Jakarta EE, otherwise javax.validation
import java.util.HashMap;
import java.util.Map;

// 1. Set Up the Controller Class
@RestController
@RequestMapping("${api.path}" + "prescription")
public class PrescriptionController {

    // 2. Autowire Dependencies
    private final PrescriptionService prescriptionService;
    private final Service service;
    private final AppointmentService appointmentService;

    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService,
                                  Service service,
                                  AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.service = service;
        this.appointmentService = appointmentService;
    }

    // 3. Define the `savePrescription` Method
    // Handles POST requests to save a new prescription. Validates doctor token.
    // Updates appointment status and delegates save logic.
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(@Valid @RequestBody Prescription prescription,
                                                                @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        // Validate token for "doctor" role
        if (service.validateToken(token, "doctor")) {
            // Update the status of the corresponding appointment (e.g., status 1 for "Prescribed/Completed")
            appointmentService.changeStatus(1, prescription.getAppointmentId());

            // Delegate saving logic to PrescriptionService
            return prescriptionService.savePrescription(prescription);
        } else {
            response.put("message", "Unauthorized access.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // 4. Define the `getPrescription` Method
    // Handles GET requests to retrieve a prescription by appointment ID.
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(@PathVariable Long appointmentId,
                                                               @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        // Validate token for "doctor" role
        if (service.validateToken(token, "doctor")) {
            // Fetch prescription using PrescriptionService
            return prescriptionService.getPrescription(appointmentId);
        } else {
            response.put("message", "Unauthorized access.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
}