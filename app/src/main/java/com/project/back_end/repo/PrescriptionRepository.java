package com.project.back_end.repo;

import com.project.back_end.models.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

    // 1. Extend MongoRepository
    // Extends MongoRepository<Prescription, String> to provide basic CRUD functionality for MongoDB.
    // The ID type is String (standard for MongoDB ObjectId).

    // 2. Custom Query Method: findByAppointmentId
    // Retrieves a list of prescriptions associated with a specific appointment ID.
    // MongoRepository automatically derives the query from this method name.
    List<Prescription> findByAppointmentId(Long appointmentId);

}
