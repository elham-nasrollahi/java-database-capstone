package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

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
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private final com.project.back_end.services.Service service;

    // 2. Constructor Injection
    @Lazy
    public DoctorService(DoctorRepository doctorRepository, 
                         AppointmentRepository appointmentRepository, 
                         TokenService tokenService,
                         com.project.back_end.services.Service service) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
        this.service = service;
    }

    // 4. getDoctorAvailability
    @Transactional(readOnly = true) 
    public List<String> getDoctorAvailability(Long doctorId, String date) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) return new ArrayList<>();

        Doctor doctor = doctorOpt.get();
        List<String> allSlots = doctor.getAvailableTimes();
        
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);

        List<Appointment> bookedAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);

        List<String> bookedTimes = bookedAppointments.stream()
                .map(appt -> appt.getAppointmentTime().toLocalTime().toString()) 
                .collect(Collectors.toList());

        return allSlots.stream()
                .filter(slot -> !bookedTimes.contains(slot))
                .collect(Collectors.toList());
    }

    // 5. saveDoctor
    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1; 
            }
            doctor.setPassword(service.hashPassword(doctor.getPassword()));
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 6. updateDoctor
    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            if (!doctorRepository.existsById(doctor.getId())) {
                return -1;
            }
            Doctor existing = doctorRepository.findById(doctor.getId()).get();
            
            existing.setName(doctor.getName());
            existing.setPhone(doctor.getPhone());
            existing.setSpecialty(doctor.getSpecialty());
            existing.setAvailableTimes(doctor.getAvailableTimes());
            
            if (doctor.getPassword() != null && !doctor.getPassword().isEmpty()) {
                existing.setPassword(service.hashPassword(doctor.getPassword()));
            }

            doctorRepository.save(existing);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 7. getDoctors
    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    // 8. deleteDoctor
    @Transactional
    public int deleteDoctor(Long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1;
            }
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 9. validateDoctor
    public String validateDoctor(Login login) {
        Doctor doctor = doctorRepository.findByEmail(login.getEmail());
        if (doctor != null && service.checkPassword(login.getPassword(), doctor.getPassword())) {
            // Uses generateToken, which was NOT affected by the parser error
            return tokenService.generateToken(doctor.getEmail());
        }
        return "Invalid Credentials";
    }

    // --------------------------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------------------------
    
    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        return doctors.stream()
                .filter(doc -> doc.getAvailableTimes().stream()
                        .anyMatch(time -> {
                            int hour = Integer.parseInt(time.split(":")[0]);
                            if ("AM".equalsIgnoreCase(amOrPm)) return hour < 12;
                            if ("PM".equalsIgnoreCase(amOrPm)) return hour >= 12;
                            return false;
                        }))
                .collect(Collectors.toList());
    }

    private Map<String, Object> wrapDoctors(List<Doctor> doctors) {
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctors);
        return response;
    }

    // --------------------------------------------------------------------------------
    // Filtering Methods
    // --------------------------------------------------------------------------------

    // 10. findDoctorByName
    @Transactional(readOnly = true)
    public Map<String, Object> findDoctorByName(String name) {
        return wrapDoctors(doctorRepository.findByNameLike(name));
    }

    // 11. filterDoctorsByNameSpecilityandTime
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        return wrapDoctors(filterDoctorByTime(doctors, amOrPm));
    }

    // 17. filterDoctorsByTime
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        List<Doctor> allDoctors = doctorRepository.findAll();
        return wrapDoctors(filterDoctorByTime(allDoctors, amOrPm));
    }

    // 13. filterDoctorByNameAndTime
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        return wrapDoctors(filterDoctorByTime(doctors, amOrPm));
    }

    // 14. filterDoctorByNameAndSpecility
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        return wrapDoctors(doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty));
    }

    // 15. filterDoctorByTimeAndSpecility
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByTimeAndSpecility(String time, String specialty) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        return wrapDoctors(filterDoctorByTime(doctors, time));
    }

    // 16. filterDoctorBySpecility
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        return wrapDoctors(doctorRepository.findBySpecialtyIgnoreCase(specialty));
    }
}