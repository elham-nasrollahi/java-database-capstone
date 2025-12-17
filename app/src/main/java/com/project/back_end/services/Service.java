package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// 1. @Service Annotation
@org.springframework.stereotype.Service
public class Service {

    // Declare necessary services and repositories
    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    // 2. Constructor Injection for Dependencies
    @Autowired
    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    // ----------------------------------------------------------------------
    // Authentication & Validation
    // ----------------------------------------------------------------------

    // validateToken Method
    // Checks validity of a token. Returns boolean for internal logic, or ResponseEntity if responding to API.
    // NOTE: DashboardController expects a boolean return, but the prompt asks for ResponseEntity.
    // I am providing the boolean overload for internal use (DashboardController) and ResponseEntity for the prompt requirement.
    public boolean validateToken(String token, String user) {
        return tokenService.validateToken(token, user);
    }

    // Prompt-specific implementation returning ResponseEntity
    public ResponseEntity<Map<String, String>> validateTokenResponse(String token, String user) {
        Map<String, String> response = new HashMap<>();
        if (!tokenService.validateToken(token, user)) {
            response.put("message", "Invalid or expired token.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return null; // or OK
    }

    // 4. validateAdmin Method
    // Validates admin credentials.
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        try {
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
            if (admin != null && admin.getPassword().equals(receivedAdmin.getPassword())) {
                String token = tokenService.generateToken(admin.getUsername());
                response.put("token", token);
                response.put("message", "Login successful");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("message", "Invalid credentials");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            response.put("message", "Internal Server Error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 8. validatePatientLogin Method
    // Validates patient credentials.
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        try {
            Patient patient = patientRepository.findByEmail(login.getEmail());
            // Note: In a real app, use checkPassword() hash comparison here.
            if (patient != null && patient.getPassword().equals(login.getPassword())) {
                String token = tokenService.generateToken(patient.getEmail());
                response.put("token", token);
                response.put("message", "Login successful");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("message", "Invalid credentials");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            response.put("message", "Internal Server Error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ----------------------------------------------------------------------
    // Doctor Management
    // ----------------------------------------------------------------------

    // 5. filterDoctor Method
    // Filters doctors based on criteria.
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        if (name != null && specialty != null && time != null) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
        } else if (name != null && time != null) {
            return doctorService.filterDoctorByNameAndTime(name, time);
        } else if (name != null && specialty != null) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        } else if (time != null && specialty != null) {
            return doctorService.filterDoctorByTimeAndSpecility(time, specialty);
        } else if (name != null) {
            return doctorService.findDoctorByName(name);
        } else if (specialty != null) {
            return doctorService.filterDoctorBySpecility(specialty);
        } else if (time != null) {
            return doctorService.filterDoctorsByTime(time);
        } else {
            // Return all doctors if no filter
            Map<String, Object> response = new HashMap<>();
            response.put("doctors", doctorService.getDoctors());
            return response;
        }
    }

    // 6. validateAppointment Method
    // Validates if appointment time is available.
    // Returns: 1 (valid), 0 (unavailable), -1 (doctor not found)
    public int validateAppointment(Appointment appointment) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(appointment.getDoctor().getId());
        
        if (doctorOpt.isEmpty()) {
            return -1; // Doctor doesn't exist
        }

        // Get available slots for the specific date
        List<String> availableSlots = doctorService.getDoctorAvailability(
                appointment.getDoctor().getId(), 
                appointment.getAppointmentDate().toString()
        );

        // Check if requested time is in available slots
        String requestedTime = appointment.getAppointmentTimeOnly().toString();
        // Handle simpler HH:mm format matching if necessary
        boolean isAvailable = availableSlots.stream().anyMatch(slot -> slot.startsWith(requestedTime));

        return isAvailable ? 1 : 0;
    }

    // ----------------------------------------------------------------------
    // Patient Management
    // ----------------------------------------------------------------------

    // 7. validatePatient Method
    // Checks if patient exists (returns false if exists, true if valid/new).
    public boolean validatePatient(Patient patient) {
        Patient existing = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
        return existing == null; // Return true if patient does NOT exist (valid for registration)
    }

    // 9. filterPatient Method
    // Filters patient appointments.
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        Long patientId = tokenService.getPatientIdFromToken(token);
        
        if (condition != null && name != null) {
            return patientService.filterByDoctorAndCondition(condition, name, patientId);
        } else if (condition != null) {
            return patientService.filterByCondition(condition, patientId);
        } else if (name != null) {
            return patientService.filterByDoctor(name, patientId);
        } else {
            return patientService.getPatientAppointment(patientId, token);
        }
    }

    // ----------------------------------------------------------------------
    // Utility Methods (Required by other services)
    // ----------------------------------------------------------------------
    
    // Hashes a password (placeholder or simple hash)
    public String hashPassword(String password) {
        // In a real application, use BCrypt: return BCrypt.hashpw(password, BCrypt.gensalt());
        return password; 
    }

    // Checks a password
    public boolean checkPassword(String raw, String hashed) {
        // In a real application, use BCrypt: return BCrypt.checkpw(raw, hashed);
        return raw.equals(hashed);
    }
}