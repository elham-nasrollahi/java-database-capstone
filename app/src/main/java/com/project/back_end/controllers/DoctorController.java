package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 1. Set Up the Controller Class
@RestController
@RequestMapping("${api.path}" + "doctor")
public class DoctorController {

    // 2. Autowire Dependencies
    private final DoctorService doctorService;
    private final Service service;

    @Autowired
    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    // 3. Define the `getDoctorAvailability` Method
    // Handles GET requests to check a specific doctor’s availability.
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(@PathVariable String user,
                                                                     @PathVariable Long doctorId,
                                                                     @PathVariable String date,
                                                                     @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        // Validate the token against the user type
        if (service.validateToken(token, user)) {
            List<String> availability = doctorService.getDoctorAvailability(doctorId, date);
            response.put("availability", availability);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("message", "Unauthorized access.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // 4. Define the `getDoctor` Method
    // Handles GET requests to retrieve a list of all doctors.
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctor() {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorService.getDoctors();
        response.put("doctors", doctors);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 5. Define the `saveDoctor` Method
    // Handles POST requests to register a new doctor. Requires Admin token.
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(@RequestBody Doctor doctor,
                                                          @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        if (service.validateToken(token, "admin")) {
            int result = doctorService.saveDoctor(doctor);
            
            if (result == 1) {
                response.put("message", "Doctor added to db");
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } else if (result == -1) {
                response.put("message", "Doctor already exists");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            } else {
                response.put("message", "Some internal error occurred");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put("message", "Unauthorized access");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // 6. Define the `doctorLogin` Method
    // Handles POST requests for doctor login.
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        Map<String, String> response = new HashMap<>();
        String result = doctorService.validateDoctor(login);

        if (!"Invalid Credentials".equals(result)) {
            response.put("token", result);
            response.put("message", "Login successful");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("message", "Invalid Credentials");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // 7. Define the `updateDoctor` Method
    // Handles PUT requests to update a doctor. Requires Admin token.
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(@RequestBody Doctor doctor,
                                                            @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        if (service.validateToken(token, "admin")) {
            int result = doctorService.updateDoctor(doctor);

            if (result == 1) {
                response.put("message", "Doctor updated");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else if (result == -1) {
                response.put("message", "Doctor not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            } else {
                response.put("message", "Some internal error occurred");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put("message", "Unauthorized access");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // 8. Define the `deleteDoctor` Method
    // Handles DELETE requests to remove a doctor. Requires Admin token.
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long id,
                                                            @PathVariable String token) {
        Map<String, String> response = new HashMap<>();

        if (service.validateToken(token, "admin")) {
            int result = doctorService.deleteDoctor(id);

            if (result == 1) {
                response.put("message", "Doctor deleted successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else if (result == -1) {
                response.put("message", "Doctor not found with id");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            } else {
                response.put("message", "Some internal error occurred");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put("message", "Unauthorized access");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // 9. Define the `filter` Method
    // Handles GET requests to filter doctors.
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filter(@PathVariable String name,
                                                      @PathVariable String time,
                                                      @PathVariable String speciality) {
        // service.filterDoctor() returns a Map<String, Object> (doctors list)
        Map<String, Object> response = service.filterDoctor(name, speciality, time);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}