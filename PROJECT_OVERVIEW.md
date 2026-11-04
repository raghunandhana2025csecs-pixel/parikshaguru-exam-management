# ParikshaGuru - Project Overview

## Executive Summary

**ParikshaGuru** is a full-stack online examination management system designed to facilitate secure, scalable, and efficient online assessments for educational institutions. The platform supports multiple user roles (Students, Faculty, Administrators) and provides comprehensive features for exam creation, delivery, and evaluation.

**Project Timeline**: October 2026 - November 2026  
**Current Version**: 1.0.0  
**Status**: Production Ready  
**Submission Date**: November 4, 2026

---

## 🎯 Project Objectives

### Primary Goals
1. **Secure Online Examination**: Provide a secure platform for conducting online exams with session management and integrity controls
2. **Multi-Role Support**: Enable distinct workflows for students, faculty, and administrators
3. **Real-Time Operations**: Support live exam taking with auto-save, timer, and instant submission
4. **Scalability**: Design architecture to handle multiple concurrent exam sessions
5. **User-Friendly Interface**: Deliver intuitive UI for all user types

### Secondary Goals
- Comprehensive result management and analytics
- Flexible question bank with multiple question types
- Notification system for exam schedules and results
- Export/Import capabilities for questions and results
- Audit logging for accountability

---

## 🏛 System Architecture Overview

### Architecture Pattern
**Three-Tier Architecture** with clear separation of concerns:

1. **Presentation Layer** (Frontend)
   - Express.js server serving static HTML/CSS/JS
   - Responsive web interface
   - Client-side validation and UX enhancements

2. **Business Logic Layer** (Backend)
   - Spring Boot REST API
   - Service-oriented architecture
   - JWT-based authentication
   - Role-based authorization

3. **Data Layer** (Database)
   - MySQL relational database
   - JPA/Hibernate ORM
   - Normalized schema design
   - Transaction management

### Key Design Decisions

#### 1. Technology Choices
- **Spring Boot**: Chosen for rapid development, built-in security, and enterprise-grade features
- **JWT Authentication**: Stateless authentication for scalability and microservices readiness
- **MySQL**: Reliable ACID-compliant RDBMS for transactional data integrity
- **Express.js**: Lightweight frontend server for efficient static file serving

#### 2. Security Approach
- **Defense in Depth**: Multiple security layers (authentication, authorization, validation)
- **Secure by Default**: All endpoints secured unless explicitly public
- **Environment-based Config**: No hardcoded credentials, all secrets in environment variables

#### 3. Deployment Strategy
- **Cloud-Native**: Designed for cloud deployment from day one
- **Containerization**: Docker support for consistent environments
- **Infrastructure as Code**: Render Blueprint for repeatable deployments

---

## 📊 Database Schema Design

### Core Entities

#### 1. User Management
```
users
├── id (PK)
├── email (unique)
├── password (encrypted)
├── full_name
├── role (STUDENT/FACULTY/ADMIN)
├── department
├── enrollment_number (for students)
├── created_at
└── updated_at
```

#### 2. Exam Management
```
exams
├── id (PK)
├── title
├── description
├── subject_id (FK)
├── created_by (FK → users)
├── duration_minutes
├── total_marks
├── passing_marks
├── start_time
├── end_time
├── status (DRAFT/SCHEDULED/ACTIVE/COMPLETED)
├── instructions
└── created_at

questions
├── id (PK)
├── exam_id (FK → exams)
├── question_text
├── question_type (MCQ/TRUE_FALSE/SHORT/ESSAY)
├── marks
├── options (JSON for MCQ)
├── correct_answer
├── difficulty_level
└── created_at
```

#### 3. Exam Sessions & Submissions
```
exam_sessions
├── id (PK)
├── exam_id (FK → exams)
├── student_id (FK → users)
├── start_time
├── end_time
├── status (IN_PROGRESS/SUBMITTED/EXPIRED)
├── remaining_time
└── created_at

submissions
├── id (PK)
├── session_id (FK → exam_sessions)
├── question_id (FK → questions)
├── answer_text
├── is_correct (for auto-grading)
├── marks_obtained
├── submitted_at
└── updated_at
```

#### 4. Results
```
results
├── id (PK)
├── exam_id (FK → exams)
├── student_id (FK → users)
├── total_marks_obtained
├── percentage
├── grade
├── status (PENDING/APPROVED/PUBLISHED)
├── approved_by (FK → users)
├── approved_at
├── remarks
└── created_at
```

#### 5. Supporting Entities
```
subjects
├── id (PK)
├── name
├── code
├── department
└── credits

notifications
├── id (PK)
├── user_id (FK → users)
├── message
├── type (INFO/WARNING/SUCCESS/ERROR)
├── is_read
└── created_at
```

### Relationships
- **One-to-Many**: User → Exams (created_by)
- **One-to-Many**: Exam → Questions
- **One-to-Many**: Exam → ExamSessions
- **One-to-Many**: ExamSession → Submissions
- **One-to-One**: ExamSession → Result
- **Many-to-One**: Exam → Subject

---

## 🎨 Feature Implementation Details

### 1. Authentication & Authorization

#### Implementation
- **Password Security**: BCrypt hashing with salt (strength factor: 12)
- **JWT Tokens**: 
  - Access Token: 24 hour expiration
  - Refresh Token: 7 day expiration
  - HS512 algorithm for signing
- **Role-Based Access Control**: 
  - Method-level security with `@PreAuthorize`
  - Custom authorization logic in services

#### Endpoints
- `POST /api/auth/register` - User registration with email verification
- `POST /api/auth/login` - Login with JWT token generation
- `GET /api/auth/me` - Get authenticated user profile
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - Invalidate tokens

### 2. Exam Management

#### Exam Lifecycle
1. **Creation** (Faculty/Admin)
   - Define exam metadata (title, duration, marks)
   - Set schedule (start/end time)
   - Configure settings (randomization, negative marking)

2. **Question Addition**
   - Add questions manually or import from bank
   - Set marks, difficulty, and correct answers
   - Support for multiple question types

3. **Publication**
   - Review and publish exam
   - Students receive notifications
   - Exam becomes available at scheduled time

4. **Execution**
   - Students start exam session
   - Real-time timer and auto-save
   - Session monitoring by faculty

5. **Evaluation**
   - Auto-grading for MCQ/True-False
   - Manual grading for subjective questions
   - Result approval and publication

### 3. Live Exam Taking

#### Student Experience
- **Pre-Exam**:
  - View exam instructions
  - Check system requirements
  - Confirm exam start

- **During Exam**:
  - Question navigation (Previous/Next/Jump to)
  - Question bookmarking for review
  - Real-time countdown timer
  - Auto-save answers every 30 seconds
  - Warning notifications (10 min, 5 min, 1 min remaining)

- **Submission**:
  - Manual submission (with confirmation)
  - Auto-submission on time expiry
  - Submission confirmation receipt

#### Technical Implementation
- **Session Management**: Server-side session tracking with expiration
- **Answer Persistence**: Real-time updates to database via AJAX
- **Timer Synchronization**: Server time used to prevent client-side manipulation
- **Concurrent Access Control**: Prevent multiple sessions for same exam

### 4. Result Management

#### Workflow
1. **Calculation**: 
   - Auto-calculate scores for objective questions
   - Aggregate total marks obtained
   - Calculate percentage and grade

2. **Review** (Faculty):
   - View student submissions
   - Grade subjective answers
   - Add remarks/feedback
   - Adjust marks if needed

3. **Approval**:
   - Faculty approves results
   - Admin can override if needed
   - Approval logged with timestamp

4. **Publication**:
   - Students notified via notification system
   - Results visible in student dashboard
   - Detailed score breakdown available

### 5. Admin Panel

#### User Management
- Create/Read/Update/Delete operations for users
- Bulk import from CSV
- Password reset functionality
- Account activation/deactivation

#### System Configuration
- Global settings (exam defaults, timeouts)
- Email/Notification templates
- Backup and restore utilities
- System health monitoring

---

## 🔧 Technical Implementation Highlights

### Backend (Spring Boot)

#### Key Components

1. **Security Configuration**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // JWT authentication filter
    // Role-based authorization rules
    // CORS configuration
    // Password encoder (BCrypt)
}
```

2. **JWT Service**
```java
@Service
public class JwtService {
    // Token generation
    // Token validation
    // Claims extraction
    // Expiration handling
}
```

3. **Service Layer Pattern**
```java
@Service
@Transactional
public class ExamService {
    // Business logic for exam operations
    // Validation and authorization checks
    // Integration with repositories
}
```

4. **Exception Handling**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Centralized exception handling
    // Consistent error response format
    // Logging and monitoring
}
```

#### Best Practices Implemented
- **DTO Pattern**: Separate DTOs for request/response to decouple API from entities
- **Builder Pattern**: Lombok builders for clean object creation
- **Repository Pattern**: Spring Data JPA repositories for data access
- **Service Layer**: Business logic separated from controllers
- **Validation**: Bean Validation (JSR-380) for input validation
- **Logging**: SLF4J with Logback for structured logging

### Frontend (Express + Vanilla JS)

#### Architecture
- **Server-Side Rendering**: Express serves HTML templates
- **Progressive Enhancement**: Works without JavaScript, enhanced with JS
- **Responsive Design**: Mobile-first CSS approach
- **Client-Side Routing**: History API for smooth navigation

#### Key Features
- **AJAX Communication**: Fetch API for REST calls
- **Local Storage**: Save user preferences and temporary data
- **Real-time Updates**: Polling for notifications and timer sync
- **Form Validation**: Client-side validation before submission
- **Error Handling**: User-friendly error messages

---

## 🚀 Deployment Architecture

### Production Environment (Render.com)

```
┌─────────────────────────────────────────┐
│        Render Load Balancer              │
│          (HTTPS Termination)             │
└─────────────┬───────────────────────────┘
              │
        ┌─────┴──────┐
        │            │
        ▼            ▼
┌──────────────┐  ┌──────────────┐
│   Frontend   │  │   Backend    │
│  (Node.js)   │  │ (Spring Boot)│
│   Service    │  │   Service    │
└──────────────┘  └──────┬───────┘
                         │
                         ▼
                  ┌──────────────┐
                  │ Aiven MySQL  │
                  │   Database   │
                  └──────────────┘
```

### Service Configuration

#### Frontend Service
- **Type**: Web Service
- **Runtime**: Node.js
- **Build**: `npm install`
- **Start**: `npm start`
- **Port**: Auto-detected by Render
- **Health Check**: `/health`

#### Backend Service
- **Type**: Web Service (Docker)
- **Runtime**: Docker
- **Build**: Maven multi-stage Dockerfile
- **Port**: 8080 (mapped by Render)
- **Health Check**: `/actuator/health`

#### Database
- **Provider**: Aiven MySQL
- **Version**: MySQL 8.0
- **Plan**: Business-4 (Production)
- **Backup**: Daily automated backups
- **SSL/TLS**: Enforced

### Environment Variables

#### Backend Environment
```
SPRING_PROFILES_ACTIVE=aiven
DB_URL=jdbc:mysql://...
DB_USER=avnadmin
DB_PASSWORD=<secure>
JWT_SECRET=<generated>
CORS_ORIGINS=<frontend-url>
SEED_ENABLED=false
```

#### Frontend Environment
```
NODE_ENV=production
API_BASE_URL=<backend-url>
SESSION_SECRET=<secure>
PORT=10000
```

---

## 📈 Performance Considerations

### Database Optimization
1. **Indexing**:
   - Primary keys on all tables
   - Foreign key indexes for joins
   - Composite indexes on frequently queried columns
   - Index on `users.email` for login queries

2. **Connection Pooling**:
   - HikariCP with optimal pool size
   - Connection timeout: 30s
   - Max lifetime: 30 minutes
   - Leak detection enabled

3. **Query Optimization**:
   - Eager/Lazy loading configured appropriately
   - N+1 query prevention with JOIN FETCH
   - Pagination for large result sets

### API Performance
1. **Caching**: 
   - HTTP caching headers for static content
   - Server-side caching for frequently accessed data
   
2. **Pagination**: 
   - All list endpoints support pagination
   - Default page size: 20 items
   
3. **Async Processing**: 
   - Non-blocking I/O where applicable
   - Async methods for long-running operations

---

## 🧪 Testing Strategy

### Unit Tests
- **Coverage Target**: 80%
- **Framework**: JUnit 5, Mockito
- **Scope**: Service layer business logic

### Integration Tests
- **Framework**: Spring Boot Test
- **Database**: H2 in-memory for tests
- **Scope**: API endpoints, repository layer

### Manual Testing
- **User Acceptance Testing**: All user flows tested
- **Browser Testing**: Chrome, Firefox, Safari, Edge
- **Mobile Testing**: Responsive design verified
- **Security Testing**: Authentication, authorization, input validation

---

## 🔐 Security Audit

### Vulnerabilities Addressed

1. **SQL Injection**: 
   - ✅ Prevented via JPA/Hibernate prepared statements
   
2. **XSS (Cross-Site Scripting)**: 
   - ✅ Input sanitization and output encoding
   
3. **CSRF (Cross-Site Request Forgery)**: 
   - ✅ CSRF tokens enabled for state-changing operations
   
4. **Authentication Bypass**: 
   - ✅ All endpoints properly secured with JWT
   
5. **Sensitive Data Exposure**: 
   - ✅ Passwords hashed, no sensitive data in logs/errors
   
6. **Broken Access Control**: 
   - ✅ Role-based authorization on all protected endpoints

### Security Checklist
- [x] HTTPS enforced in production
- [x] Secure password hashing (BCrypt)
- [x] JWT token expiration implemented
- [x] Database SSL/TLS enabled
- [x] Input validation on all endpoints
- [x] Error messages don't leak sensitive information
- [x] Audit logging for critical operations
- [x] Environment variables for secrets
- [x] CORS properly configured
- [x] Security headers set (X-Frame-Options, X-Content-Type-Options, etc.)

---

## 📚 Documentation Deliverables

### Included Documentation
1. ✅ **README.md** - Main project documentation
2. ✅ **PROJECT_OVERVIEW.md** - This file (comprehensive overview)
3. ✅ **RUN_LOCAL.md** - Local development setup guide
4. ✅ **API Documentation** - Swagger UI (auto-generated)
5. ✅ **Code Comments** - Inline documentation in code
6. ✅ **Database Schema** - SQL files with table definitions

### Additional Resources
- **Render Blueprint**: `render.yaml` for deployment
- **Environment Template**: `.env.example` files
- **Startup Scripts**: `start_local.bat` for Windows

---

## 🎓 Learning Outcomes

### Technologies Mastered
1. **Spring Boot Ecosystem**:
   - Spring Security with JWT
   - Spring Data JPA
   - Spring Boot Actuator
   - Spring Validation

2. **RESTful API Design**:
   - Resource-based URL design
   - HTTP status codes
   - Versioning strategies
   - Documentation with OpenAPI

3. **Authentication & Authorization**:
   - JWT token-based authentication
   - Role-based access control
   - Password security best practices

4. **Database Management**:
   - Schema design and normalization
   - JPA entity relationships
   - Transaction management
   - Query optimization

5. **Cloud Deployment**:
   - Docker containerization
   - Cloud platform configuration
   - Environment-based configuration
   - CI/CD concepts

---

## 🔮 Future Enhancements

### Planned Features (V2.0)
1. **Advanced Proctoring**:
   - Webcam monitoring
   - Screen recording
   - Face recognition
   - Browser lockdown

2. **Analytics Dashboard**:
   - Student performance analytics
   - Exam difficulty analysis
   - Question statistics
   - Comparative reports

3. **Mobile Application**:
   - Native Android/iOS apps
   - Offline exam support
   - Push notifications

4. **AI Integration**:
   - Auto-generation of questions
   - Plagiarism detection
   - Intelligent grading for essays
   - Personalized learning recommendations

5. **Collaboration Features**:
   - Faculty collaboration on question banks
   - Peer review of questions
   - Shared exam templates

### Technical Improvements
- Implement Redis caching
- Add real-time WebSocket notifications
- Migrate to microservices architecture
- Implement event-driven architecture
- Add comprehensive monitoring (Prometheus/Grafana)

---

## 📞 Project Contact

**Developer**: Arunkumar  
**Email**: admin@parikshaguru.com  
**GitHub**: [ParikshaGuru Repository](https://github.com/yourusername/ParikshaGuru)  
**Submission Date**: November 4, 2026

---

## 🏆 Project Highlights

### Why This Project Stands Out

1. **Production-Ready**: Not just a prototype, fully deployable to production
2. **Security-First**: Enterprise-grade security implementation
3. **Scalable Architecture**: Designed to handle growth
4. **Clean Code**: Well-organized, documented, and maintainable
5. **Modern Stack**: Latest technologies and best practices
6. **Complete Documentation**: Comprehensive guides and API docs
7. **Real-World Application**: Solves actual problems in education sector

### Technical Excellence
- ✅ Clean architecture with separation of concerns
- ✅ SOLID principles followed
- ✅ RESTful API design best practices
- ✅ Secure by default configuration
- ✅ Proper error handling and logging
- ✅ Environment-based configuration
- ✅ Database normalization and optimization
- ✅ Responsive and accessible UI

---

**Thank you for reviewing ParikshaGuru!**

This project represents a comprehensive implementation of a real-world online examination system, demonstrating proficiency in full-stack development, secure application design, and cloud deployment.
