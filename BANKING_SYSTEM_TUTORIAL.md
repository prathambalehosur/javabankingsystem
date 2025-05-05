# Java Banking System - Complete Tutorial

## Table of Contents
1. [Introduction](#introduction)
2. [Project Setup](#project-setup)
3. [System Architecture](#system-architecture)
4. [Feature Walkthrough](#feature-walkthrough)
5. [Code Explanation](#code-explanation)
6. [Database Schema](#database-schema)
7. [API Endpoints](#api-endpoints)
8. [Security Implementation](#security-implementation)
9. [Testing](#testing)
10. [Deployment](#deployment)
11. [Troubleshooting](#troubleshooting)
12. [Future Enhancements](#future-enhancements)

## Introduction
This tutorial explains how to build and understand the Java Banking System, a full-stack web application for managing bank accounts and transactions.

## Project Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ (for production)
- Git

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/prathambalehosur/javabankingsystem.git
   cd javabankingsystem
   ```

2. Configure database in `src/main/resources/application.properties`

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

5. Access the application at `http://localhost:8081`

## System Architecture

### Tech Stack
- **Backend**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, Bootstrap 5, JavaScript
- **Database**: H2 (dev), MySQL (prod)
- **Build Tool**: Maven

### Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/bankingsystem/
│   │       ├── config/      # Configuration classes
│   │       ├── controller/  # Web controllers
│   │       ├── model/       # Entity classes
│   │       ├── repository/  # Data access layer
│   │       ├── security/    # Security configurations
│   │       └── service/     # Business logic
│   └── resources/
│       ├── static/     # CSS, JS, images
│       └── templates/    # Thymeleaf templates
└── test/                 # Test files
```

## Feature Walkthrough

### 1. User Authentication
- Secure login/logout
- User registration
- Password encryption
- Session management

### 2. Account Management
- Create multiple account types
- View account details
- Check balances
- View transaction history

### 3. Transactions
- Transfer between accounts
- Transaction history
- Real-time balance updates

### 4. Notifications
- Transaction alerts
- Account activity notifications
- System messages

## Code Explanation

### Key Classes

#### AccountController.java
Handles all account-related HTTP requests:
- `GET /accounts` - List all accounts
- `GET /accounts/{id}` - View account details
- `POST /accounts` - Create new account

#### TransactionService.java
Contains business logic for transactions:
- Transfer money between accounts
- Validate transactions
- Update account balances

#### SecurityConfig.java
Configures security settings:
- Authentication rules
- Password encoding
- Protected routes

## Database Schema

### Tables
1. `users` - User account information
2. `accounts` - Bank accounts
3. `transactions` - Transaction records
4. `notifications` - System notifications

## API Endpoints

### Authentication
- `POST /register` - Register new user
- `POST /login` - User login
- `POST /logout` - User logout

### Accounts
- `GET /accounts` - List user accounts
- `POST /accounts` - Create account
- `GET /accounts/{id}` - Get account details

### Transactions
- `POST /transactions` - Create new transaction
- `GET /transactions` - List transactions

## Security Implementation
- BCrypt password hashing
- CSRF protection
- Session management
- Role-based access control

## Testing

### Unit Tests
Run all tests:
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

## Deployment

### Production Setup
1. Configure MySQL database
2. Update `application-prod.properties`
3. Build with production profile:
   ```bash
   mvn clean package -Pprod
   ```
4. Deploy the generated JAR file

## Troubleshooting

### Common Issues
1. **Database Connection Errors**
   - Verify database credentials
   - Check if MySQL service is running

2. **Port Conflicts**
   - Change server port in `application.properties`

3. **Build Failures**
   - Clean Maven cache: `mvn clean install -U`

## Future Enhancements
1. Mobile app integration
2. Two-factor authentication
3. Check deposit via mobile
4. Budgeting tools
5. Investment features

---

For more information, visit the [GitHub repository](https://github.com/prathambalehosur/javabankingsystem).
