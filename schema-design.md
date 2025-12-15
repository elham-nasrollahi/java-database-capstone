## MySQL Database Design
### Table: Patient
CREATE TABLE patient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,        
    name VARCHAR(100) NOT NULL,                  
    email VARCHAR(255) NOT NULL UNIQUE,          
    password VARCHAR(255) NOT NULL,              
    phone VARCHAR(10) NOT NULL,                  
    address VARCHAR(255) NOT NULL                
);

### Table: Doctor
CREATE TABLE doctor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,        
    name VARCHAR(100) NOT NULL,                  
    specialty VARCHAR(50) NOT NULL,              
    email VARCHAR(255) NOT NULL UNIQUE,          
    password VARCHAR(255) NOT NULL,              
    phone VARCHAR(10) NOT NULL,                  
    available_times JSON                         
);

### Table: Admin
CREATE TABLE admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,        
    username VARCHAR(255) NOT NULL UNIQUE,       
    password VARCHAR(255) NOT NULL               
);

### Table: Appointment
CREATE TABLE appointment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,        
    doctor_id BIGINT NOT NULL,                   
    patient_id BIGINT NOT NULL,                  
    appointment_time DATETIME NOT NULL,          
    status INT NOT NULL DEFAULT 0,               
    CONSTRAINT fk_appointment_doctor 
        FOREIGN KEY (doctor_id) REFERENCES doctor(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_appointment_patient 
        FOREIGN KEY (patient_id) REFERENCES patient(id) 
        ON DELETE CASCADE
);

## MongoDB Collection Design

### Collection: prescription
{
  "id": "653a1b2c8d9e4f0012345678",
  "patientName": "Sarah Conner",
  "appointmentId": 105,
  "medication": "Amoxicillin",
  "dosage": "500mg, take 1 capsule every 8 hours for 7 days",
  "doctorNotes": "Patient has a mild penicillin allergy history, monitoring required."
}

### Collection: Feedback
{
  "id": "653a1c3d9e0f5a0098765432",
  "patientId": 42,
  "doctorId": 7,
  "rating": 5,
  "comments": "Dr. Smith was incredibly professional and explained the diagnosis clearly.",
  "tags": [
    "knowledgeable",
    "on-time",
    "clean facility"
  ],
  "submittedAt": "2023-10-27T14:30:00"
}

### Collection: SystemLog
{
  "id": "653a1d4e0f1a6b0011223344",
  "level": "INFO",
  "service": "AppointmentService",
  "message": "Appointment status updated from SCHEDULED to CANCELLED",
  "userId": 88,
  "timestamp": "2023-10-27T15:45:12.123",
  "metadata": {
    "userRole": "DOCTOR",
    "clientIp": "192.168.1.15",
    "actionSource": "mobile-app"
  }
}
