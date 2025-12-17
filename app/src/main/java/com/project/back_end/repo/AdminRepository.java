package com.project.back_end.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.project.back_end.models.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // 1. Extend JpaRepository
    // The interface extends JpaRepository<Admin, Long> to inherit basic CRUD functionality.

    // 2. Custom Query Method: findByUsername
    // Returns an Admin entity that matches the provided username.
    Admin findByUsername(String username);

}