-- Reference data only. Users/passwords are created by the application's DataSeeder (BCrypt hashed), not by SQL.
-- Start the backend once, then (optionally) run this file. All statements are idempotent.
USE parikshaguru_db;

INSERT IGNORE INTO departments (code, name) VALUES
 ('CSE','Computer Science & Engineering'), ('ECE','Electronics & Communication Engineering'),
 ('EEE','Electrical & Electronics Engineering'), ('ME','Mechanical Engineering'),
 ('CE','Civil Engineering'), ('MATH','Mathematics');

INSERT IGNORE INTO subjects (code, name, department_id, semester, credit_points, description)
SELECT 'CS603','Computer Networks & Protocol Engineering', d.id, 6, 4.0, 'Sample subject' FROM departments d WHERE d.code='CSE';
INSERT IGNORE INTO subjects (code, name, department_id, semester, credit_points, description)
SELECT 'CS604','Software Engineering Methodologies', d.id, 6, 3.0, 'Sample subject' FROM departments d WHERE d.code='CSE';
INSERT IGNORE INTO subjects (code, name, department_id, semester, credit_points, description)
SELECT 'MA601','Discrete Probability & Stochastic Processes', d.id, 6, 3.0, 'Sample subject' FROM departments d WHERE d.code='MATH';

-- Useful verification queries
-- SELECT id, email, role, status FROM users;
-- SELECT code, title, status FROM examinations;
-- SELECT s.id, u.email, s.status, s.deadline FROM examination_sessions s JOIN users u ON u.id = s.student_id;
