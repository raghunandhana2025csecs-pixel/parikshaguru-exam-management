const express = require('express');
const path = require('path');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;
const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Serve a dynamic config script that injects API_BASE_URL into the browser client
app.get('/config.js', (req, res) => {
    res.type('application/javascript');
    res.send(`window.API_BASE_URL = "${API_BASE_URL}";`);
});

const baseDir = path.join(__dirname, 'stitch_parikshaguru_assessment_portal');

// Static Asset Routing for Stitch UI folders
app.use('/sign_in', express.static(path.join(baseDir, 'sign_in_registration')));
app.use('/student', express.static(path.join(baseDir, 'student_dashboard_available_exams')));
app.use('/exam-hall', express.static(path.join(baseDir, 'live_exam_taking_interface')));
app.use('/results', express.static(path.join(baseDir, 'student_results_approvals')));
app.use('/faculty', express.static(path.join(baseDir, 'faculty_management_results_approval')));

// Root redirect to Sign-In page
app.get('/', (req, res) => {
    res.redirect('/sign_in/code.html');
});

// Start Express Static Server
app.listen(PORT, () => {
    console.log(`ParikshaGuru Frontend Static Portal running on http://localhost:${PORT}`);
    console.log(`Target Backend API URL: ${API_BASE_URL}`);
});
