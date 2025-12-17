package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
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

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private final com.project.back_end.services.Service service; // Used for password hashing

    // 2. Constructor Injection for Dependencies
    @Autowired
    public DoctorService(DoctorRepository doctorRepository, 
                         AppointmentRepository appointmentRepository, 
                         TokenService tokenService,
                         com.project.back_end.services.Service service) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
        this.service = service;
    }

    // 4. getDoctorAvailability Method
    // Retrieves available slots for a specific date by removing booked appointments.
    @Transactional(readOnly = true) // 3. Add @Transactional
    public List<String> getDoctorAvailability(Long doctorId, String date) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) return new ArrayList<>();

        Doctor doctor = doctorOpt.get();
        List<String> allSlots = doctor.getAvailableTimes(); // e.g., ["09:00", "10:00"]
        
        // Calculate start and end of the day for the query
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);

        // Fetch booked appointments
        List<Appointment> bookedAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);

        // Convert booked appointment times to strings (HH:mm) for comparison
        List<String> bookedTimes = bookedAppointments.stream()
                .map(appt -> appt.getAppointmentTime().toLocalTime().toString()) 
                .collect(Collectors.toList());

        // Filter: Keep slots that are NOT in bookedTimes
        return allSlots.stream()
                .filter(slot -> !bookedTimes.contains(slot))
                .collect(Collectors.toList());
    }

    // 5. saveDoctor Method
    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1; // Conflict
            }
            // Hash password before saving
            doctor.setPassword(service.hashPassword(doctor.getPassword()));
            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Error
        }
    }

    // 6. updateDoctor Method
    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            if (!doctorRepository.existsById(doctor.getId())) {
                return -1; // Not found
            }
            // Ensure we don't overwrite password with null/empty if not provided
            // Logic depends on specific update requirements, usually fetching existing first
            Doctor existing = doctorRepository.findById(doctor.getId()).get();
            
            // Map fields to update
            existing.setName(doctor.getName());
            existing.setPhone(doctor.getPhone());
            existing.setSpecialty(doctor.getSpecialty());
            existing.setAvailableTimes(doctor.getAvailableTimes());
            // Only update password if a new one is provided
            if (doctor.getPassword() != null && !doctor.getPassword().isEmpty()) {
                existing.setPassword(service.hashPassword(doctor.getPassword()));
            }

            doctorRepository.save(existing);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 7. getDoctors Method
    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    // 8. deleteDoctor Method
    @Transactional
    public int deleteDoctor(Long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1;
            }
            // Delete associated appointments first
            appointmentRepository.deleteAllByDoctorId(id);
            // Delete doctor
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 9. validateDoctor Method
    public String validateDoctor(Login login) {
        Doctor doctor = doctorRepository.findByEmail(login.getEmail());
        if (doctor != null && service.checkPassword(login.getPassword(), doctor.getPassword())) {
            return tokenService.generateToken(doctor.getId(), "doctor");
        }
        return "Invalid Credentials";
    }

    // 10. findDoctorByName Method
    @Transactional(readOnly = true) //*******
    public Map<String, Object> findDoctorByName(String name) {
        return wrapDoctors(doctorRepository.findByNameLike(name));
    }

    // Helper to check time period (AM/PM)
    private boolean isTimeInPeriod(String time, String period) {
        // Assuming time format "HH:mm"
        int hour = Integer.parseInt(time.split(":")[0]);
        if ("AM".equalsIgnoreCase(period)) {
            return hour < 12;
        } else if ("PM".equalsIgnoreCase(period)) {
            return hour >= 12;
        }
        return false;
    }

    // Helper to filter a list of doctors by time availability
    private List<Doctor> filterListByTime(List<Doctor> doctors, String timePeriod) {
        return doctors.stream()
                .filter(doc -> doc.getAvailableTimes().stream()
                        .anyMatch(t -> isTimeInPeriod(t, timePeriod)))
                .collect(Collectors.toList());
    }

    // 11. filterDoctorsByNameSpecilityandTime Method
    @Transactional(readOnly = true) //########
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String time) {
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        return wrapDoctors(filterListByTime(doctors, time));
    }

    // 12. filterDoctorByTime Method (Assuming single doctor list filtering logic is handled in filterDoctorsByTime below)
    // 17. filterDoctorsByTime Method
    // Filters ALL doctors by time.
    @Transactional(readOnly = true) //############
    public Map<String, Object> filterDoctorsByTime(String time) {
        List<Doctor> allDoctors = doctorRepository.findAll();
        return wrapDoctors(filterListByTime(allDoctors, time));
    }

    // 13. filterDoctorByNameAndTime Method
    @Transactional(readOnly = true) //##########
    public Map<String, Object> filterDoctorByNameAndTime(String name, String time) {
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        return wrapDoctors(filterListByTime(doctors, time));
    }

    // 14. filterDoctorByNameAndSpecility Method
    @Transactional(readOnly = true) //###########
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        return wrapDoctors(doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty));
    }

    // 15. filterDoctorByTimeAndSpecility Method
    @Transactional(readOnly = true) //####
    public Map<String, Object> filterDoctorByTimeAndSpecility(String time, String specialty) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        return wrapDoctors(filterListByTime(doctors, time));
    }

    // 16. filterDoctorBySpecility Method
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        return wrapDoctors(doctorRepository.findBySpecialtyIgnoreCase(specialty));
    }

    // Helper to wrap list in Map
    private Map<String, Object> wrapDoctors(List<Doctor> doctors) {
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctors);
        return response;
    }
}