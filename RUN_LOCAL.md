# ParikshaGuru Local Development Setup (Stage 1)

This guide walks you through running ParikshaGuru locally with Aiven MySQL as the database.

## Prerequisites

1. **Java 21+** - For Spring Boot backend
2. **Node.js 18+** - For Express frontend server  
3. **Maven** - For building the Java project
4. **VS Code** (optional) - For integrated development
5. **Aiven MySQL Service** - Production database

## Aiven MySQL Connection Setup

### Step 1: Get Aiven Connection Details
1. Log into your Aiven account at [console.aiven.io](https://console.aiven.io)
2. Navigate to your MySQL service
3. Click on the **"Overview"** tab
4. Copy the following details:
   - **Host** (connection string)
   - **Port**
   - **Database Name** 
   - **Username**
   - **Password** (click "Show" next to password)

### Step 2: Configure Environment Variables

Create a `.env` file in the backend directory:

```bash
# Copy the example file
cd parikshaguru-backend/parikshaguru-backend
copy .env.example .env
```

Edit the `.env` file with your Aiven details:

```properties
# Aiven MySQL Configuration
DB_URL=jdbc:mysql://[AIVEN_HOST]:[PORT]/[DATABASE_NAME]?useSSL=true&requireSSL=true
DB_USER=[AIVEN_USERNAME]
DB_PASSWORD=[AIVEN_PASSWORD]

# Server Configuration  
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=aiven

# Security
JWT_SECRET=your-strong-jwt-secret-key-minimum-32-characters

# Development Settings (disable for production)
SEED_ENABLED=false
SEED_DEMO_DATA=false

# CORS (frontend URL)
CORS_ORIGINS=http://localhost:3000
```

## Running the Application

### Option A: VS Code (Recommended)

1. Open the project root in VS Code:
   ```bash
   code C:\Users\Arunkumar\ParikshaGuru
   ```

2. Install frontend dependencies (first time only):
   - Press `Ctrl+Shift+P` → "Tasks: Run Task" → "Install Frontend Dependencies"

3. Start both servers:
   - Press `Ctrl+Shift+D` to open Debug view
   - Select "Full Stack Launch (Backend + Frontend)" from dropdown
   - Click the green play button ▶️
   - OR start separately:
     - "Launch ParikshaGuru Backend (Aiven)" for backend
     - "Start Frontend Server" task for frontend

### Option B: Manual Terminal

**Terminal 1 - Backend:**
```bash
cd parikshaguru-backend/parikshaguru-backend

# Build the project
mvn clean compile

# Run with Aiven profile (reads from .env)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=aiven"
```

**Terminal 2 - Frontend:**
```bash
cd "ParikshaGuru Frontend"

# Install dependencies (first time only)
npm install

# Start Express server
npm start
```

## Verify the Setup

### 1. Check Backend Status
- **Backend URL:** http://localhost:8080/api/auth/me
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Health Check:** http://localhost:8080/actuator/health

### 2. Access the Frontend
- **Login Page:** http://localhost:3000/sign_in/code.html

### 3. Test Credentials

**For testing with H2 (temporary database):**
1. Stop the Aiven backend
2. Create a `.env.h2` file:
   ```properties
   SPRING_PROFILES_ACTIVE=h2
   SEED_ENABLED=true
   SEED_DEMO_DATA=true
   ```
3. Run: `mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"`
4. Use:
   - Student: `rohan.verma@university.ac.in` / `Student@123`
   - Faculty: `nair@university.ac.in` / `Faculty@123`
   - Admin: `admin@test.local` / `Admin@123`

## Configuration Overview

### Environment Variables Reference

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_URL` | MySQL connection URL | - | **Yes** |
| `DB_USER` | Database username | - | **Yes** |
| `DB_PASSWORD` | Database password | - | **Yes** |
| `SPRING_PROFILES_ACTIVE` | Spring profile | `aiven` | No |
| `JWT_SECRET` | JWT signing key | Generated | **Yes** |
| `SERVER_PORT` | Backend port | `8080` | No |
| `CORS_ORIGINS` | Allowed frontend origins | `http://localhost:3000` | No |
| `SEED_ENABLED` | Enable data seeding | `false` | No |
| `SEED_DEMO_DATA` | Seed demo accounts | `false` | No |

### Profiles Available

1. **`aiven`** - Production Aiven MySQL with SSL
2. **`local`** - Local MySQL development
3. **`h2`** - In-memory H2 database (testing only)

## Database Configuration Notes

### Aiven SSL Requirements
The Aiven profile automatically configures:
- `useSSL=true` - Enable SSL
- `requireSSL=true` - Require SSL connection
- `verifyServerCertificate=true` - Validate server certificate

### Local MySQL
For local MySQL without SSL:
- Profile: `local`
- Connection: `jdbc:mysql://localhost:3306/parikshaguru_db?useSSL=false`

## Troubleshooting

### 1. Database Connection Issues
```bash
# Test MySQL connection
mysql -h [AIVEN_HOST] -P [PORT] -u [USERNAME] -p [DATABASE_NAME]
```

### 2. Port Conflicts
```bash
# Check ports in use
netstat -ano | findstr :8080
netstat -ano | findstr :3000
```

### 3. Missing Dependencies
```bash
# Reinstall frontend dependencies
cd "ParikshaGuru Frontend"
rm -rf node_modules package-lock.json
npm install
```

### 4. Java/Maven Issues
```bash
# Verify Java version
java -version

# Clean Maven build
cd parikshaguru-backend/parikshaguru-backend
mvn clean install -DskipTests
```

## Next Steps

After verifying the local setup works with Aiven:

1. **Test the full student flow:**
   - Login with your Aiven-based users
   - Start an exam session
   - Answer questions
   - Submit exam

2. **Prepare for Stage 2 (Render deployment):**
   - All configuration is already environment-based
   - No hardcoded URLs in code
   - CORS configured for production origins
   - SSL/security settings for Aiven are in place

## Important Notes

⚠️ **Never commit `.env` files to source control**  
⚠️ **Keep Aiven credentials secure and rotate regularly**  
⚠️ **Disable seeding (`SEED_ENABLED=false`) for production**  
⚠️ **Use strong JWT secrets in production**

## Support

- Backend logs: Check console output or `logs/application.log`
- Frontend logs: Check browser Developer Tools (F12) → Console
- API testing: Use Swagger UI at http://localhost:8080/swagger-ui.html