CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,        --
    name VARCHAR(100) NOT NULL,                  --
    email VARCHAR(255) NOT NULL UNIQUE,          --
    password VARCHAR(255) NOT NULL,              --
    phone VARCHAR(10) NOT NULL,                  --
    address VARCHAR(255) NOT NULL                --
);

CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,        --
    name VARCHAR(100) NOT NULL,                  --
    specialty VARCHAR(50) NOT NULL,              --
    email VARCHAR(255) NOT NULL UNIQUE,          --
    password VARCHAR(255) NOT NULL,              --
    phone VARCHAR(10) NOT NULL,                  --
    available_times JSON                         --
);

CREATE TABLE admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,        --
    username VARCHAR(255) NOT NULL UNIQUE,       --
    password VARCHAR(255) NOT NULL               --
);

CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,        --
    doctor_id BIGINT NOT NULL,                   --
    patient_id BIGINT NOT NULL,                  --
    appointment_time DATETIME NOT NULL,          --
    status INT NOT NULL DEFAULT 0,               --
    Foreign Key Constraints
    CONSTRAINT fk_appointment_doctor 
        FOREIGN KEY (doctor_id) REFERENCES doctors(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_appointment_patient 
        FOREIGN KEY (patient_id) REFERENCES patients(id) 
        ON DELETE CASCADE
);
