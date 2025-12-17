package com.project.back_end.controllers;


import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import com.project.back_end.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// 1. Set Up the Controller Class
@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    // 2. Autowire Dependencies
    private final AppointmentService appointmentService;
    private final Service service;
    
    // Additional dependencies needed to resolve Token -> DoctorId
    private final TokenService tokenService;
    private final DoctorRepository doctorRepository;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, 
                                 Service service,
                                 TokenService tokenService,
                                 DoctorRepository doctorRepository) {
        this.appointmentService = appointmentService;
        this.service = service;
        this.tokenService = tokenService;
        this.doctorRepository = doctorRepository;
    }

    // 3. Define the `getAppointments` Method
    // Handles GET requests to fetch appointments. Validates doctor token first.
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(@PathVariable String date,
                                                               @PathVariable String patientName,
                                                               @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        // Validate token for "doctor" role
        if (service.validateToken(token, "doctor")) {
            // Logic to retrieve doctorId from token to pass to service
            String email = tokenService.extractIdentifier(token);
            Doctor doctor = doctorRepository.findByEmail(email);
            
            if (doctor != null) {
                // Fetch appointments using appointmentService
                Map<String, Object> appointments = appointmentService.getAppointments(doctor.getId(), date, patientName);
                return new ResponseEntity<>(appointments, HttpStatus.OK);
            } else {
                response.put("message", "Doctor not found.");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } else {
            response.put("message", "Unauthorized access.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // 4. Define the `bookAppointment` Method
    // Handles POST requests to book a new appointment. Validates patient token.
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(@RequestBody Appointment appointment,
                                                               @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        // Validate token for "patient" role
        if (service.validateToken(token, "patient")) {
            // Validate appointment availability using Service
            int validationResult = service.validateAppointment(appointment);

            if (validationResult == 1) {
                // Proceed to book
                int result = appointmentService.bookAppointment(appointment);
                if (result == 1) {
                    response.put("message", "Appointment booked successfully.");
                    return new ResponseEntity<>(response, HttpStatus.CREATED);
                } else {
                    response.put("message", "Failed to book appointment.");
                    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else if (validationResult == 0) {
                response.put("message", "Time slot not available.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            } else {
                response.put("message", "Doctor not found.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } else {
            response.put("message", "Unauthorized access.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // 5. Define the `updateAppointment` Method
    // Handles PUT requests to update an appointment.
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(@RequestBody Appointment appointment,
                                                                 @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        if (service.validateToken(token, "patient")) {
            // Delegate to service
            String result = appointmentService.updateAppointment(appointment.getId(), appointment, token);
            
            response.put("message", result);
            if ("Appointment updated successfully.".equals(result)) {
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } else {
            response.put("message", "Unauthorized access.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // 6. Define the `cancelAppointment` Method
    // Handles DELETE requests to cancel an appointment.
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable Long id,
                                                                 @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        if (service.validateToken(token, "patient")) {
            // Delegate to service
            String result = appointmentService.cancelAppointment(id, token);
            
            response.put("message", result);
            if ("Appointment cancelled successfully.".equals(result)) {
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } else {
            response.put("message", "Unauthorized access.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
}
