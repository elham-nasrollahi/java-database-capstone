/* doctorCard.js */

// Imports
// Adjust paths based on your actual file structure
import { showBookingOverlay } from '../loggedPatient.js'; 
import { deleteDoctor } from '../services/doctorServices.js';
import { getPatientData } from '../services/patientServices.js';

/**
 * Creates and returns a DOM element for a single doctor card.
 * @param {Object} doctor - The doctor data object.
 * @returns {HTMLElement} - The constructed card element.
 */
export function createDoctorCard(doctor) {
    // 1. Create Main Container
    const card = document.createElement("div");
    card.classList.add("doctor-card");

    // 2. Get User Role
    const role = localStorage.getItem("userRole");

    // 3. Create Doctor Info Section
    const infoDiv = document.createElement("div");
    infoDiv.classList.add("doctor-info");

    // Name
    const name = document.createElement("h3");
    name.textContent = doctor.name;

    // Specialization
    const specialization = document.createElement("p");
    specialization.textContent = `Specialty: ${doctor.specialization}`;

    // Email
    const email = document.createElement("p");
    email.textContent = `Email: ${doctor.email}`;

    // Availability
    const availability = document.createElement("p");
    // Assuming availability is an array, join it. If string, display as is.
    const availText = Array.isArray(doctor.availability) ? doctor.availability.join(", ") : doctor.availability;
    availability.textContent = `Availability: ${availText}`;

    // Append Info
    infoDiv.appendChild(name);
    infoDiv.appendChild(specialization);
    infoDiv.appendChild(email);
    infoDiv.appendChild(availability);

    // 4. Create Action Buttons Container
    const actionsDiv = document.createElement("div");
    actionsDiv.classList.add("card-actions");

    // 5. Role-Based Buttons

    // === ADMIN ROLE ===
    if (role === "admin") {
        const removeBtn = document.createElement("button");
        removeBtn.textContent = "Delete";
        removeBtn.className = "delete-btn"; // Add class for styling if needed

        removeBtn.addEventListener("click", async () => {
            if (confirm(`Are you sure you want to delete Dr. ${doctor.name}?`)) {
                const token = localStorage.getItem("token");
                const success = await deleteDoctor(token, doctor.id); // Call API
                
                if (success) {
                    card.remove(); // Remove from DOM
                    alert("Doctor deleted successfully.");
                } else {
                    alert("Failed to delete doctor.");
                }
            }
        });
        actionsDiv.appendChild(removeBtn);
    } 
    
    // === GUEST PATIENT ROLE ===
    else if (role === "patient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.className = "book-btn";

        bookNow.addEventListener("click", () => {
            alert("Please log in or sign up to book an appointment.");
        });
        actionsDiv.appendChild(bookNow);
    } 
    
    // === LOGGED-IN PATIENT ROLE ===
    else if (role === "loggedPatient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.className = "book-btn";

        bookNow.addEventListener("click", async (e) => {
            const token = localStorage.getItem("token");
            if (!token) {
                alert("Session expired.");
                window.location.href = "/";
                return;
            }

            try {
                // Fetch patient details to pre-fill booking
                const patientData = await getPatientData(token);
                // Trigger the booking overlay (defined in loggedPatient.js)
                showBookingOverlay(e, doctor, patientData);
            } catch (error) {
                console.error("Error preparing booking:", error);
                alert("Could not load booking details.");
            }
        });
        actionsDiv.appendChild(bookNow);
    }

    // 6. Final Assembly
    card.appendChild(infoDiv);
    card.appendChild(actionsDiv);

    return card;
}