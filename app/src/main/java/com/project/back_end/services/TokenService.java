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

    // Injected from application.properties
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
    // Retrieves the HMAC SHA key used for signing/verifying.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 4. generateToken Method
    // Generates a JWT token for a user identifier (username or email).
    // Expiration is set to 7 days.
    public String generateToken(String identifier) {
        return Jwts.builder()
                .setSubject(identifier)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7 days
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 5. extractIdentifier Method
    // Extracts the identifier (subject) from the token.
    public String extractIdentifier(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Alias method to maintain compatibility with services expecting "getEmailFromToken"
    public String getEmailFromToken(String token) {
        return extractIdentifier(token);
    }

    // 6. validateToken Method
    // Validates the token and checks if the user exists in the specific role repository.
    public boolean validateToken(String token, String role) {
        try {
            String identifier = extractIdentifier(token);
            
            if (identifier == null || identifier.isEmpty()) {
                return false;
            }

            // Check existence based on role
            switch (role.toLowerCase()) {
                case "admin":
                    return adminRepository.findByUsername(identifier) != null;
                case "doctor":
                    return doctorRepository.findByEmail(identifier) != null;
                case "patient":
                    return patientRepository.findByEmail(identifier) != null;
                default:
                    return false;
            }
        } catch (Exception e) {
            // Token invalid or expired
            return false;
        }
    }
    
    // Helper specifically for extracting Patient ID if needed by other services
    public Long getPatientIdFromToken(String token) {
        String email = extractIdentifier(token);
        var patient = patientRepository.findByEmail(email);
        return (patient != null) ? patient.getId() : null;
    }
}