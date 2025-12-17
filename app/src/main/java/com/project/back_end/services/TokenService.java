package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

// 1. @Component Annotation
@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    // Inject secret from application.properties
    @Value("${jwt.secret}")
    private String secret;

    // 2. Constructor Injection for Dependencies
    @Autowired
    public TokenService(AdminRepository adminRepository, 
                        DoctorRepository doctorRepository, 
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    // 3. getSigningKey Method
    // Retrieves the signing key used for JWT token signing.
    private SecretKey getSigningKey() {
        // Uses Keys.hmacShaKeyFor() to convert the secret into a valid Key
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 4. generateToken Method
    // Generates a JWT token for a given user's identifier.
    public String generateToken(String identifier) {
        return Jwts.builder()
                .setSubject(identifier) // Sets the identifier (username/email) as subject
                .setIssuedAt(new Date()) // Current date
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7 days expiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Sign with key
                .compact();
    }

    // 5. extractIdentifier Method
    // Extracts the identifier (subject) from a JWT token.
    public String extractIdentifier(String token) {
        // ERROR FIX: Using Jwts.parser() instead of parserBuilder() to match common 0.9.x versions and your hints
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Alias for getEmailFromToken if other services rely on that naming
    public String getEmailFromToken(String token) {
        return extractIdentifier(token);
    }

    // 6. validateToken Method
    // Validates the JWT token for a given user type.
    public boolean validateToken(String token, String userType) {
        try {
            String identifier = extractIdentifier(token);
            
            if (identifier == null || identifier.isEmpty()) {
                return false;
            }

            // Check if the user exists in the database based on the user type
            switch (userType.toLowerCase()) {
                case "admin":
                    // For admin, the identifier is the username
                    return adminRepository.findByUsername(identifier) != null;
                case "doctor":
                    // For doctor, the identifier is the email
                    return doctorRepository.findByEmail(identifier) != null;
                case "patient":
                    // For patient, the identifier is the email
                    return patientRepository.findByEmail(identifier) != null;
                default:
                    return false;
            }
        } catch (Exception e) {
            // Token is invalid or expired
            return false;
        }
    }
    
    // Helper to get Patient ID explicitly if needed by PatientService/Controller
    public Long getPatientIdFromToken(String token) {
        try {
            String email = extractIdentifier(token);
            var patient = patientRepository.findByEmail(email);
            return (patient != null) ? patient.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}