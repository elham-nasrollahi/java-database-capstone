/*
  Import the openModal function to handle showing login popups/modals
  Import the base API URL from the config file
  Define constants for the admin and doctor login API endpoints using the base URL

  Use the window.onload event to ensure DOM elements are available after page load
  Inside this function:
    - Select the "adminLogin" and "doctorLogin" buttons using getElementById
    - If the admin login button exists:
        - Add a click event listener that calls openModal('adminLogin') to show the admin login modal
    - If the doctor login button exists:
        - Add a click event listener that calls openModal('doctorLogin') to show the doctor login modal


  Define a function named adminLoginHandler on the global window object
  This function will be triggered when the admin submits their login credentials

  Step 1: Get the entered username and password from the input fields
  Step 2: Create an admin object with these credentials

  Step 3: Use fetch() to send a POST request to the ADMIN_API endpoint
    - Set method to POST
    - Add headers with 'Content-Type: application/json'
    - Convert the admin object to JSON and send in the body

  Step 4: If the response is successful:
    - Parse the JSON response to get the token
    - Store the token in localStorage
    - Call selectRole('admin') to proceed with admin-specific behavior

  Step 5: If login fails or credentials are invalid:
    - Show an alert with an error message

  Step 6: Wrap everything in a try-catch to handle network or server errors
    - Show a generic error message if something goes wrong


  Define a function named doctorLoginHandler on the global window object
  This function will be triggered when a doctor submits their login credentials

  Step 1: Get the entered email and password from the input fields
  Step 2: Create a doctor object with these credentials

  Step 3: Use fetch() to send a POST request to the DOCTOR_API endpoint
    - Include headers and request body similar to admin login

  Step 4: If login is successful:
    - Parse the JSON response to get the token
    - Store the token in localStorage
    - Call selectRole('doctor') to proceed with doctor-specific behavior

  Step 5: If login fails:
    - Show an alert for invalid credentials

  Step 6: Wrap in a try-catch block to handle errors gracefully
    - Log the error to the console
    - Show a generic error message
*/




/*
  index.js
  Handles the logic for the landing page, including role selection 
  and login handlers for Admin and Doctor.
*/

// Import the openModal function to handle showing login popups/modals
import { openModal } from '../components/modals.js';

// Import the base API URL from the config file
// Assuming a config.js exists, otherwise we define the base URL here
import { BASE_URL } from '../config/config.js';

// Define constants for the admin and doctor login API endpoints
const ADMIN_API = `${BASE_URL}/admin`;
const DOCTOR_API = `${BASE_URL}/doctor/login`;

// Use the window.onload event to ensure DOM elements are available after page load
window.onload = function () {

    // Select the buttons for role selection
    // Note: IDs match those generated in index.html (btn-admin, btn-doctor)
    const adminBtn = document.getElementById("btn-admin");
    const doctorBtn = document.getElementById("btn-doctor");
    const patientBtn = document.getElementById("btn-patient");

    // If the admin login button exists, add click listener
    if (adminBtn) {
        adminBtn.addEventListener("click", () => {
            selectRole('admin'); // Set role first
            openModal('adminLogin'); // Show admin login modal
        });
    }

    // If the doctor login button exists, add click listener
    if (doctorBtn) {
        doctorBtn.addEventListener("click", () => {
            selectRole('doctor'); // Set role first
            openModal('doctorLogin'); // Show doctor login modal
        });
    }

    // Patient button handler (implied for completeness)
    if (patientBtn) {
        patientBtn.addEventListener("click", () => {
            selectRole('patient'); // Set role first
            // Patients usually have a separate flow or login/signup choice
            openModal('patientLogin');
        });
    }
};

/* -----------------------------------------------------------
   Admin Login Handler
----------------------------------------------------------- */

// Define a function named adminLoginHandler on the global window object
window.adminLoginHandler = async function () {

    // Step 1: Get the entered username and password from the input fields
    const usernameInput = document.getElementById("adminUsername");
    const passwordInput = document.getElementById("adminPassword");

    if (!usernameInput || !passwordInput) {
        console.error("Admin input fields not found");
        return;
    }

    const username = usernameInput.value;
    const password = passwordInput.value;

    // Step 2: Create an admin object with these credentials
    const adminData = { username, password };

    try {
        // Step 3: Use fetch() to send a POST request to the ADMIN_API endpoint
        const response = await fetch(ADMIN_API, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(adminData)
        });

        // Step 4: If the response is successful
        if (response.ok) {
            // Parse the JSON response to get the token
            const data = await response.json();

            // Store the token and role in localStorage
            // Note: Storing 'userRole' is essential for header.js to work correctly
            localStorage.setItem("token", data.token);
            localStorage.setItem("userRole", "admin");

            // Proceed to admin dashboard
            alert("Login Successful!");
            window.location.href = "pages/adminDashboard.html";
        } else {
            // Step 5: If login fails or credentials are invalid
            alert("Invalid Admin credentials. Please try again.");
        }
    } catch (error) {
        // Step 6: Wrap everything in a try-catch to handle network or server errors
        console.error("Admin Login Error:", error);
        alert("An error occurred during login. Please try again later.");
    }
};

/* -----------------------------------------------------------
   Doctor Login Handler
----------------------------------------------------------- */

// Define a function named doctorLoginHandler on the global window object
window.doctorLoginHandler = async function () {

    // Step 1: Get the entered email and password from the input fields
    const emailInput = document.getElementById("doctorEmail");
    const passwordInput = document.getElementById("doctorPassword");

    if (!emailInput || !passwordInput) {
        console.error("Doctor input fields not found");
        return;
    }

    const email = emailInput.value;
    const password = passwordInput.value;

    // Step 2: Create a doctor object with these credentials
    const doctorData = { email, password };

    try {
        // Step 3: Use fetch() to send a POST request to the DOCTOR_API endpoint
        const response = await fetch(DOCTOR_API, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(doctorData)
        });

        // Step 4: If login is successful
        if (response.ok) {
            // Parse the JSON response to get the token
            const data = await response.json();

            // Store the token and role in localStorage
            localStorage.setItem("token", data.token);
            localStorage.setItem("userRole", "doctor");

            // Proceed to doctor dashboard
            alert("Login Successful!");
            window.location.href = "pages/doctorDashboard.html";
        } else {
            // Step 5: If login fails, show alert
            alert("Invalid Doctor credentials.");
        }
    } catch (error) {
        // Step 6: Log the error and show generic message
        console.error("Doctor Login Error:", error);
        alert("System error. Please contact support.");
    }
};