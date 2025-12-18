/* index.js 
  Handles role selection, modal triggers, and authentication logic for Admin and Doctor.
*/

// 1. Import Required Modules
import { openModal } from '../components/modals.js';
import { API_BASE_URL } from '../config/config.js';

// 2. Define API Endpoints
const ADMIN_API = API_BASE_URL + '/admin';
const DOCTOR_API = API_BASE_URL + '/doctor/login';

// 3. Setup Button Event Listeners on Page Load
window.onload = function () {
    
    // Select the buttons that should trigger the login modals
    // Note: Ensure your HTML buttons have these specific IDs
    const adminBtn = document.getElementById('adminLogin'); // or 'adminRoleBtn' if matching index.html
    const doctorBtn = document.getElementById('doctorLogin'); // or 'doctorRoleBtn'

    // Attach listener for Admin
    if (adminBtn) {
        adminBtn.addEventListener('click', () => {
            openModal('adminLogin'); // Opens the Admin Login Modal
        });
    }

    // Attach listener for Doctor
    if (doctorBtn) {
        doctorBtn.addEventListener('click', () => {
            openModal('doctorLogin'); // Opens the Doctor Login Modal
        });
    }
};

/**
 * 4. Implement Admin Login Handler
 * This function is attached to window so it can be called from the HTML onclick attribute.
 */
window.adminLoginHandler = async function () {
    // Step 1: Get entered credentials
    // Note: IDs 'username' and 'password' should exist in your admin modal form
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    // Step 2: Create admin object
    const admin = { username, password };

    try {
        // Step 3: Send POST request
        const response = await fetch(ADMIN_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(admin)
        });

        // Step 4: Handle Response
        if (response.ok) {
            const data = await response.json();
            
            // Store token
            if (data) {
                 // Assuming the API returns the token directly or in an object
                 localStorage.setItem('token', JSON.stringify(data)); 
            }

            // Set Role and Redirect
            // selectRole is a helper from render.js (assumed globally available)
            // If selectRole is not global, we manually set the item:
            localStorage.setItem('userRole', 'admin'); 
            
            if (typeof selectRole === 'function') {
                selectRole('admin');
            }

            alert("Login Successful!");
            window.location.href = "/templates/admin/adminDashboard.html"; // Redirect
        } else {
            // Step 5: Handle Login Failure
            alert("Invalid credentials!");
        }
    } catch (error) {
        // Step 6: Handle Network/Server Errors
        console.error("Admin login error:", error);
        alert("Login failed. Please check the server connection.");
    }
};

/**
 * 5. Implement Doctor Login Handler
 */
window.doctorLoginHandler = async function () {
    // Step 1: Get entered credentials
    // Note: IDs 'docEmail' and 'docPassword' should exist in your doctor modal form
    const email = document.getElementById('docEmail').value;
    const password = document.getElementById('docPassword').value;

    // Step 2: Create doctor object
    const doctor = { email, password };

    try {
        // Step 3: Send POST request
        const response = await fetch(DOCTOR_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(doctor)
        });

        // Step 4: Handle Response
        if (response.ok) {
            const data = await response.json();
            
            // Store token
            if (data) {
                localStorage.setItem('token', JSON.stringify(data));
            }

            // Set Role and Redirect
            localStorage.setItem('userRole', 'doctor');

            if (typeof selectRole === 'function') {
                selectRole('doctor');
            }

            alert("Login Successful!");
            window.location.href = "/templates/doctor/doctorDashboard.html"; // Redirect
        } else {
            // Step 5: Handle Failure
            alert("Invalid credentials!");
        }
    } catch (error) {
        // Step 6: Handle Errors
        console.error("Doctor login error:", error);
        alert("Login failed. Please check the server connection.");
    }
};

/* Global Function for Patient Role Selection (Guest)
  This handles the "Patient" button which doesn't require immediate login
*/
window.selectRole = function(role) {
    localStorage.setItem('userRole', role);
    if (role === 'patient') {
        window.location.href = '/pages/patientDashboard.html';
    }
    // Admin and Doctor roles usually handled after login, 
    // but this sets the state for the header renderer.
}