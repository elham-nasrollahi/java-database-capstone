/*
  Import the base API URL from the config file
  Define a constant DOCTOR_API to hold the full endpoint for doctor-related actions


  Function: getDoctors
  Purpose: Fetch the list of all doctors from the API

   Use fetch() to send a GET request to the DOCTOR_API endpoint
   Convert the response to JSON
   Return the 'doctors' array from the response
   If there's an error (e.g., network issue), log it and return an empty array


  Function: deleteDoctor
  Purpose: Delete a specific doctor using their ID and an authentication token

   Use fetch() with the DELETE method
    - The URL includes the doctor ID and token as path parameters
   Convert the response to JSON
   Return an object with:
    - success: true if deletion was successful
    - message: message from the server
   If an error occurs, log it and return a default failure response


  Function: saveDoctor
  Purpose: Save (create) a new doctor using a POST request

   Use fetch() with the POST method
    - URL includes the token in the path
    - Set headers to specify JSON content type
    - Convert the doctor object to JSON in the request body

   Parse the JSON response and return:
    - success: whether the request succeeded
    - message: from the server

   Catch and log errors
    - Return a failure response if an error occurs


  Function: filterDoctors
  Purpose: Fetch doctors based on filtering criteria (name, time, and specialty)

   Use fetch() with the GET method
    - Include the name, time, and specialty as URL path parameters
   Check if the response is OK
    - If yes, parse and return the doctor data
    - If no, log the error and return an object with an empty 'doctors' array

   Catch any other errors, alert the user, and return a default empty result
*/


/*
  doctorServices.js
  Handles API interactions for doctor-related operations.
*/

// Import the base API URL from the config file
import { BASE_URL } from '../config/config.js';

// Define a constant DOCTOR_API to hold the full endpoint for doctor-related actions
const DOCTOR_API = `${BASE_URL}/doctor`;

/* -----------------------------------------------------------
   Function: getDoctors
   Purpose: Fetch the list of all doctors from the API
----------------------------------------------------------- */
export async function getDoctors() {
    try {
        // Use fetch() to send a GET request to the DOCTOR_API endpoint
        const response = await fetch(DOCTOR_API);
        
        // Convert the response to JSON
        const data = await response.json();
        
        // Return the 'doctors' array from the response
        // Guarding against undefined data.doctors just in case
        return data.doctors || []; 
    } catch (error) {
        // If there's an error, log it and return an empty array
        console.error("Error fetching doctors:", error);
        return [];
    }
}

/* -----------------------------------------------------------
   Function: deleteDoctor
   Purpose: Delete a specific doctor using their ID and authentication token
----------------------------------------------------------- */
export async function deleteDoctor(id, token) {
    try {
        // Use fetch() with the DELETE method
        // The URL includes the doctor ID and token as path parameters
        const response = await fetch(`${DOCTOR_API}/${id}/${token}`, {
            method: 'DELETE'
        });

        // Convert the response to JSON
        const data = await response.json();

        // Return an object with success status and message
        return {
            success: response.ok, // Assuming 200 OK means success
            message: data.message
        };

    } catch (error) {
        // If an error occurs, log it and return a default failure response
        console.error("Error deleting doctor:", error);
        return { success: false, message: "Network error occurred." };
    }
}

/* -----------------------------------------------------------
   Function: saveDoctor
   Purpose: Save (create) a new doctor using a POST request
----------------------------------------------------------- */
export async function saveDoctor(doctor, token) {
    try {
        // Use fetch() with the POST method
        // URL includes the token in the path
        const response = await fetch(`${DOCTOR_API}/${token}`, {
            method: 'POST',
            // Set headers to specify JSON content type
            headers: {
                'Content-Type': 'application/json'
            },
            // Convert the doctor object to JSON in the request body
            body: JSON.stringify(doctor)
        });

        // Parse the JSON response
        const data = await response.json();

        // Return whether the request succeeded and the message
        return {
            success: response.ok,
            message: data.message
        };

    } catch (error) {
        // Catch and log errors, returning a failure response
        console.error("Error saving doctor:", error);
        return { success: false, message: "Error saving doctor." };
    }
}

/* -----------------------------------------------------------
   Function: filterDoctors
   Purpose: Fetch doctors based on filtering criteria
----------------------------------------------------------- */
export async function filterDoctors(name, time, specialty) {
    try {
        // Handle empty values to prevent malformed URLs if strictly using path params
        // Assuming the backend expects "null" or specific placeholders if fields are empty
        const safeName = name || "null";
        const safeTime = time || "null";
        const safeSpecialty = specialty || "null";

        // Use fetch() with the GET method
        // Include the name, time, and specialty as URL path parameters
        const response = await fetch(`${DOCTOR_API}/search/${safeName}/${safeTime}/${safeSpecialty}`);

        // Check if the response is OK
        if (response.ok) {
            // If yes, parse and return the doctor data
            const data = await response.json();
            return data;
        } else {
            // If no, log the error and return an object with an empty 'doctors' array
            console.error("Error filtering doctors: Status", response.status);
            return { doctors: [] };
        }

    } catch (error) {
        // Catch any other errors, alert the user, and return a default empty result
        console.error("Network error during filtering:", error);
        alert("An error occurred while filtering doctors.");
        return { doctors: [] };
    }
}