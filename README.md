# GameStack Backend

This is the backend service for the **GameStack** application — a personal game backlog manager.  
Built with Java, Spring Boot, Spring Security (JWT), Hibernate (JPA), and MySQL.

## 📋 Features

- User authentication (Register/Login) with JWT tokens
- Role-based access control (User/Admin)
- CRUD functionality for game-related data (e.g., game reviews, libraries, or collections).
- RESTful API design for seamless frontend integration.
- Admin management of the master game list
- API endpoints for user backlog management
- IGDB API integration for game search

## 🛠 Technologies Used

- Java 21
- Spring Boot
- Spring Security
- Hibernate (JPA)
- MySQL
- Maven
- Lombok
- JWT (JSON Web Tokens)

---

## 🚀 Getting Started

### 1. Prerequisites

Make sure you have the following installed:

- [Java 17+](https://adoptium.net/)
- [Maven](https://maven.apache.org/)
- [MySQL](https://www.mysql.com/)
- [Lombok Plugin](https://projectlombok.org/) installed in your IDE (Important!)
  - IntelliJ: Enable annotation processing (`Settings -> Build, Execution, Deployment -> Compiler -> Annotation Processors -> Enable`)
- (Optional) [Postman](https://www.postman.com/) for testing API endpoints

---

### 2. Setup Instructions

#### Clone the repository

    git clone https://github.com/your-username/GameStack-Backend.git
    cd GameStack-Backend

Configure application properties
In src/main/resources/application.properties, configure your database settings:

    spring.datasource.url=jdbc:mysql://localhost:3306/gamestack_db
    spring.datasource.username=your_mysql_username
    spring.datasource.password=your_mysql_password
    spring.jpa.hibernate.ddl-auto=update
    
    # JWT Settings
    gamestack.jwt.secret=your_secret_key
    gamestack.jwt.expiration=86400000

---

### 🔥 Important Notes
- Lombok is required — missing Lombok will cause build errors.

- JWT token is required for accessing protected endpoints (obtain it via the /login endpoint).

- Admin and User roles are set upon registration or by seeding the database manually.

---

### 📬 API Endpoints Summary

Method | Endpoint | Description

POST | `/api/auth/register` | Register a new user

POST | `/api/auth/login` | Login and retrieve JWT token

GET | `/api/games/` | Get all games (Admin only)

POST | `/api/usergames/` | Add game to user's backlog

PUT | `/api/usergames/{id}` | Update a game in user's backlog

DELETE | `/api/usergames/{id}` | Remove game from backlog

---

🤝 Contributions
Feel free to open issues, submit pull requests, or suggest new features.

