/*
  This script handles the admin dashboard functionality for managing doctors:
  - Loads all doctor cards
  - Filters doctors by name, time, or specialty
  - Adds a new doctor via modal form


  Attach a click listener to the "Add Doctor" button
  When clicked, it opens a modal form using openModal('addDoctor')


  When the DOM is fully loaded:
    - Call loadDoctorCards() to fetch and display all doctors


  Function: loadDoctorCards
  Purpose: Fetch all doctors and display them as cards

    Call getDoctors() from the service layer
    Clear the current content area
    For each doctor returned:
    - Create a doctor card using createDoctorCard()
    - Append it to the content div

    Handle any fetch errors by logging them


  Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
  On any input change, call filterDoctorsOnChange()


  Function: filterDoctorsOnChange
  Purpose: Filter doctors based on name, available time, and specialty

    Read values from the search bar and filters
    Normalize empty values to null
    Call filterDoctors(name, time, specialty) from the service

    If doctors are found:
    - Render them using createDoctorCard()
    If no doctors match the filter:
    - Show a message: "No doctors found with the given filters."

    Catch and display any errors with an alert


  Function: renderDoctorCards
  Purpose: A helper function to render a list of doctors passed to it

    Clear the content area
    Loop through the doctors and append each card to the content area


  Function: adminAddDoctor
  Purpose: Collect form data and add a new doctor to the system

    Collect input values from the modal form
    - Includes name, email, phone, password, specialty, and available times

    Retrieve the authentication token from localStorage
    - If no token is found, show an alert and stop execution

    Build a doctor object with the form values

    Call saveDoctor(doctor, token) from the service

    If save is successful:
    - Show a success message
    - Close the modal and reload the page

    If saving fails, show an error message
*/


/*
  adminDashboard.js
  This script handles the admin dashboard functionality for managing doctors.
*/

// Import necessary functions from services and components
import { getDoctors, saveDoctor, filterDoctors } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';
import { openModal } from '../components/modals.js';

// When the DOM is fully loaded
document.addEventListener('DOMContentLoaded', () => {

    // Attach a click listener to the "Add Doctor" button (located in the header)
    // We use a timeout or check specifically because the header is rendered dynamically
    const addDocBtn = document.getElementById('addDocBtn');
    if (addDocBtn) {
        // When clicked, it opens a modal form using openModal('addDoctor')
        addDocBtn.addEventListener('click', () => openModal('addDoctor'));
    } else {
        // Fallback: simpler event delegation if the header renders late
        document.body.addEventListener('click', (e) => {
            if (e.target && e.target.id === 'addDocBtn') {
                openModal('addDoctor');
            }
        });
    }

    // Call loadDoctorCards() to fetch and display all doctors
    loadDoctorCards();

    // Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
    const searchBar = document.getElementById('searchBar');
    const filterTime = document.getElementById('filterTime');
    const filterSpecialty = document.getElementById('filterSpecialty');

    if (searchBar) searchBar.addEventListener('input', filterDoctorsOnChange);
    if (filterTime) filterTime.addEventListener('change', filterDoctorsOnChange);
    if (filterSpecialty) filterSpecialty.addEventListener('change', filterDoctorsOnChange);
});

/* -----------------------------------------------------------
   Function: loadDoctorCards
   Purpose: Fetch all doctors and display them as cards
----------------------------------------------------------- */
async function loadDoctorCards() {
    try {
        // Call getDoctors() from the service layer
        const doctors = await getDoctors();
        
        // Render the fetched doctors
        renderDoctorCards(doctors);
    } catch (error) {
        // Handle any fetch errors by logging them
        console.error("Error loading doctors:", error);
    }
}

/* -----------------------------------------------------------
   Function: filterDoctorsOnChange
   Purpose: Filter doctors based on name, available time, and specialty
----------------------------------------------------------- */
async function filterDoctorsOnChange() {
    try {
        // Read values from the search bar and filters
        const name = document.getElementById('searchBar').value;
        const time = document.getElementById('filterTime').value;
        const specialty = document.getElementById('filterSpecialty').value;

        // Normalize empty values to null (or empty string depending on service requirement)
        // Calling filterDoctors(name, time, specialty) from the service
        const response = await filterDoctors(name, time, specialty);

        // The service might return { doctors: [...] } or just the array. Handling both:
        const doctors = response.doctors || response;

        // If doctors are found:
        if (doctors && doctors.length > 0) {
            // Render them using createDoctorCard() (via helper)
            renderDoctorCards(doctors);
        } else {
            // If no doctors match the filter, Show a message
            const content = document.getElementById('content');
            content.innerHTML = '<p class="no-records">No doctors found with the given filters.</p>';
        }
    } catch (error) {
        // Catch and display any errors with an alert
        console.error(error);
        alert("An error occurred while filtering.");
    }
}

/* -----------------------------------------------------------
   Function: renderDoctorCards
   Purpose: A helper function to render a list of doctors passed to it
----------------------------------------------------------- */
function renderDoctorCards(doctors) {
    // Clear the content area
    const content = document.getElementById('content');
    if (!content) return;
    
    content.innerHTML = ''; 

    // Loop through the doctors and append each card to the content area
    doctors.forEach(doctor => {
        const card = createDoctorCard(doctor);
        content.appendChild(card);
    });
}

/* -----------------------------------------------------------
   Function: adminAddDoctor
   Purpose: Collect form data and add a new doctor to the system
   Note: Exposed to window so it can be called from the Modal's "Save" button
----------------------------------------------------------- */
window.adminAddDoctor = async function() {
    // Collect input values from the modal form
    const name = document.getElementById('docName').value;
    const email = document.getElementById('docEmail').value;
    const phone = document.getElementById('docPhone').value;
    const password = document.getElementById('docPassword').value;
    const specialty = document.getElementById('docSpecialty').value;
    const timeSlot = document.getElementById('docTime').value; // Assuming single string input

    // Retrieve the authentication token from localStorage
    const token = localStorage.getItem('token');

    // If no token is found, show an alert and stop execution
    if (!token) {
        alert("Authorization failed. Please login as admin.");
        return;
    }

    // Build a doctor object with the form values
    // Note: Converting single time slot string to an array for the backend List<String>
    const doctor = {
        name,
        email,
        phone,
        password,
        specialty,
        availableTimes: [timeSlot] 
    };

    // Call saveDoctor(doctor, token) from the service
    const result = await saveDoctor(doctor, token);

    // If save is successful
    if (result.success) {
        // Show a success message
        alert("Doctor added successfully!");
        // Close the modal and reload the page
        location.reload(); 
    } else {
        // If saving fails, show an error message
        alert("Failed to add doctor: " + result.message);
    }
};