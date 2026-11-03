-- ParikshaGuru database schema (MySQL 8). Run in MySQL Workbench: File > Open SQL Script > Execute (lightning icon).
-- OPTIONAL: the Spring Boot app creates/updates these tables itself (spring.jpa.hibernate.ddl-auto=update).
CREATE DATABASE IF NOT EXISTS parikshaguru_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE parikshaguru_db;

CREATE TABLE IF NOT EXISTS departments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(20) NOT NULL,
  name VARCHAR(120) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_dept_code (code),
  UNIQUE KEY uk_dept_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL,
  identifier VARCHAR(60) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  department_id BIGINT NULL,
  token_version INT NOT NULL DEFAULT 0,
  last_login_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email),
  UNIQUE KEY uk_users_identifier (identifier),
  KEY idx_users_role_status (role, status),
  CONSTRAINT fk_users_dept FOREIGN KEY (department_id) REFERENCES departments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS student_profiles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  course VARCHAR(100) NOT NULL,
  semester INT NOT NULL,
  phone VARCHAR(20) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_student_user (user_id),
  CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS faculty_profiles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  designation VARCHAR(100) NOT NULL,
  phone VARCHAR(20) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_faculty_user (user_id),
  CONSTRAINT fk_faculty_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS subjects (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL,
  name VARCHAR(150) NOT NULL,
  department_id BIGINT NOT NULL,
  semester INT NOT NULL,
  credit_points DOUBLE NOT NULL,
  description TEXT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_subject_code (code),
  KEY idx_subjects_semester (semester),
  CONSTRAINT fk_subject_dept FOREIGN KEY (department_id) REFERENCES departments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS subject_faculty (
  subject_id BIGINT NOT NULL,
  faculty_id BIGINT NOT NULL,
  PRIMARY KEY (subject_id, faculty_id),
  CONSTRAINT fk_sf_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
  CONSTRAINT fk_sf_faculty FOREIGN KEY (faculty_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS examinations (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(40) NOT NULL,
  title VARCHAR(200) NOT NULL,
  subject_id BIGINT NOT NULL,
  semester INT NOT NULL,
  exam_type VARCHAR(40) NOT NULL,
  total_marks DOUBLE NOT NULL,
  duration_minutes INT NOT NULL,
  start_time DATETIME(6) NOT NULL,
  end_time DATETIME(6) NOT NULL,
  negative_marking BIT(1) NOT NULL DEFAULT b'0',
  number_of_questions INT NOT NULL DEFAULT 0,
  instructions TEXT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  created_by BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_exam_code (code),
  KEY idx_exam_status (status),
  KEY idx_exam_window (start_time, end_time),
  CONSTRAINT fk_exam_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
  CONSTRAINT fk_exam_creator FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS exam_assignments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  exam_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_assignment (exam_id, student_id),
  CONSTRAINT fk_asg_exam FOREIGN KEY (exam_id) REFERENCES examinations(id) ON DELETE CASCADE,
  CONSTRAINT fk_asg_student FOREIGN KEY (student_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS questions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  exam_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  topic VARCHAR(120) NULL,
  question_text TEXT NOT NULL,
  question_type VARCHAR(20) NOT NULL,
  correct_answer TEXT NULL,
  marks DOUBLE NOT NULL,
  negative_marks DOUBLE NOT NULL DEFAULT 0,
  order_no INT NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_question_exam (exam_id, order_no),
  CONSTRAINT fk_q_exam FOREIGN KEY (exam_id) REFERENCES examinations(id) ON DELETE CASCADE,
  CONSTRAINT fk_q_subject FOREIGN KEY (subject_id) REFERENCES subjects(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS question_options (
  id BIGINT NOT NULL AUTO_INCREMENT,
  question_id BIGINT NOT NULL,
  label VARCHAR(2) NOT NULL,
  option_text TEXT NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_option_label (question_id, label),
  CONSTRAINT fk_opt_q FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS examination_sessions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  exam_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  started_at DATETIME(6) NOT NULL,
  deadline DATETIME(6) NOT NULL,
  submitted_at DATETIME(6) NULL,
  status VARCHAR(20) NOT NULL,
  ip_address VARCHAR(64) NULL,
  last_activity_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_session_exam_student (exam_id, student_id),
  KEY idx_session_status_deadline (status, deadline),
  CONSTRAINT fk_sess_exam FOREIGN KEY (exam_id) REFERENCES examinations(id),
  CONSTRAINT fk_sess_student FOREIGN KEY (student_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS student_answers (
  id BIGINT NOT NULL AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  selected_answer TEXT NULL,
  marked_for_review BIT(1) NOT NULL DEFAULT b'0',
  answered_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_answer_session_question (session_id, question_id),
  CONSTRAINT fk_ans_session FOREIGN KEY (session_id) REFERENCES examination_sessions(id) ON DELETE CASCADE,
  CONSTRAINT fk_ans_question FOREIGN KEY (question_id) REFERENCES questions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS submissions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  exam_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  submitted_at DATETIME(6) NOT NULL,
  auto_submitted BIT(1) NOT NULL DEFAULT b'0',
  objective_marks DOUBLE NOT NULL DEFAULT 0,
  total_marks DOUBLE NOT NULL DEFAULT 0,
  grading_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  feedback TEXT NULL,
  evaluated_by BIGINT NULL,
  evaluated_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_submission_session (session_id),
  UNIQUE KEY uk_submission_exam_student (exam_id, student_id),
  KEY idx_submission_grading (grading_status),
  CONSTRAINT fk_sub_session FOREIGN KEY (session_id) REFERENCES examination_sessions(id),
  CONSTRAINT fk_sub_exam FOREIGN KEY (exam_id) REFERENCES examinations(id),
  CONSTRAINT fk_sub_student FOREIGN KEY (student_id) REFERENCES users(id),
  CONSTRAINT fk_sub_evaluator FOREIGN KEY (evaluated_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS evaluations (
  id BIGINT NOT NULL AUTO_INCREMENT,
  submission_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  marks_awarded DOUBLE NULL,
  feedback TEXT NULL,
  auto_graded BIT(1) NOT NULL DEFAULT b'0',
  evaluator_id BIGINT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_eval_submission_question (submission_id, question_id),
  CONSTRAINT fk_eval_sub FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE CASCADE,
  CONSTRAINT fk_eval_q FOREIGN KEY (question_id) REFERENCES questions(id),
  CONSTRAINT fk_eval_user FOREIGN KEY (evaluator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS results (
  id BIGINT NOT NULL AUTO_INCREMENT,
  submission_id BIGINT NOT NULL,
  exam_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  total_marks DOUBLE NOT NULL,
  max_marks DOUBLE NOT NULL,
  percentage DOUBLE NOT NULL,
  grade VARCHAR(5) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING_APPROVAL',
  approved_by BIGINT NULL,
  approved_at DATETIME(6) NULL,
  published_by BIGINT NULL,
  published_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_result_submission (submission_id),
  UNIQUE KEY uk_result_exam_student (exam_id, student_id),
  KEY idx_result_status (status),
  CONSTRAINT fk_res_sub FOREIGN KEY (submission_id) REFERENCES submissions(id),
  CONSTRAINT fk_res_exam FOREIGN KEY (exam_id) REFERENCES examinations(id),
  CONSTRAINT fk_res_student FOREIGN KEY (student_id) REFERENCES users(id),
  CONSTRAINT fk_res_appr FOREIGN KEY (approved_by) REFERENCES users(id),
  CONSTRAINT fk_res_pub FOREIGN KEY (published_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  message TEXT NOT NULL,
  type VARCHAR(30) NOT NULL DEFAULT 'INFO',
  is_read BIT(1) NOT NULL DEFAULT b'0',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_notification_user (user_id, is_read),
  CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS proctoring_events (
  id BIGINT NOT NULL AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  event_type VARCHAR(30) NOT NULL,
  details VARCHAR(500) NULL,
  occurred_at DATETIME(6) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_proctor_session (session_id, occurred_at),
  CONSTRAINT fk_proc_session FOREIGN KEY (session_id) REFERENCES examination_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token_hash VARCHAR(64) NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  used BIT(1) NOT NULL DEFAULT b'0',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_reset_token (token_hash),
  CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NULL,
  action VARCHAR(60) NOT NULL,
  entity_type VARCHAR(40) NULL,
  entity_id BIGINT NULL,
  details VARCHAR(1000) NULL,
  ip_address VARCHAR(64) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_audit_entity (entity_type, entity_id),
  KEY idx_audit_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
