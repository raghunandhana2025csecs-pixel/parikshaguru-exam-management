# ParikshaGuru - Online Examination Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Node.js](https://img.shields.io/badge/Node.js-18+-green.svg)](https://nodejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**ParikshaGuru** is a comprehensive, secure online examination and assessment platform designed for educational institutions. It provides a complete end-to-end solution for creating, managing, conducting, and evaluating online exams with real-time proctoring capabilities.

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [System Architecture](#-system-architecture)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Deployment](#-deployment)
- [API Documentation](#-api-documentation)
- [Security](#-security)
- [Screenshots](#-screenshots)
- [Contributing](#-contributing)
- [License](#-license)

## ✨ Features

### Core Functionalities

#### 👨‍🎓 Student Features
- **Secure Authentication**: JWT-based authentication with role-based access control
- **Exam Dashboard**: View available exams, schedules, and deadlines
- **Live Exam Interface**: Take exams with real-time timer and auto-save functionality
- **Question Navigation**: Easy navigation between questions with bookmarking
- **Result Viewing**: Access exam results, scores, and performance analytics
- **Notification System**: Real-time notifications for exam schedules and results

#### 👨‍🏫 Faculty Features
- **Exam Management**: Create, edit, and schedule exams
- **Question Bank**: Comprehensive question management with multiple question types
- **Result Review**: Evaluate student submissions and provide feedback
- **Grade Management**: Approve and publish results
- **Analytics Dashboard**: View student performance metrics and statistics
- **Bulk Operations**: Import/export questions and results

#### 👨‍💼 Admin Features
- **User Management**: Manage students, faculty, and administrative accounts
- **Course Management**: Create and manage courses, subjects, and departments
- **System Configuration**: Configure exam settings, notifications, and system parameters
- **Audit Logs**: Track system activities and user actions
- **Report Generation**: Generate comprehensive reports for analysis

### Advanced Features
- **Multi-tier Role Management**: Student, Faculty, Admin roles with granular permissions
- **Exam Session Tracking**: Monitor active exam sessions with timestamps
- **Auto-submission**: Automatic exam submission on time expiry
- **Flexible Exam Types**: Support for MCQ, True/False, Short Answer, and Essay questions
- **Randomization**: Question and option randomization for exam integrity
- **Time Management**: Configurable exam duration with countdown timer
- **Draft Saving**: Auto-save answers as drafts during exam
- **Secure Proctoring**: Session-based exam monitoring

## 🛠 Technology Stack

### Backend
- **Framework**: Spring Boot 3.3.5
- **Language**: Java 21
- **Database**: MySQL 8.0 (Production), H2 (Development/Testing)
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT authentication
- **API Documentation**: Springdoc OpenAPI 2.6.0 (Swagger UI)
- **Build Tool**: Maven
- **Additional Libraries**:
  - Lombok for boilerplate reduction
  - JJWT for JWT token handling
  - Bean Validation for input validation

### Frontend
- **Runtime**: Node.js with Express.js server
- **Language**: JavaScript (ES6+)
- **Templating**: Server-side rendering with EJS
- **Styling**: Custom CSS with responsive design
- **HTTP Client**: Axios for API communication
- **Session Management**: Express-session with secure cookies

### Database
- **Production**: Aiven MySQL Cloud Database
- **SSL/TLS**: Enabled for secure connections
- **Connection Pooling**: HikariCP for efficient connection management
- **Migrations**: JPA schema management with validation mode

### DevOps & Deployment
- **Containerization**: Docker
- **Cloud Platform**: Render.com
- **CI/CD**: Git-based deployment
- **Version Control**: Git

## 🏗 System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│  (Web Browser - Responsive UI for Students/Faculty/Admin)   │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTPS
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    Frontend Server                           │
│         (Express.js - Static File Serving + Routing)        │
└────────────────────┬────────────────────────────────────────┘
                     │ REST API (JWT Auth)
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    Backend API Server                        │
│                  (Spring Boot REST API)                      │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Controller Layer (REST Endpoints)                   │   │
│  │  • AuthController    • ExamController                │   │
│  │  • StudentController • ResultController              │   │
│  │  • AdminController   • NotificationController        │   │
│  └──────────────────┬──────────────────────────────────┘   │
│                     ▼                                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Service Layer (Business Logic)                      │   │
│  │  • Authentication & Authorization                    │   │
│  │  • Exam Session Management                           │   │
│  │  • Result Calculation & Publishing                   │   │
│  └──────────────────┬──────────────────────────────────┘   │
│                     ▼                                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Repository Layer (Data Access)                      │   │
│  │  Spring Data JPA Repositories                        │   │
│  └──────────────────┬──────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────────┘
                     │ JDBC (SSL/TLS)
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    Database Layer                            │
│              (Aiven MySQL Cloud Database)                    │
│  • Users            • Exams          • Questions            │
│  • Exam Sessions    • Submissions    • Results              │
│  • Subjects         • Notifications                         │
└─────────────────────────────────────────────────────────────┘
```

### Request Flow
1. **Authentication**: User logs in → JWT token issued
2. **Authorization**: Token validated on each request → Role-based access granted
3. **Exam Flow**: Student starts exam → Session created → Answers submitted → Auto-save
4. **Result Flow**: Submission processed → Scores calculated → Results published

## 📁 Project Structure

```
ParikshaGuru/
├── parikshaguru-backend/
│   └── parikshaguru-backend/
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/parikshaguru/
│       │   │   │   ├── config/           # Configuration classes
│       │   │   │   │   ├── AppConfig.java
│       │   │   │   │   ├── SecurityConfig.java
│       │   │   │   │   ├── JwtConfig.java
│       │   │   │   │   └── DataSeeder.java
│       │   │   │   ├── controller/       # REST API endpoints
│       │   │   │   │   ├── AuthController.java
│       │   │   │   │   ├── ExamController.java
│       │   │   │   │   ├── StudentController.java
│       │   │   │   │   ├── ResultController.java
│       │   │   │   │   ├── AdminController.java
│       │   │   │   │   └── NotificationController.java
│       │   │   │   ├── dto/              # Data Transfer Objects
│       │   │   │   ├── entity/           # JPA Entities
│       │   │   │   │   ├── User.java
│       │   │   │   │   ├── Exam.java
│       │   │   │   │   ├── Question.java
│       │   │   │   │   ├── ExamSession.java
│       │   │   │   │   ├── Submission.java
│       │   │   │   │   └── Result.java
│       │   │   │   ├── repository/       # Data access layer
│       │   │   │   ├── service/          # Business logic
│       │   │   │   ├── security/         # JWT & Auth filters
│       │   │   │   └── exception/        # Custom exceptions
│       │   │   └── resources/
│       │   │       ├── application.yml           # Main config
│       │   │       ├── application-aiven.yml     # Aiven MySQL profile
│       │   │       ├── application-local.yml     # Local MySQL profile
│       │   │       └── application-h2.yml        # H2 test profile
│       │   └── test/                     # Unit & integration tests
│       ├── Dockerfile                    # Docker configuration
│       ├── pom.xml                       # Maven dependencies
│       └── .env.example                  # Environment template
│
├── ParikshaGuru Frontend/
│   ├── stitch_parikshaguru_assessment_portal/
│   │   ├── sign_in_registration/         # Login/Registration pages
│   │   ├── student_dashboard_available_exams/  # Student dashboard
│   │   ├── live_exam_taking_interface/   # Exam taking UI
│   │   ├── student_results_approvals/    # Results viewing
│   │   └── faculty_management_results_approval/ # Faculty interface
│   ├── server.js                         # Express server
│   ├── db.js                            # Database utility
│   ├── seed.js                          # Database seeding
│   ├── package.json                     # Node dependencies
│   └── Dockerfile                       # Frontend Docker config
│
├── render.yaml                          # Render deployment blueprint
├── RUN_LOCAL.md                        # Local setup guide
├── start_local.bat                     # Windows startup script
├── test_schema.sql                     # Test database schema
└── README.md                           # This file
```

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21 or higher** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **Node.js 18 or higher** - [Download](https://nodejs.org/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **MySQL 8.0** (for production) or H2 (embedded, for development)
- **Git** - [Download](https://git-scm.com/downloads)

### Installation

#### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/ParikshaGuru.git
cd ParikshaGuru
```

#### 2. Backend Setup

```bash
cd parikshaguru-backend/parikshaguru-backend

# Copy environment template
cp .env.example .env

# Edit .env with your database credentials
# For H2 testing (no MySQL required):
# SPRING_PROFILES_ACTIVE=h2
# SEED_ENABLED=true
# SEED_DEMO_DATA=true

# For MySQL/Aiven:
# DB_URL=jdbc:mysql://your-host:port/database?useSSL=true
# DB_USER=your-username
# DB_PASSWORD=your-password
# SPRING_PROFILES_ACTIVE=aiven

# Build the project
mvn clean install

# Run the backend (H2 profile for quick testing)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"
```

The backend will start on `http://localhost:8080`

#### 3. Frontend Setup

```bash
cd ParikshaGuru\ Frontend

# Install dependencies
npm install

# Start the frontend server
npm start
```

The frontend will start on `http://localhost:3000`

### 🧪 Testing the Application

#### With H2 Database (Quick Test)

The H2 profile automatically seeds demo data. Access the application at:

- **Frontend**: http://localhost:3000/sign_in/code.html
- **Backend API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html

**Test Credentials**:
```
Student:
  Email: rohan.verma@university.ac.in
  Password: Student@123

Faculty:
  Email: nair@university.ac.in
  Password: Faculty@123

Admin:
  Email: admin@test.local
  Password: Admin@123
```

#### With MySQL/Aiven (Production)

1. Create a MySQL database
2. Configure `.env` with your database credentials
3. Run with `aiven` or `local` profile
4. The schema will be automatically validated

### 📖 Detailed Setup Guide

For comprehensive local development setup instructions, see [RUN_LOCAL.md](RUN_LOCAL.md)

## 🌐 Deployment

### Render.com Deployment

This project includes a pre-configured `render.yaml` blueprint for one-click deployment.

#### Prerequisites
- Render.com account
- Aiven MySQL database (or any MySQL service)
- GitHub repository

#### Deployment Steps

1. **Fork/Push to GitHub**
   ```bash
   git remote add origin https://github.com/yourusername/ParikshaGuru.git
   git push -u origin master
   ```

2. **Create Render Blueprint**
   - Go to [Render Dashboard](https://dashboard.render.com/)
   - Click "New" → "Blueprint"
   - Connect your GitHub repository
   - Render will detect `render.yaml` automatically

3. **Configure Environment Variables**
   
   The blueprint will prompt for:
   - `DB_URL` - Your Aiven MySQL connection string
   - `DB_USER` - Database username
   - `DB_PASSWORD` - Database password
   - `JWT_SECRET` - Auto-generated (or provide your own)

4. **Deploy**
   - Click "Apply" to start deployment
   - Both backend and frontend will deploy automatically
   - Services are auto-linked (CORS and API URLs configured)

5. **Access Your Application**
   - Frontend: `https://parikshaguru-frontend.onrender.com`
   - Backend API: `https://parikshaguru-backend.onrender.com`

### Docker Deployment

Both frontend and backend include Dockerfiles for containerized deployment.

```bash
# Build backend
cd parikshaguru-backend/parikshaguru-backend
docker build -t parikshaguru-backend .

# Build frontend
cd ParikshaGuru\ Frontend
docker build -t parikshaguru-frontend .

# Run with docker-compose (create docker-compose.yml)
docker-compose up
```

## 📚 API Documentation

### Interactive API Documentation

Once the backend is running, access the Swagger UI for interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Key API Endpoints

#### Authentication
```
POST   /api/auth/register          # Register new user
POST   /api/auth/login             # Login and get JWT token
GET    /api/auth/me                # Get current user profile
POST   /api/auth/logout            # Logout
```

#### Exams
```
GET    /api/exams                  # List available exams
GET    /api/exams/{id}             # Get exam details
POST   /api/exams                  # Create exam (Faculty/Admin)
PUT    /api/exams/{id}             # Update exam
DELETE /api/exams/{id}             # Delete exam
```

#### Exam Sessions
```
POST   /api/sessions/start         # Start exam session
GET    /api/sessions/{id}          # Get session details
POST   /api/sessions/{id}/submit   # Submit exam
```

#### Results
```
GET    /api/results/student/{id}   # Get student results
GET    /api/results/exam/{id}      # Get exam results (Faculty)
POST   /api/results/approve        # Approve results (Faculty)
```

#### Questions
```
GET    /api/questions              # List questions (Faculty)
POST   /api/questions              # Create question
PUT    /api/questions/{id}         # Update question
DELETE /api/questions/{id}         # Delete question
```

#### Admin
```
GET    /api/admin/users            # List all users
POST   /api/admin/users            # Create user
PUT    /api/admin/users/{id}       # Update user
DELETE /api/admin/users/{id}       # Delete user
GET    /api/admin/analytics        # System analytics
```

### Authentication

All protected endpoints require JWT authentication:

```bash
# Login to get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"student@test.com","password":"Student@123"}'

# Use token in subsequent requests
curl -X GET http://localhost:8080/api/exams \
  -H "Authorization: Bearer <your-jwt-token>"
```

## 🔒 Security

### Security Features

1. **Authentication & Authorization**
   - JWT-based stateless authentication
   - Secure password hashing with BCrypt
   - Token expiration and refresh mechanism
   - Role-based access control (RBAC)

2. **API Security**
   - CORS configured for trusted origins only
   - CSRF protection enabled
   - Rate limiting (configurable)
   - Input validation with Bean Validation

3. **Database Security**
   - SSL/TLS encryption for database connections
   - Prepared statements to prevent SQL injection
   - Connection pooling with HikariCP
   - Sensitive data encryption

4. **Session Security**
   - Secure exam session management
   - Session timeout handling
   - Concurrent session detection
   - Auto-logout on inactivity

5. **Data Protection**
   - Environment-based configuration
   - Secrets management via environment variables
   - No hardcoded credentials
   - Audit logging for sensitive operations

### Best Practices Implemented

- **Principle of Least Privilege**: Users have minimal permissions
- **Defense in Depth**: Multiple security layers
- **Fail Securely**: Default deny access policy
- **Secure by Default**: Production-ready security configurations
- **Regular Updates**: Dependencies kept up-to-date

## 📸 Screenshots

### Student Dashboard
![Student Dashboard](docs/screenshots/student-dashboard.png)
*View available exams, schedules, and deadlines*

### Live Exam Interface
![Live Exam](docs/screenshots/live-exam.png)
*Take exams with real-time timer and navigation*

### Faculty Result Management
![Faculty Results](docs/screenshots/faculty-results.png)
*Review and approve student results*

### Admin Panel
![Admin Panel](docs/screenshots/admin-panel.png)
*Manage users, courses, and system configuration*

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java coding conventions
- Write unit tests for new features
- Update documentation as needed
- Ensure all tests pass before submitting PR
- Keep commits atomic and well-described

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Arunkumar** - *Initial work* - [ParikshaGuru Admin](mailto:admin@parikshaguru.com)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Aiven for reliable MySQL cloud hosting
- Render.com for seamless deployment platform
- OpenAPI/Swagger for API documentation tools
- All contributors and testers

## 📞 Support

For support, please email admin@parikshaguru.com or open an issue in the GitHub repository.

---

**Project Status**: Active Development | **Version**: 1.0.0 | **Last Updated**: November 2026

Made with ❤️ for better online education
