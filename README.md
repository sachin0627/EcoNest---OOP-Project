# EcoNest — Smart Home & Garden Retail Management System

## Project Overview

EcoNest is a Java Swing desktop application developed for the Object-Oriented Programming (OOP) group project.

The system helps a home and garden retail company manage its products, clients, inventory, orders, deliveries, reports, and email notifications.

**Course:** Higher Diploma in Information Technology (HDIT)  
**Module:** Object-Oriented Programming (Java)  
**Academic Year:** 2026  
**Semester:** Year 1 Semester 2  
**Group Size:** 6 Members

## Technologies Used

- Java
- Java Swing
- MySQL
- JDBC
- Maven
- Apache NetBeans
- FlatLaf
- JFreeChart
- Jakarta Mail

## Main System Features

1. Product Catalogue Management
2. Client Management
3. Stock / Inventory Management
4. Delivery Agent Management
5. Client Order Processing
6. Delivery Agent Assignment
7. Monthly Sales & Inventory Reports
8. Client Email Notifications
9. Delivery Agent Email Notifications

## Group Members and Task Allocation

| Student ID | Assigned Tasks | Responsibilities |
|---|---|---|
| SA 26610042 | Tasks 1 & 7 | Product Catalogue Management and Reports |
| SA 26610057 | Task 2 | Client Management |
| SA 26610141 | Task 3 | Stock / Inventory Management |
| SA 25610851 | Task 4 | Delivery Agent Management |
| SA 26610104 | Tasks 5 & 6 | Order Processing and Delivery Assignment |
| SA 26610158 | Tasks 8 & 9 | Email Notifications |

**Group Leader:** SA 26610042

## Object-Oriented Programming Concepts

The application demonstrates the following OOP concepts:

- **Encapsulation:** Private attributes with getters and setters.
- **Inheritance:** Classes extending parent classes.
- **Polymorphism:** Method overriding and overloading.
- **Abstraction:** Abstract classes and interfaces.
- **Exception Handling:** Handling invalid operations and application errors.
- **Database Integration:** MySQL database operations using JDBC.

## GitHub Collaboration

The repository is organized using GitHub Issues and feature branches.

### Branch Allocation

| Branch | Assigned Tasks |
|---|---|
| `feature/product-reports` | Tasks 1 & 7 |
| `feature/client-management` | Task 2 |
| `feature/inventory-management` | Task 3 |
| `feature/delivery-agent-management` | Task 4 |
| `feature/orders-delivery` | Tasks 5 & 6 |
| `feature/email-notifications` | Tasks 8 & 9 |

The `main` branch contains the integrated EcoNest application.

GitHub Issues are used to record task responsibilities, while feature branches provide separate working areas for each member.

## Database

The application uses a MySQL database.

The database setup script is available in:

`econest_db.sql`

The database supports product management, clients, inventory, suppliers, orders, delivery agents, deliveries, and notification records.

## How to Run

### Requirements

- Java JDK 17 or higher
- MySQL Server
- Apache Maven
- NetBeans or another Java IDE

### Instructions

1. Download or clone the repository.
2. Open the project as a Maven project in NetBeans.
3. Create the MySQL database using `econest_db.sql`.
4. Configure the database connection using the provided `.env.example` as a template.
5. Ensure MySQL Server is running.
6. Build the Maven project.
7. Run the main class `com.econest.Main`.

Alternatively, use the provided Windows batch scripts according to the project documentation.

## Project Documentation

The project includes Java source code, database scripts, and supporting documentation.

Individual Use Case Diagrams, Class Diagrams, and ER Diagrams are prepared according to the university project requirements.

## Project Purpose

The purpose of EcoNest is to apply Object-Oriented Programming concepts to a practical retail management system while gaining experience with Java application development, database integration, project organization, and GitHub collaboration.
