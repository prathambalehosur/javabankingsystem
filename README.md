# Banking System

A comprehensive Java banking system with frontend, backend, database, and MVC architecture.

## Features

### User Authentication & Security
- Biometric login simulation
- Two-factor authentication
- Password encryption
- Session timeout and automatic logout

### Account Management
- Multiple account types (savings, checking, fixed-deposit)
- Joint accounts
- Freeze/unfreeze accounts
- Auto-generate statements

### Transactions
- Real-time fund transfers
- Scheduled/recurring payments
- Multi-currency support
- Transaction history with filters

### Loan Management
- Loan eligibility calculator
- Multiple loan types
- EMI calculator
- Auto-debit EMI payments

### Investment Features
- Mock investment integrations
- Portfolio tracking
- Interest calculators
- Retirement planning

## Technical Stack
- Spring Boot
- Spring Security
- Spring Data JPA
- Thymeleaf
- MySQL
- Bootstrap

## Getting Started

### Prerequisites
- Java 11 or higher
- Maven
- MySQL

### Installation
1. Clone the repository
2. Create a MySQL database named `bankdb`
3. Update `src/main/resources/application.properties` with your MySQL credentials
4. Run the application using: `mvn spring-boot:run`
5. Access the application at: `http://localhost:8080`

## Project Structure
- `src/main/java/com/bankingsystem/model` - Entity classes
- `src/main/java/com/bankingsystem/repository` - Data access layer
- `src/main/java/com/bankingsystem/service` - Business logic
- `src/main/java/com/bankingsystem/controller` - Web controllers
- `src/main/resources/templates` - Thymeleaf templates
- `src/main/resources/static` - Static resources (CSS, JS, images)

## License
This project is licensed under the MIT License - see the LICENSE file for details.
