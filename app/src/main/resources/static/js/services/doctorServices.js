/* doctorServices.js */

// 1. Import API Base URL
import { API_BASE_URL } from "../config/config.js";

// 2. Set Doctor API Endpoint
const DOCTOR_API = API_BASE_URL + '/doctor';

/**
 * Function: getDoctors
 * Purpose: Fetch the list of all doctors from the API
 */
export async function getDoctors() {
    try {
        // Send GET request
        const response = await fetch(DOCTOR_API);
        
        // Convert response to JSON
        const data = await response.json();
        
        // Return the doctors list
        // Assuming API returns { doctors: [...] } or just [...]
        // Adjust logic if API returns directly an array or nested object
        return data.doctors || data; 
    } catch (error) {
        // Log error and return empty array
        console.error("Error fetching doctors:", error);
        return [];
    }
}

/**
 * Function: deleteDoctor
 * Purpose: Delete a specific doctor using their ID and an authentication token
 */
export async function deleteDoctor(id, token) {
    try {
        // Construct URL with ID and Token as path parameters
        // Example: /doctor/delete/123/tokenABC
        const url = `${DOCTOR_API}/delete/${id}/${token}`;

        // Send DELETE request
        const response = await fetch(url, {
            method: 'DELETE'
        });

        // Convert to JSON
        const data = await response.json();

        // Return structured response
        return {
            success: true,
            message: data.message || "Doctor deleted successfully"
        };
    } catch (error) {
        console.error("Error deleting doctor:", error);
        return {
            success: false,
            message: "Failed to delete doctor due to a network or server error."
        };
    }
}

/**
 * Function: saveDoctor
 * Purpose: Save (create) a new doctor using a POST request
 */
export async function saveDoctor(doctor, token) {
    try {
        // URL includes token
        const url = `${DOCTOR_API}/save/${token}`;

        // Send POST request
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(doctor)
        });

        // Parse response
        const data = await response.json();

        return {
            success: true, // Assuming API returns 200 OK for success
            message: data.message || "Doctor saved successfully"
        };
    } catch (error) {
        console.error("Error saving doctor:", error);
        return {
            success: false,
            message: "Failed to save doctor details."
        };
    }
}

/**
 * Function: filterDoctors
 * Purpose: Fetch doctors based on filtering criteria (name, time, and specialty)
 */
export async function filterDoctors(name, time, specialty) {
    try {
        // Construct URL with path parameters
        // IMPORTANT: Ensure backend accepts these specific path params in this order
        // Use placeholders or empty strings if a filter is missing/null
        const searchName = name || "empty";
        const searchTime = time || "empty";
        const searchSpecialty = specialty || "empty";

        const url = `${DOCTOR_API}/search/${searchName}/${searchTime}/${searchSpecialty}`;

        // Send GET request
        const response = await fetch(url);

        if (response.ok) {
            const data = await response.json();
            return data.doctors || data;
        } else {
            console.error("Failed to filter doctors. Status:", response.status);
            return { doctors: [] };
        }
    } catch (error) {
        console.error("Error filtering doctors:", error);
        alert("An error occurred while filtering doctors.");
        return { doctors: [] };
    }
}