/* header.js */

/**
 * Renders the header dynamically based on the user's role and login status.
 */
function renderHeader() {
    const headerDiv = document.getElementById("header");
    
    // 1. Root Path Check: Clear session if on the homepage
    if (window.location.pathname.endsWith("/") || window.location.pathname.endsWith("index.html")) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
        
        // Render basic header for homepage
        headerDiv.innerHTML = `
            <header class="header">
                <div class="logo-section">
                    <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
                    <span class="logo-title">Hospital CMS</span>
                </div>
            </header>`;
        return;
    }

    // 2. Get User Role and Token
    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");

    // 3. Session Validation: If role requires auth but no token, force logout
    if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
        localStorage.removeItem("userRole");
        alert("Session expired or invalid login. Please log in again.");
        window.location.href = "../index.html"; 
        return;
    }

    // 4. Initialize Header HTML
    let headerContent = `
        <header class="header">
            <div class="logo-section">
                <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
                <span class="logo-title">Hospital CMS</span>
            </div>
            <nav class="nav-links">`;

    // 5. Build Role-Based Navigation
    if (role === "admin") {
        headerContent += `
            <button id="addDocBtn" class="adminBtn">Add Doctor</button>
            <a href="#" id="logoutBtn">Logout</a>`;
    } 
    else if (role === "doctor") {
        headerContent += `
            <button id="doctorHomeBtn" class="adminBtn">Home</button>
            <a href="#" id="logoutBtn">Logout</a>`;
    } 
    else if (role === "patient") { // Guest patient
        headerContent += `
            <button id="patientLogin" class="adminBtn">Login</button>
            <button id="patientSignup" class="adminBtn">Sign Up</button>`;
    } 
    else if (role === "loggedPatient") {
        headerContent += `
            <button id="patientHome" class="adminBtn">Home</button>
            <button id="patientAppointments" class="adminBtn">Appointments</button>
            <a href="#" id="logoutPatientBtn">Logout</a>`;
    }

    // Close tags
    headerContent += `</nav></header>`;

    // 6. Inject HTML
    headerDiv.innerHTML = headerContent;

    // 7. Attach Event Listeners
    attachHeaderButtonListeners(role);
}

/**
 * Attaches event listeners to the dynamic header elements.
 */
function attachHeaderButtonListeners(role) {
    // Admin Actions
    if (role === "admin") {
        document.getElementById("addDocBtn")?.addEventListener("click", () => openModal('addDoctor')); // Assumes openModal is global or imported
        document.getElementById("logoutBtn")?.addEventListener("click", logout);
    }

    // Doctor Actions
    if (role === "doctor") {
        document.getElementById("doctorHomeBtn")?.addEventListener("click", () => window.location.reload());
        document.getElementById("logoutBtn")?.addEventListener("click", logout);
    }

    // Guest Patient Actions
    if (role === "patient") {
        document.getElementById("patientLogin")?.addEventListener("click", () => openModal('login')); 
        document.getElementById("patientSignup")?.addEventListener("click", () => openModal('signup'));
    }

    // Logged Patient Actions
    if (role === "loggedPatient") {
        document.getElementById("patientHome")?.addEventListener("click", () => window.location.href = '/pages/loggedPatientDashboard.html');
        document.getElementById("patientAppointments")?.addEventListener("click", () => window.location.href = '/pages/patientAppointments.html');
        document.getElementById("logoutPatientBtn")?.addEventListener("click", logoutPatient);
    }
}

/**
 * General Logout: Clears all data and redirects to landing page.
 */
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    window.location.href = "../index.html";
}

/**
 * Patient Logout: Clears token but resets role to guest 'patient' to see public view.
 */
function logoutPatient() {
    localStorage.removeItem("token");
    localStorage.setItem("userRole", "patient");
    window.location.href = "/pages/patientDashboard.html";
}

// Initialize
renderHeader();