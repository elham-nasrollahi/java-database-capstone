package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 1. Add @Service Annotation
@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    // 2. Constructor Injection for Dependencies
    @Autowired
    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    // 3. savePrescription Method
    // Saves a prescription if one does not already exist for the appointment.
    // Returns ResponseEntity<Map<String, String>> as requested.
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> response = new HashMap<>();
        try {
            // Check if a prescription already exists for the same appointment
            List<Prescription> existing = prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());

            if (!existing.isEmpty()) {
                // Return 400 Bad Request if it exists
                response.put("message", "Prescription already exists for this appointment.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Save the new prescription
            prescriptionRepository.save(prescription);

            // Return 201 Created on success
            response.put("message", "Prescription saved");
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            // 5. Exception Handling
            response.put("message", "Error saving prescription: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4. getPrescription Method
    // Retrieves prescription details. Returns ResponseEntity<Map<String, Object>>.
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Retrieve prescriptions associated with the appointment ID
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);

            // Return 200 OK with data
            response.put("prescriptions", prescriptions);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            // 5. Exception Handling
            response.put("message", "Error fetching prescription: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}