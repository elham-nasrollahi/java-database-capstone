package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// 1. Add @Service Annotation
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final com.project.back_end.services.Service service; // Generic Service dependency
    private final TokenService tokenService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    // 2. Constructor Injection for Dependencies
    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository,
                              com.project.back_end.services.Service service,
                              TokenService tokenService,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.service = service;
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    // 4. Book Appointment Method
    // Saves the new appointment to the database. Returns 1 for success, 0 for failure.
    @Transactional // 3. Add @Transactional Annotation
    public int bookAppointment(Appointment appointment) {
        try {
            // Additional logic to check doctor availability could go here
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 5. Update Appointment Method
    // Validates patient ID matches, checks availability, and updates.
    @Transactional
    public String updateAppointment(Long id, Appointment updatedDetails, String token) {
        try {
            // Validate the patient ID from token matches the appointment owner
            Long patientId = tokenService.getPatientIdFromToken(token); // Assuming method exists
            Optional<Appointment> existingApptOpt = appointmentRepository.findById(id);

            if (existingApptOpt.isEmpty()) {
                return "Appointment not found.";
            }

            Appointment existingAppt = existingApptOpt.get();

            if (!existingAppt.getPatient().getId().equals(patientId)) {
                return "Unauthorized: You can only update your own appointments.";
            }

            // Check if the doctor is available at the new specified time
            // (Simplified check: actual logic would query doctor's available times/slots)
            boolean isDoctorAvailable = true; // Placeholder for actual availability logic

            if (!isDoctorAvailable) {
                return "Doctor is not available at the selected time.";
            }

            // Update details
            existingAppt.setAppointmentTime(updatedDetails.getAppointmentTime());
            // Update other fields as necessary
            
            appointmentRepository.save(existingAppt);
            return "Appointment updated successfully.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error updating appointment.";
        }
    }

    // 6. Cancel Appointment Method
    // Cancels (deletes) an appointment after checking patient ownership.
    @Transactional
    public String cancelAppointment(Long id, String token) {
        try {
            Long patientId = tokenService.getPatientIdFromToken(token);
            Optional<Appointment> existingApptOpt = appointmentRepository.findById(id);

            if (existingApptOpt.isPresent()) {
                Appointment appt = existingApptOpt.get();
                // Check for patient ID match
                if (appt.getPatient().getId().equals(patientId)) {
                    appointmentRepository.deleteById(id);
                    return "Appointment cancelled successfully.";
                } else {
                    return "Unauthorized action.";
                }
            } else {
                return "Appointment not found.";
            }
        } catch (Exception e) {
            return "Error cancelling appointment.";
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAppointments(Long doctorId, String date, String patientName) {
        // Parse date
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);

        List<Appointment> appointments;

        // Logic to fetch the list based on filters
        if (patientName != null && !patientName.equalsIgnoreCase("null") && !patientName.trim().isEmpty()) {
            appointments = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doctorId, patientName, startOfDay, endOfDay);
        } else {
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    doctorId, startOfDay, endOfDay);
        }

        // Wrap the list in a Map
        Map<String, Object> response = new HashMap<>();
        response.put("appointments", appointments);
        
        return response;
    }

    // 8. Change Status Method
    // Updates the status of an appointment.
    @Transactional // Add @Transactional to ensure atomicity
    public void changeStatus(int status, long id) {
        appointmentRepository.updateStatus(status, id);
    }
}