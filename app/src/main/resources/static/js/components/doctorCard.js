/*
Import the overlay function for booking appointments from loggedPatient.js

  Import the deleteDoctor API function to remove doctors (admin role) from docotrServices.js

  Import function to fetch patient details (used during booking) from patientServices.js

  Function to create and return a DOM element for a single doctor card
    Create the main container for the doctor card
    Retrieve the current user role from localStorage
    Create a div to hold doctor information
    Create and set the doctor’s name
    Create and set the doctor's specialization
    Create and set the doctor's email
    Create and list available appointment times
    Append all info elements to the doctor info container
    Create a container for card action buttons
    === ADMIN ROLE ACTIONS ===
      Create a delete button
      Add click handler for delete button
     Get the admin token from localStorage
        Call API to delete the doctor
        Show result and remove card if successful
      Add delete button to actions container
   
    === PATIENT (NOT LOGGED-IN) ROLE ACTIONS ===
      Create a book now button
      Alert patient to log in before booking
      Add button to actions container
  
    === LOGGED-IN PATIENT ROLE ACTIONS === 
      Create a book now button
      Handle booking logic for logged-in patient   
        Redirect if token not available
        Fetch patient data with token
        Show booking overlay UI with doctor and patient info
      Add button to actions container
   
  Append doctor info and action buttons to the car
  Return the complete doctor card element
*/


/*
  doctorCard.js
  Function to create and return a DOM element for a single doctor card.
*/

// Import dependencies as specified in the instructions
import { showBookingOverlay } from '../js/loggedPatient.js'; // Adjust path as needed
import { deleteDoctor } from '../services/doctorServices.js';
import { getPatientData } from '../services/patientServices.js';

export function createDoctorCard(doctor) {
    
    // 1. Create the main container for the doctor card
    const card = document.createElement('div');
    card.classList.add("doctor-card");
    card.setAttribute('data-id', doctor.id); // Useful for DOM manipulation

    // 2. Retrieve the current user role from localStorage
    const role = localStorage.getItem('userRole');

    // 3. Create a div to hold doctor information
    const infoDiv = document.createElement('div');
    infoDiv.classList.add("doctor-info");

    // 4. Create and set the doctor details
    const name = document.createElement('h3');
    name.textContent = doctor.name;

    const specialty = document.createElement('p');
    specialty.innerHTML = `<strong>Specialty:</strong> ${doctor.specialty}`;

    const email = document.createElement('p');
    email.innerHTML = `<strong>Email:</strong> ${doctor.email}`;

    // 5. Create and list available appointment times
    const times = document.createElement('p');
    // Assuming availableTimes is an array of strings, join them nicely
    const timesList = doctor.availableTimes ? doctor.availableTimes.join(', ') : 'No times available';
    times.innerHTML = `<strong>Available:</strong> ${timesList}`;

    // 6. Append all info elements to the doctor info container
    infoDiv.appendChild(name);
    infoDiv.appendChild(specialty);
    infoDiv.appendChild(email);
    infoDiv.appendChild(times);

    // 7. Create a container for card action buttons
    const actionsDiv = document.createElement('div');
    actionsDiv.className = 'card-actions';

    /* === ADMIN ROLE ACTIONS === 
    */
    if (role === 'admin') {
        // Create a delete button
        const deleteBtn = document.createElement('button');
        deleteBtn.textContent = 'Delete';
        
        // Add click handler for delete button
        deleteBtn.onclick = async () => {
            if (confirm(`Are you sure you want to delete Dr. ${doctor.name}?`)) {
                // Get the admin token
                const token = localStorage.getItem('token');
                
                // Call API to delete the doctor
                const success = await deleteDoctor(doctor.id, token);
                
                // Show result and remove card if successful
                if (success) {
                    alert('Doctor deleted successfully.');
                    card.remove(); // Remove element from DOM immediately
                } else {
                    alert('Failed to delete doctor.');
                }
            }
        };
        // Add delete button to actions container
        actionsDiv.appendChild(deleteBtn);
    }

    /* === PATIENT (NOT LOGGED-IN) ROLE ACTIONS === 
    */
    else if (role === 'patient') {
        // Create a book now button
        const bookBtn = document.createElement('button');
        bookBtn.textContent = 'Book Now';
        
        // Alert patient to log in before booking
        bookBtn.onclick = () => {
            alert('Please login to book an appointment.');
            // Optional: Redirect to login modal or page
        };
        
        actionsDiv.appendChild(bookBtn);
    }

    /* === LOGGED-IN PATIENT ROLE ACTIONS === 
    */
    else if (role === 'loggedPatient') {
        // Create a book now button
        const bookBtn = document.createElement('button');
        bookBtn.textContent = 'Book Now';
        bookBtn.className = 'book-btn';
        
        // Handle booking logic for logged-in patient
        bookBtn.onclick = async () => {
            const token = localStorage.getItem('token');
            
            // Redirect if token not available
            if (!token) {
                alert("Session expired. Please log in again.");
                window.location.href = "../index.html"; 
                return;
            }

            try {
                // Fetch patient data with token
                const patient = await getPatientData(token);
                showBookingOverlay(doctor, patient);
            } catch (error) {
                console.error("Error initiating booking:", error);
                alert("Could not load booking details.");
            }
        };
        
        actionsDiv.appendChild(bookBtn);
    }

    // 8. Append doctor info and action buttons to the card
    card.appendChild(infoDiv);
    card.appendChild(actionsDiv);

    // 9. Return the complete doctor card element
    return card;
}