const bcrypt = require('bcrypt');
const db = require('./db');

async function seedDatabase() {
    try {
        console.log('Starting optional database seeding...');

        // 1. Seed Users
        const studentPass = await bcrypt.hash('StudentSecure#2024', 12);
        const facultyPass = await bcrypt.hash('FacultyProctor#2024', 12);

        await db.query(`
            INSERT IGNORE INTO users (id, email, password_hash, full_name, role, roll_no, department, semester, faculty_id) VALUES
            (1, 'rohan.verma@institute.edu.in', ?, 'Rohan Verma', 'student', '2024-CS-084', 'Computer Science & Engineering', 'Sem VI / Year 3', NULL),
            (2, 'dr.sknair@univ.ac.in', ?, 'Dr. S. K. Nair', 'faculty', NULL, 'Department of Computer Science', NULL, 'FAC-2021-992'),
            (3, 'priya.sharma@institute.edu.in', ?, 'Priya Sharma', 'student', '2024-CS-085', 'Computer Science & Engineering', 'Sem VI / Year 3', NULL),
            (4, 'karan.mehra@institute.edu.in', ?, 'Karan Mehra', 'student', '2024-CS-086', 'Computer Science & Engineering', 'Sem VI / Year 3', NULL)
        `, [studentPass, facultyPass, studentPass, studentPass]);

        console.log('Users seeded successfully.');

        // 2. Seed Subjects
        await db.query(`
            INSERT IGNORE INTO subjects (subject_code, title, credits, department, semester, enrolled_count) VALUES
            ('CS601', 'Operating Systems & Kernel Architecture', 4.0, 'Department of Computer Science', 'Semester VI', 132),
            ('CS602', 'Advanced Database Management Systems', 4.0, 'Department of Computer Science', 'Semester VI', 132),
            ('CS603', 'Computer Networks & Protocol Engineering', 4.0, 'Department of Computer Science', 'Semester VI', 128),
            ('CS604', 'Software Engineering Methodologies', 3.0, 'Department of Computer Science', 'Semester VI', 140),
            ('CS605', 'Design & Analysis of Algorithms', 4.0, 'Department of Computer Science', 'Semester VI', 144),
            ('MA601', 'Discrete Probability & Stochastic Processes', 3.0, 'Department of Mathematics', 'Semester VI', 110)
        `);

        console.log('Subjects seeded successfully.');

        // 3. Seed Exams
        await db.query(`
            INSERT IGNORE INTO exams (id, exam_code, title, subject_code, category, duration_mins, total_marks, passing_marks, status, start_time, closes_at) VALUES
            (1, 'CS602', 'Advanced Database Management Systems', 'CS602', 'cs', 60, 50, 20, 'published', NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 4 HOUR),
            (2, 'CS605', 'Design & Analysis of Algorithms', 'CS605', 'cs', 90, 75, 30, 'published', NOW() + INTERVAL 2 HOUR, NOW() + INTERVAL 6 HOUR),
            (3, 'MA601', 'Discrete Probability & Stochastic Processes', 'MA601', 'math', 50, 50, 20, 'published', NOW() - INTERVAL 2 HOUR, NOW() + INTERVAL 2 HOUR)
        `);

        console.log('Exams seeded successfully.');

        // 4. Seed Sample Questions for CS602 (Exam ID 1)
        await db.query(`
            INSERT IGNORE INTO questions (exam_id, q_number, question_text, code_snippet, question_type, marks, negative_marks, options_json, correct_answer) VALUES
            (1, 14, 'Consider the following log records in a relational database engine operating under the Write-Ahead Logging (WAL) protocol with strict ARIES recovery semantics. A system crash occurs abruptly at timestamp T_crash immediately following the write operation of Log Sequence Number (LSN) 106.',
            '[LSN 101] <T1 START>\n[LSN 102] <T1, Account_A, 500, 400, PrevLSN: 0>\n[LSN 103] <T2 START>\n[LSN 104] <T2, Account_B, 1200, 1500, PrevLSN: 0>\n[LSN 105] <CHECKPOINT BEGIN: ActiveTx=[T1, T2]>\n[LSN 106] <T1 COMMIT>\n---------------- CRASH OCCURS (SYSTEM HALT) ----------------',
            'mcq', 2.0, 0.25,
            '["Both T1 and T2 must be undone because the checkpoint was never officially finalized with an <CHECKPOINT END> log entry.", "Only T2 belongs to the Undo list since T1 committed before the crash; compensating log records (CLRs) will revert changes for Account_B.", "Neither transaction is undone because the Redo pass re-applies all actions to memory pages and commits them implicitly during checkpoint recovery.", "T1 is undone first in reverse chronological order, followed by the partial compensation of T2 up to LSN 103."]',
            'opt_b')
        `);

        console.log('Questions seeded successfully.');

        // 5. Seed Exam Attempts & Results
        await db.query(`
            INSERT IGNORE INTO exam_attempts (exam_id, student_id, status, start_time, submitted_at, auto_score, final_score, letter_grade, percentile, proctor_flags, approval_state, faculty_remarks, answers_json) VALUES
            (1, 1, 'submitted', NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 30 MINUTE, 42.00, 42.00, 'A', 94.00, 0, 'pending_approval', 'Awaiting faculty sign-off.', '{"14": "opt_b"}'),
            (3, 1, 'in_progress', NOW() - INTERVAL 20 MINUTE, NULL, 0.00, 0.00, 'PENDING', 0.00, 0, 'pending_approval', NULL, NULL)
        `);

        console.log('Exam attempts seeded successfully.');
        console.log('Database seeding completed successfully!');
        process.exit(0);
    } catch (err) {
        console.error('Seeding failed:', err.message);
        process.exit(1);
    }
}

seedDatabase();
