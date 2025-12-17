package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// 1. Set Up the Controller Class
@RestController
@RequestMapping("/patient")
public class PatientController {

    // 2. Autowire Dependencies
    private final PatientService patientService;
    private final Service service;

    @Autowired
    public PatientController(PatientService patientService, Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    // 3. Define the `getPatient` Method
    // Handles GET requests to retrieve patient details using a token.
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        // Validate token for "patient" role
        if (service.validateToken(token, "patient")) {
            // Fetch patient details
            return patientService.getPatientDetails(token);
        } else {
            response.put("message", "Unauthorized access.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // 4. Define the `createPatient` Method
    // Handles POST requests for patient registration.
    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@RequestBody Patient patient) {
        Map<String, String> response = new HashMap<>();

        // Check if patient already exists (validatePatient returns true if valid/not exists)
        if (service.validatePatient(patient)) {
            // Attempt to create patient
            int result = patientService.createPatient(patient);

            if (result == 1) {
                response.put("message", "Signup successful");
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } else {
                response.put("message", "Internal server error");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put("message", "Patient with email id or phone no already exist");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    // 5. Define the `login` Method
    // Handles POST requests for patient login.
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Login login) {
        // Delegates authentication to service.validatePatientLogin
        return service.validatePatientLogin(login);
    }

    // 6. Define the `getPatientAppointment` Method
    // Handles GET requests to fetch appointment details for a specific patient.
    @GetMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(@PathVariable Long id,
                                                                     @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        // Validate token for "patient" role
        if (service.validateToken(token, "patient")) {
            // Fetch appointments
            return patientService.getPatientAppointment(id, token);
        } else {
            response.put("message", "Unauthorized access.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // 7. Define the `filterPatientAppointment` Method
    // Handles GET requests to filter a patient's appointments.
    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(@PathVariable String condition,
                                                                        @PathVariable String name,
                                                                        @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        // Validate token for "patient" role
        if (service.validateToken(token, "patient")) {
            // Delegates filtering logic to service.filterPatient
            return service.filterPatient(condition, name, token);
        } else {
            response.put("message", "Unauthorized access.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
}