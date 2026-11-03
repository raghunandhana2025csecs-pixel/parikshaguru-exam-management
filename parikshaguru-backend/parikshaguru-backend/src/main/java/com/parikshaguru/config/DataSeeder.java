package com.parikshaguru.config;

import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import com.parikshaguru.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Seeds reference data, the first administrator and (optionally) demo accounts so the system is usable
 * immediately. Passwords are BCrypt-hashed here. Disable demo data in production: SEED_DEMO_DATA=false.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final AppProperties props;
    private final DepartmentRepository departments;
    private final UserRepository users;
    private final StudentRepository students;
    private final FacultyRepository faculties;
    private final SubjectRepository subjects;
    private final ExaminationRepository exams;
    private final ExamAssignmentRepository assignments;
    private final QuestionRepository questions;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (!props.getSeed().isEnabled()) return;
        Department cse = dept("CSE", "Computer Science & Engineering");
        dept("ECE", "Electronics & Communication Engineering");
        dept("EEE", "Electrical & Electronics Engineering");
        dept("ME", "Mechanical Engineering");
        dept("CE", "Civil Engineering");
        dept("MATH", "Mathematics");

        var s = props.getSeed();
        if (!users.existsByEmailIgnoreCase(s.getAdminEmail())) {
            newUser(s.getAdminName(), s.getAdminEmail(), "ADMIN-001", s.getAdminPassword(), Role.ADMIN, cse);
            log.info("Seeded administrator {}", s.getAdminEmail());
        }
        if (!s.isDemoData() || users.existsByEmailIgnoreCase("nair@university.ac.in")) return;

        User nair = newUser("Dr. S. K. Nair", "nair@university.ac.in", "EMP-2001", "Faculty@123", Role.FACULTY, cse);
        Faculty f = new Faculty(); f.setUser(nair); f.setDesignation("Professor"); faculties.save(f);

        User rohan = newUser("Rohan Verma", "rohan.verma@university.ac.in", "2024-CS-084", "Student@123", Role.STUDENT, cse);
        student(rohan, "B.Tech Computer Science & Engineering", 6);
        User aarav = newUser("Aarav Sharma", "aarav.sharma@university.ac.in", "ENR-2024-902", "Student@123", Role.STUDENT, cse);
        student(aarav, "B.Tech Computer Science & Engineering", 6);

        Subject dbms = subject("CS602", "Advanced Database Management Systems", cse, 6, 4.0, nair);
        subject("CS605", "Design & Analysis of Algorithms", cse, 6, 4.0, nair);
        subject("CS701", "Distributed Systems & Cloud", cse, 7, 3.5, nair);

        Instant now = Instant.now();
        Examination e = new Examination();
        e.setCode("EX-CS602-DEMO"); e.setTitle("DBMS Mid-Term (Demo)"); e.setSubject(dbms); e.setSemester(6);
        e.setExamType("MID_TERM"); e.setTotalMarks(10); e.setDurationMinutes(30);
        e.setStartTime(now.minus(Duration.ofHours(1))); e.setEndTime(now.plus(Duration.ofDays(30)));
        e.setNegativeMarking(true); e.setNumberOfQuestions(5); e.setStatus(ExamStatus.PUBLISHED); e.setCreatedBy(nair);
        e.setInstructions("Answer all questions. Negative marking applies to objective questions.");
        exams.save(e);

        q(e, dbms, 1, QuestionType.MCQ, "Which ACID property guarantees a committed transaction survives a crash?", "D", 2, 0.5,
                "Atomicity", "Consistency", "Isolation", "Durability");
        q(e, dbms, 2, QuestionType.MULTI_SELECT, "Select all standard SQL isolation levels.", "A,B,C", 3, 0.5,
                "READ COMMITTED", "REPEATABLE READ", "SERIALIZABLE", "READ NONE");
        q(e, dbms, 3, QuestionType.TRUE_FALSE, "A PRIMARY KEY column may contain NULL values.", "FALSE", 1, 0.25);
        q(e, dbms, 4, QuestionType.SHORT_ANSWER, "Which normal form removes transitive dependencies?", "3NF|third normal form", 2, 0);
        q(e, dbms, 5, QuestionType.DESCRIPTIVE, "Explain the Write-Ahead Logging (WAL) protocol.", null, 2, 0);

        for (User st : List.of(rohan, aarav)) {
            ExamAssignment a = new ExamAssignment(); a.setExam(e); a.setStudent(st); assignments.save(a);
        }
        log.info("Seeded demo accounts: nair@university.ac.in / Faculty@123, rohan.verma@university.ac.in / Student@123");
    }

    private Department dept(String code, String name) {
        return departments.findByCodeIgnoreCase(code).orElseGet(() -> departments.save(new Department(code, name)));
    }

    private User newUser(String name, String email, String ident, String pw, Role role, Department d) {
        User u = new User();
        u.setFullName(name); u.setEmail(email.toLowerCase()); u.setIdentifier(ident.toUpperCase());
        u.setPasswordHash(encoder.encode(pw)); u.setRole(role); u.setStatus(AccountStatus.ACTIVE); u.setDepartment(d);
        return users.save(u);
    }

    private void student(User u, String course, int sem) {
        Student s = new Student(); s.setUser(u); s.setCourse(course); s.setSemester(sem); students.save(s);
    }

    private Subject subject(String code, String name, Department d, int sem, double credits, User fac) {
        Subject s = new Subject();
        s.setCode(code); s.setName(name); s.setDepartment(d); s.setSemester(sem); s.setCreditPoints(credits);
        s.getFaculty().add(fac);
        return subjects.save(s);
    }

    private void q(Examination e, Subject s, int order, QuestionType t, String text, String correct, double marks, double neg, String... opts) {
        Question q = new Question();
        q.setExam(e); q.setSubject(s); q.setOrderNo(order); q.setQuestionType(t); q.setQuestionText(text);
        q.setCorrectAnswer(correct); q.setMarks(marks); q.setNegativeMarks(neg); q.setTopic("Databases");
        for (int i = 0; i < opts.length; i++) {
            QuestionOption o = new QuestionOption();
            o.setQuestion(q); o.setLabel(String.valueOf((char) ('A' + i))); o.setOptionText(opts[i]);
            q.getOptions().add(o);
        }
        questions.save(q);
    }
}
