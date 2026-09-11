# 💼 JobSphere — Job Application Portal

**JobSphere** is a job application portal built with **Java and Spring Boot** that allows users to discover job opportunities, manage applications, and provides companies/admins with tools to manage job postings.

## 🚀 Features

* 👤 User registration and authentication
* 🔐 JWT-based authentication with Spring Security
* 👥 Role-based authorization
* 💼 Job posting and management
* 🔎 Job search by title, location, and company
* 📄 Job application management
* 📑 Pagination for job listings
* 🏢 Company and user management
* 🗄️ Relational database management using MySQL

## 🛠️ Tech Stack

| Technology          | Usage                          |
| ------------------- | ------------------------------ |
| **Java**            | Backend development            |
| **Spring Boot**     | REST API development           |
| **Spring Security** | Authentication & authorization |
| **JWT**             | Secure authentication          |
| **Spring Data JPA** | Database interaction           |
| **MySQL**           | Relational database            |
| **Postman**         | API testing                    |
| **Maven**           | Build & dependency management  |
| **Git & GitHub**    | Version control                |

## 🏗️ Architecture

The application follows a layered backend architecture:

```text
Client
   ↓
REST Controllers
   ↓
Service Layer
   ↓
Repository Layer
   ↓
MySQL Database
```

Authentication is handled through **Spring Security and JWT**, with role-based access controlling protected resources.

## 📌 Key Backend Implementations

* Designed RESTful APIs for **users, companies, jobs, and applications**
* Implemented **JWT authentication** and role-based authorization
* Created JPA entities and relationships for the relational data model
* Implemented job searching with **pagination**
* Tested and validated APIs using **Postman**

## ⚙️ Getting Started

### Prerequisites

* Java 17+
* Maven
* MySQL
* Git

### Clone the Repository

```bash
git clone <your-repository-url>
cd JobSphere
```

### Configure Database

Update your `application.properties` / `application.yml` with your MySQL configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jobsphere
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Run the Application

```bash
mvn spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

## 📮 API Testing

API endpoints can be tested using **Postman**.

Example API modules:

```text
/auth
/users
/companies
/jobs
/applications
```

## 🔮 Future Improvements

* Advanced job recommendation system
* Resume upload and parsing
* Email notifications
* Company profiles
* Application status tracking
* Admin analytics dashboard

## 👩‍💻 Author

**Chhavi Dubey**

Java Backend Developer | Spring Boot | SQL | DSA

