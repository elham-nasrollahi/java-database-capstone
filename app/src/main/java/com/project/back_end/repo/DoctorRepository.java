package com.project.back_end.repo;

import com.project.back_end.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // 1. Extend JpaRepository
    // Inherits basic CRUD functionality (save, delete, update, find, etc.).

    // 2. Custom Query Methods

    // **findByEmail**
    // Retrieves a Doctor by their email.
    Doctor findByEmail(String email);

    // **findByNameLike**
    // Retrieves a list of Doctors whose name contains the search string.
    // Using @Query to handle the CONCAT pattern matching as described.
    @Query("SELECT d FROM Doctor d WHERE d.name LIKE CONCAT('%', :name, '%')")
    List<Doctor> findByNameLike(@Param("name") String name);

    // **findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase**
    // Updated to use explicit @Query with LOWER, CONCAT, and LIKE as requested.
    // - Name: Partial match (LIKE %...%), Case-insensitive (LOWER)
    // - Specialty: Exact match (=), Case-insensitive (LOWER)
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) AND LOWER(d.specialty) = LOWER(:specialty)")
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(@Param("name") String name, @Param("specialty") String specialty);

    // **findBySpecialtyIgnoreCase**
    // Retrieves doctors with a specific specialty, ignoring case.
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);

}