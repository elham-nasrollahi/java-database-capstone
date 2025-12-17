package com.project.back_end.repo;

import com.project.back_end.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // 1. Extend JpaRepository
    // Basic CRUD methods are inherited automatically.

    // 2. Custom Query Methods

    // **findByDoctorIdAndAppointmentTimeBetween**
    // Uses LEFT JOIN FETCH to eagerly load doctor and available times as requested.
    @Query("SELECT DISTINCT a FROM Appointment a " +
           "LEFT JOIN FETCH a.doctor d " +
           "LEFT JOIN FETCH d.availableTimes " +
           "WHERE d.id = :doctorId AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(@Param("doctorId") Long doctorId, 
                                                              @Param("start") LocalDateTime start, 
                                                              @Param("end") LocalDateTime end);

    // **findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween**
    // Uses LEFT JOIN to fetch doctor and patient details.
    @Query("SELECT DISTINCT a FROM Appointment a " +
           "LEFT JOIN FETCH a.doctor d " +
           "LEFT JOIN FETCH a.patient p " +
           "WHERE d.id = :doctorId " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :patientName, '%')) " +
           "AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(@Param("doctorId") Long doctorId, 
                                                                                                 @Param("patientName") String patientName, 
                                                                                                 @Param("start") LocalDateTime start, 
                                                                                                 @Param("end") LocalDateTime end);

    // **deleteAllByDoctorId**
    // Marked as @Modifying and @Transactional.
    @Modifying
    @Transactional
    void deleteAllByDoctorId(Long doctorId);

    // **findByPatientId**
    List<Appointment> findByPatientId(Long patientId);

    // **findByPatient_IdAndStatusOrderByAppointmentTimeAsc**
    List<Appointment> findByPatientIdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    // **filterByDoctorNameAndPatientId**
    // Custom query for filtering by doctor name (LIKE) and patient ID.
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))")
    List<Appointment> filterByDoctorNameAndPatientId(@Param("doctorName") String doctorName, @Param("patientId") Long patientId);

    // **filterByDoctorNameAndPatientIdAndStatus**
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.status = :status AND LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(@Param("doctorName") String doctorName, 
                                                              @Param("patientId") Long patientId, 
                                                              @Param("status") int status);

    // **updateStatus**
    // Updates status for a specific appointment ID.
    @Modifying
    @Transactional
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
    void updateStatus(@Param("status") int status, @Param("id") long id);
}
