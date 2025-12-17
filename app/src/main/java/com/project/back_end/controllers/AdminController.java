
package com.project.back_end.controllers;

import com.project.back_end.models.Admin;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// 1. Set Up the Controller Class
// Annotated as RestController to handle web requests and return JSON.
// Base path configured using the 'api.path' property + "admin".
@RestController
@RequestMapping("${api.path}" + "admin")
public class AdminController {

    private final Service service;

    // 2. Autowire Service Dependency
    // Constructor injection for the Service class.
    @Autowired
    public AdminController(Service service) {
        this.service = service;
    }

    // 3. Define the `adminLogin` Method
    // Handles HTTP POST requests for admin login.
    // Delegates to service.validateAdmin() and returns the ResponseEntity containing the token or error.
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        return service.validateAdmin(admin);
    }
}

