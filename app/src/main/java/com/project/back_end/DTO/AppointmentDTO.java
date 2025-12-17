package com.project.back_end.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AppointmentDTO {
// 1. 'id' field
private Long id;

// 2. 'doctorId' field
private Long doctorId;

// 3. 'doctorName' field
private String doctorName;

// 4. 'patientId' field
private Long patientId;

// 5. 'patientName' field
private String patientName;

// 6. 'patientEmail' field
private String patientEmail;

// 7. 'patientPhone' field
private String patientPhone;

// 8. 'patientAddress' field
private String patientAddress;

// 9. 'appointmentTime' field
private LocalDateTime appointmentTime;

// 10. 'status' field
private int status;

// 11-13. Derived fields
private LocalDate appointmentDate;      //
private LocalTime appointmentTimeOnly;  //
private LocalDateTime endTime;          //

// 14. Constructor
// Accepts all relevant fields and calculates derived time fields
public AppointmentDTO(Long id, Long doctorId, String doctorName, 
                      Long patientId, String patientName, String patientEmail, 
                      String patientPhone, String patientAddress, 
                      LocalDateTime appointmentTime, int status) {
    this.id = id;
    this.doctorId = doctorId;
    this.doctorName = doctorName;
    this.patientId = patientId;
    this.patientName = patientName;
    this.patientEmail = patientEmail;
    this.patientPhone = patientPhone;
    this.patientAddress = patientAddress;
    this.appointmentTime = appointmentTime;
    this.status = status;

    // Calculate custom fields based on appointmentTime
    if (appointmentTime != null) {
        this.appointmentDate = appointmentTime.toLocalDate();
        this.appointmentTimeOnly = appointmentTime.toLocalTime();
        this.endTime = appointmentTime.plusHours(1); // Adds 1 hour duration
    }
}

// Default constructor
public AppointmentDTO() {}

// 15. Getters
public Long getId() { return id; }
public void setId(Long id) { this.id = id; }

public Long getDoctorId() { return doctorId; }
public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

public String getDoctorName() { return doctorName; }
public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

public Long getPatientId() { return patientId; }
public void setPatientId(Long patientId) { this.patientId = patientId; }

public String getPatientName() { return patientName; }
public void setPatientName(String patientName) { this.patientName = patientName; }

public String getPatientEmail() { return patientEmail; }
public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

public String getPatientPhone() { return patientPhone; }
public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

public String getPatientAddress() { return patientAddress; }
public void setPatientAddress(String patientAddress) { this.patientAddress = patientAddress; }

public LocalDateTime getAppointmentTime() { return appointmentTime; }
public void setAppointmentTime(LocalDateTime appointmentTime) { 
    this.appointmentTime = appointmentTime;
    // Recalculate derived fields if time is updated
    if (appointmentTime != null) {
        this.appointmentDate = appointmentTime.toLocalDate();
        this.appointmentTimeOnly = appointmentTime.toLocalTime();
        this.endTime = appointmentTime.plusHours(1);
    }
}

public int getStatus() { return status; }
public void setStatus(int status) { this.status = status; }

// Getters for derived fields
public LocalDate getAppointmentDate() { return appointmentDate; }
public LocalTime getAppointmentTimeOnly() { return appointmentTimeOnly; }
public LocalDateTime getEndTime() { return endTime; }
}
