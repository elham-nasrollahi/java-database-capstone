/* adminDashboard.js */

// 1. Import Required Modules
import { openModal } from './components/modals.js';
import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';

// 2. Event Binding on Page Load
document.addEventListener("DOMContentLoaded", () => {
    
    // Load all doctors initially
    loadDoctorCards();

    // Bind "Add Doctor" button to open modal
    const addDocBtn = document.getElementById('addDocBtn');
    if (addDocBtn) {
        addDocBtn.addEventListener('click', () => {
            openModal('addDoctor');
        });
    }

    // Bind Search and Filter Events
    const searchBar = document.getElementById("searchBar");
    const filterTime = document.getElementById("filterTime");
    const filterSpecialty = document.getElementById("filterSpecialty");

    if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
    if (filterTime) filterTime.addEventListener("change", filterDoctorsOnChange);
    if (filterSpecialty) filterSpecialty.addEventListener("change", filterDoctorsOnChange);
});

/**
 * Function: loadDoctorCards
 * Purpose: Fetch all doctors and display them
 */
async function loadDoctorCards() {
    const contentDiv = document.getElementById("content");
    contentDiv.innerHTML = "<p>Loading doctors...</p>"; // Temporary loading state

    const doctors = await getDoctors();

    renderDoctorCards(doctors);
}

/**
 * Function: filterDoctorsOnChange
 * Purpose: Handle search and filter inputs
 */
async function filterDoctorsOnChange() {
    const nameInput = document.getElementById("searchBar").value.trim();
    const timeInput = document.getElementById("filterTime").value;
    const specialtyInput = document.getElementById("filterSpecialty").value;

    // Normalize values (pass null/empty string based on what service expects)
    // Assuming service expects null or specific strings for empty filters
    const name = nameInput === "" ? null : nameInput;
    const time = timeInput === "" ? null : timeInput;
    const specialty = specialtyInput === "" ? null : specialtyInput;

    const doctors = await filterDoctors(name, time, specialty);

    renderDoctorCards(doctors);
}

/**
 * Function: renderDoctorCards
 * Purpose: Utility to render list to DOM
 */
function renderDoctorCards(doctors) {
    const contentDiv = document.getElementById("content");
    contentDiv.innerHTML = ""; // Clear existing

    if (!doctors || doctors.length === 0) {
        contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
        return;
    }

    doctors.forEach(doctor => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
    });
}

/**
 * Function: adminAddDoctor
 * Purpose: Handle form submission to add a new doctor
 * Attached to window to be accessible via HTML onclick attributes
 */
window.adminAddDoctor = async function() {
    // 1. Collect Form Data
    const name = document.getElementById('docName').value;
    const email = document.getElementById('docEmail').value;
    const password = document.getElementById('docPassword').value; // Ensure ID matches HTML
    const specialty = document.getElementById('docSpecialty').value;
    const mobileNo = document.getElementById('docPhone').value;
    
    // Collect Checkbox values for availability
    // Assuming checkboxes have name="availability" or similar common selector
    // If your HTML uses a select-multiple, logic changes. Assuming checkboxes here:
    const availabilityCheckboxes = document.querySelectorAll('input[name="availability"]:checked');
    const availability = Array.from(availabilityCheckboxes).map(cb => cb.value);

    // 2. Auth Check
    const token = localStorage.getItem("token");
    if (!token) {
        alert("Session expired. Please login again.");
        return;
    }

    // 3. Build Object
    const doctorData = {
        name,
        email,
        password,
        specialization: specialty, // Ensure key matches backend DTO
        mobileNo,
        availability
    };

    // 4. Send Request
    const result = await saveDoctor(doctorData, token);

    // 5. Handle Result
    if (result.success) {
        alert("Doctor added successfully!");
        // Close modal (assuming a global closeModal or finding the close button)
        const modal = document.getElementById("modal");
        if(modal) modal.style.display = "none";
        
        // Refresh list
        loadDoctorCards();
        
        // Optional: Reset form
        // document.getElementById("addDoctorForm").reset();
    } else {
        alert("Failed to add doctor: " + result.message);
    }
};