# Educatio Quiz - Educational Quiz Platform

## Overview
Educatio Quiz is a Spring Boot web application for creating and managing educational quizzes across different education levels (from elementary school to graduate studies). The platform allows professors to create quizzes and share questions with other professors in their area, while students can access and complete these quizzes.

## Project Status
✅ Successfully imported from GitHub and configured for Replit environment
✅ Application running on port 5000
✅ Thymeleaf frontend with responsive design
✅ Spring Boot 3.5.6 with Java 19
✅ PostgreSQL database support configured

## Technology Stack
- **Backend**: Spring Boot 3.5.6, Java 19
- **Frontend**: Thymeleaf templates, HTML5, CSS3
- **Database**: PostgreSQL (with JPA/Hibernate)
- **Build Tool**: Maven
- **Server**: Embedded Tomcat (port 5000)

## Project Structure
```
Projeto IntelliJ/
├── src/main/
│   ├── java/br/uel/educatio/quiz/
│   │   ├── controller/     # REST controllers and web controllers
│   │   ├── dao/           # Data Access Objects for database operations
│   │   ├── model/         # Entity models (Aluno, Professor, Quiz, Questao, etc.)
│   │   └── service/       # Business logic services
│   └── resources/
│       ├── templates/     # Thymeleaf HTML templates
│       ├── static/        # Static assets (CSS, JS, images)
│       └── application.properties
└── pom.xml               # Maven dependencies
```

## Recent Changes (November 4, 2025)
- Fixed Java version compatibility (changed from Java 25 to Java 19)
- Corrected main method visibility in EducatioQuizApplication.java
- Fixed compilation errors in RespostaDAO.java (removed malformed method)
- Fixed import issues in AlunoController.java
- Fixed static method issue in AuthService.java
- Configured server to run on 0.0.0.0:5000 for Replit compatibility
- Created beautiful Thymeleaf homepage template
- Configured Maven wrapper permissions
- Set up deployment configuration for autoscale

## Database Configuration
The application uses PostgreSQL and is configured to read database credentials from environment variables:
- `DATABASE_URL`: Full PostgreSQL connection URL
- `PGUSER`: Database username
- `PGPASSWORD`: Database password

Default fallback: `jdbc:postgresql://localhost:5432/educatio_quiz`

## Running the Application
The application automatically starts via the workflow system. To manually run:
```bash
cd "Projeto IntelliJ"
./mvnw spring-boot:run
```

## Key Features (Planned)
- 📚 Professor dashboard for quiz creation and management
- 🎓 Student interface for taking quizzes
- 🔗 Shared question bank across professors in the same area
- 📊 Support for multiple education levels
- Multiple question types: True/False, Fill in the Blank, Multiple Choice

## Database Schema
The database schema is defined in `Detalhamento do sistema/Educatio Quiz.sql` and includes tables for:
- AREA (subject areas)
- ALUNO (students)
- PROFESSOR (professors)
- QUIZ (quizzes)
- QUESTAO (questions)
- ALTERNATIVA (answer choices)
- RESPOSTA (student responses)
- QUIZ_QUESTAO (quiz-question relationships)
- PROFESSOR_AREA (professor area assignments)

## Known Issues
⚠️ Database needs to be created and initialized with schema
⚠️ Some controller methods are stubs and need implementation
⚠️ Password storage is currently plain text (should be hashed for production)

## Next Steps
1. Set up PostgreSQL database in Replit
2. Initialize database with schema from SQL file
3. Implement authentication flows
4. Build out quiz creation and management features
5. Add proper password hashing
