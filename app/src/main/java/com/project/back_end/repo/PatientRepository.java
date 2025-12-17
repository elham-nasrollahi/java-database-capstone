package com.project.back_end.repo;

import com.project.back_end.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // **findByEmail**
    // Retrieves a Patient by their email address.
    Patient findByEmail(String email);

    // **findByEmailOrPhone**
    // Retrieves a Patient by either their email or phone number.
    Patient findByEmailOrPhone(String email, String phone);

}