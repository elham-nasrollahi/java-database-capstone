/* doctorDashboard.js */

// 1. Import Required Modules
import { getAllAppointments } from './services/appointmentRecordService.js'; // Adjust path if needed
import { createPatientRow } from './components/patientRows.js'; // Adjust path if needed

// 2. Global Variables
const patientTableBody = document.getElementById("patientTableBody");
let selectedDate = new Date().toISOString().split('T')[0]; // Today's date YYYY-MM-DD
const token = localStorage.getItem("token");
let patientName = "null"; // Initialize as string "null" for backend compatibility

// 3. Page Load Initialization
document.addEventListener("DOMContentLoaded", () => {
    // Call global renderContent if it exists (from render.js)
    if (typeof renderContent === 'function') {
        renderContent();
    }

    // Set initial date picker value
    const datePicker = document.getElementById("datePicker");
    if(datePicker) datePicker.value = selectedDate;

    // Load initial data
    loadAppointments();

    // Bind Search Bar
    const searchBar = document.getElementById("searchBar");
    if (searchBar) {
        searchBar.addEventListener("input", (e) => {
            const val = e.target.value.trim();
            patientName = val === "" ? "null" : val;
            loadAppointments();
        });
    }

    // Bind "Today" Button
    const todayButton = document.getElementById("todayButton");
    if (todayButton) {
        todayButton.addEventListener("click", () => {
            selectedDate = new Date().toISOString().split('T')[0];
            if(datePicker) datePicker.value = selectedDate;
            loadAppointments();
        });
    }

    // Bind Date Picker
    if (datePicker) {
        datePicker.addEventListener("change", (e) => {
            if(e.target.value) {
                selectedDate = e.target.value;
                loadAppointments();
            }
        });
    }
});

/**
 * Function: loadAppointments
 * Purpose: Fetch and display appointments based on filters
 */
async function loadAppointments() {
    if (!token) {
        patientTableBody.innerHTML = "<tr><td colspan='5'>Please login to view appointments.</td></tr>";
        return;
    }

    // Clear current content
    patientTableBody.innerHTML = "<tr><td colspan='5'>Loading...</td></tr>";

    try {
        // Step 1: Call Service
        // getAllAppointments signature based on previous context: (date, name, token)
        const appointments = await getAllAppointments(selectedDate, patientName, token);

        patientTableBody.innerHTML = ""; // Clear loading message

        // Step 3: Handle Empty Result
        if (!appointments || appointments.length === 0) {
            patientTableBody.innerHTML = "<tr><td colspan='5' class='noPatientRecord'>No Appointments found for today.</td></tr>";
            return;
        }

        // Step 4: Render Rows
        appointments.forEach(appointment => {
            // Assuming appointment object has a nested patient object
            // If the API returns a flat structure, adjust accordingly
            const patientObj = appointment.patient || appointment; 
            
            const row = createPatientRow({
                id: patientObj.id,
                name: patientObj.name,
                mobileNo: patientObj.mobileNo,
                email: patientObj.email,
                // Pass prescription/appointment specific data if needed for the row actions
                appointmentId: appointment.appointmentId 
            });
            
            patientTableBody.appendChild(row);
        });

    } catch (error) {
        console.error("Error loading appointments:", error);
        patientTableBody.innerHTML = "<tr><td colspan='5' style='color:red;'>Error loading appointments. Try again later.</td></tr>";
    }
}