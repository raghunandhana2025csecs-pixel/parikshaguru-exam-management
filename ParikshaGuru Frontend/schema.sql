-- =========================================================================
-- ParikshaGuru Academic Assessment & Proctoring Portal - Production MySQL Schema
-- =========================================================================
-- Safe, robust, relational database schema with strict referential integrity.
-- Run in MySQL Workbench after selecting or creating your database.
-- =========================================================================

-- Ensure database exists & select it
CREATE DATABASE IF NOT EXISTS parikshaguru_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE parikshaguru_db;

-- Disable FK checks temporarily for clean initialization if re-running
SET FOREIGN_KEY_CHECKS = 0;

-- Drop existing tables in correct dependency order if re-initializing
DROP TABLE IF EXISTS exam_appeals;
DROP TABLE IF EXISTS exam_attempts;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS exams;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. USERS TABLE
-- Handles both Students and Faculty/Admins with strict role separation.
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role ENUM('student', 'faculty') NOT NULL DEFAULT 'student',
    roll_no VARCHAR(64) UNIQUE NULL,
    department VARCHAR(128) NOT NULL,
    semester VARCHAR(64) NULL,
    faculty_id VARCHAR(64) UNIQUE NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. SUBJECTS TABLE
-- Academic subjects taught across departments.
CREATE TABLE subjects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(32) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    credits DECIMAL(3,1) NOT NULL DEFAULT 4.0,
    department VARCHAR(128) NOT NULL,
    semester VARCHAR(64) NOT NULL,
    enrolled_count INT DEFAULT 0,
    INDEX idx_subjects_code (subject_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. EXAMS TABLE
-- Assessment sessions created by faculty.
CREATE TABLE exams (
    id INT AUTO_INCREMENT PRIMARY KEY,
    exam_code VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    subject_code VARCHAR(32) NOT NULL,
    category ENUM('cs', 'math', 'elec') NOT NULL DEFAULT 'cs',
    duration_mins INT NOT NULL DEFAULT 60,
    total_marks INT NOT NULL DEFAULT 50,
    passing_marks INT NOT NULL DEFAULT 20,
    status ENUM('draft', 'published', 'grading_review', 'closed') NOT NULL DEFAULT 'draft',
    start_time DATETIME NOT NULL,
    closes_at DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_code) REFERENCES subjects(subject_code) ON DELETE CASCADE,
    INDEX idx_exams_code (exam_code),
    INDEX idx_exams_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. QUESTIONS TABLE
-- Examination questions (MCQ / SAQ) linked to specific exams.
CREATE TABLE questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    exam_id INT NOT NULL,
    q_number INT NOT NULL,
    question_text TEXT NOT NULL,
    code_snippet TEXT NULL,
    question_type ENUM('mcq', 'saq') NOT NULL DEFAULT 'mcq',
    marks DECIMAL(4,2) NOT NULL DEFAULT 2.0,
    negative_marks DECIMAL(4,2) NOT NULL DEFAULT 0.25,
    options_json JSON NOT NULL,
    correct_answer VARCHAR(16) NOT NULL,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    UNIQUE KEY uq_exam_question (exam_id, q_number),
    INDEX idx_questions_exam (exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. EXAM ATTEMPTS TABLE
-- Tracks candidate attempts, answers, scoring, and faculty approval state.
-- Enforces strictly 1 attempt per student per exam via unique constraint.
CREATE TABLE exam_attempts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    exam_id INT NOT NULL,
    student_id INT NOT NULL,
    status ENUM('not_started', 'in_progress', 'submitted', 'approved') NOT NULL DEFAULT 'not_started',
    start_time DATETIME NULL,
    submitted_at DATETIME NULL,
    auto_score DECIMAL(5,2) DEFAULT 0.00,
    final_score DECIMAL(5,2) DEFAULT 0.00,
    letter_grade VARCHAR(8) DEFAULT 'PENDING',
    percentile DECIMAL(5,2) DEFAULT 0.00,
    proctor_flags INT DEFAULT 0,
    approval_state ENUM('pending_approval', 'approved') NOT NULL DEFAULT 'pending_approval',
    faculty_remarks TEXT NULL,
    answers_json JSON NULL,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_student_exam (exam_id, student_id),
    INDEX idx_attempts_status (status),
    INDEX idx_attempts_approval (approval_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. EXAM APPEALS TABLE
-- Student petitions for re-evaluation of finalized scores.
CREATE TABLE exam_appeals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    exam_id INT NOT NULL,
    grounds TEXT NOT NULL,
    status ENUM('submitted', 'under_review', 'resolved') NOT NULL DEFAULT 'submitted',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    INDEX idx_appeals_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
