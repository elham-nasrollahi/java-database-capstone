/*
  Import getAllAppointments to fetch appointments from the backend
  Import createPatientRow to generate a table row for each patient appointment


  Get the table body where patient rows will be added
  Initialize selectedDate with today's date in 'YYYY-MM-DD' format
  Get the saved token from localStorage (used for authenticated API calls)
  Initialize patientName to null (used for filtering by name)


  Add an 'input' event listener to the search bar
  On each keystroke:
    - Trim and check the input value
    - If not empty, use it as the patientName for filtering
    - Else, reset patientName to "null" (as expected by backend)
    - Reload the appointments list with the updated filter


  Add a click listener to the "Today" button
  When clicked:
    - Set selectedDate to today's date
    - Update the date picker UI to match
    - Reload the appointments for today


  Add a change event listener to the date picker
  When the date changes:
    - Update selectedDate with the new value
    - Reload the appointments for that specific date


  Function: loadAppointments
  Purpose: Fetch and display appointments based on selected date and optional patient name

  Step 1: Call getAllAppointments with selectedDate, patientName, and token
  Step 2: Clear the table body content before rendering new rows

  Step 3: If no appointments are returned:
    - Display a message row: "No Appointments found for today."

  Step 4: If appointments exist:
    - Loop through each appointment and construct a 'patient' object with id, name, phone, and email
    - Call createPatientRow to generate a table row for the appointment
    - Append each row to the table body

  Step 5: Catch and handle any errors during fetch:
    - Show a message row: "Error loading appointments. Try again later."


  When the page is fully loaded (DOMContentLoaded):
    - Call renderContent() (assumes it sets up the UI layout)
    - Call loadAppointments() to display today's appointments by default
*/

/*
  doctorDashboard.js
  Manages the Doctor Dashboard logic: fetching appointments, filtering by date/name, 
  and rendering patient tables.
*/

// Import getAllAppointments to fetch appointments from the backend
import { getAllAppointments } from './services/appointmentRecordService.js';

// Import createPatientRow to generate a table row for each patient appointment
import { createPatientRow } from './components/patientRows.js';

// Get the table body where patient rows will be added
const tableBody = document.getElementById('patientTableBody');

// Initialize selectedDate with today's date in 'YYYY-MM-DD' format
let selectedDate = new Date().toISOString().split('T')[0];

// Get the saved token from localStorage (used for authenticated API calls)
const token = localStorage.getItem('token');

// Initialize patientName to null (used for filtering by name)
// Backend expects "null" string for empty searches based on typical service patterns
let patientName = "null";

// --------------------------------------------------------------------------
// Event Listeners
// --------------------------------------------------------------------------

// Add an 'input' event listener to the search bar
const searchBar = document.getElementById('searchBar');
if (searchBar) {
  searchBar.addEventListener('input', (e) => {
    // Trim and check the input value
    const value = e.target.value.trim();

    // If not empty, use it as the patientName; Else, reset to "null"
    patientName = value ? value : "null";

    // Reload the appointments list with the updated filter
    loadAppointments();
  });
}

// Add a click listener to the "Today" button
const todayButton = document.getElementById('todayButton');
if (todayButton) {
  todayButton.addEventListener('click', () => {
    // Set selectedDate to today's date
    const today = new Date().toISOString().split('T')[0];
    selectedDate = today;

    // Update the date picker UI to match
    const datePicker = document.getElementById('datePicker');
    if (datePicker) datePicker.value = today;

    // Reload the appointments for today
    loadAppointments();
  });
}

// Add a change event listener to the date picker
const datePicker = document.getElementById('datePicker');
if (datePicker) {
  // Set initial value to today
  datePicker.value = selectedDate;

  datePicker.addEventListener('change', (e) => {
    // Update selectedDate with the new value
    selectedDate = e.target.value;

    // Reload the appointments for that specific date
    loadAppointments();
  });
}

// --------------------------------------------------------------------------
// Core Functionality
// --------------------------------------------------------------------------

/*
  Function: loadAppointments
  Purpose: Fetch and display appointments based on selected date and optional patient name
*/
async function loadAppointments() {
  // Step 2: Clear the table body content before rendering new rows
  if (!tableBody) return;
  tableBody.innerHTML = '';

  try {
    // Step 1: Call getAllAppointments with selectedDate, patientName, and token
    const appointments = await getAllAppointments(selectedDate, patientName, token);

    // Step 3: If no appointments are returned
    if (!appointments || appointments.length === 0) {
      // Display a message row: "No Appointments found for today."
      tableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="noPatientRecord">No Appointments found for today.</td>
                </tr>`;
      return;
    }

    // Step 4: If appointments exist
    appointments.forEach(appt => {
      // Loop through each appointment and construct a 'patient' object
      // Note: The appointment object usually contains a nested 'patient' entity
      const patientData = {
        id: appt.patient.id,
        name: appt.patient.name,
        phone: appt.patient.phone,
        email: appt.patient.email,
        // Passing appointment ID is crucial for prescriptions
        appointmentId: appt.id
      };

      // Call createPatientRow to generate a table row for the appointment
      const row = createPatientRow(patientData);

      // Append each row to the table body
      tableBody.appendChild(row);
    });

  } catch (error) {
    // Step 5: Catch and handle any errors during fetch
    console.error("Error loading appointments:", error);
    // Show a message row: "Error loading appointments. Try again later."
    tableBody.innerHTML = `
            <tr>
                <td colspan="5" class="error-message">Error loading appointments. Try again later.</td>
            </tr>`;
  }
}

// When the page is fully loaded (DOMContentLoaded)
document.addEventListener('DOMContentLoaded', () => {
  // Call renderContent() (assumes it sets up the UI layout - typically global)
  if (typeof window.renderContent === 'function') {
    window.renderContent();
  }

  // Call loadAppointments() to display today's appointments by default
  loadAppointments();
});