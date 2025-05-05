# Project Changelog

This file tracks all significant changes and actions made to the Java Banking System project from its inception.

---

## 2025-05-04
- Initial project setup with Spring Boot, Thymeleaf, MySQL, and MVC architecture.
- Created `.gitignore` to exclude build and IDE files from version control.
- Added `README.md` with features, stack, setup, and structure.
- Configured `application.properties` for MySQL (username: root, password: [set/blank depending on XAMPP]).
- Created MySQL database `bankdb`.
- Created registration page template (`register.html`) in `templates/auth/`.
- Fixed missing Thymeleaf template error.
- Updated project to use MySQL instead of H2.
- Added project to GitHub: [https://github.com/prathambalehosur/javabankingsystem](https://github.com/prathambalehosur/javabankingsystem)
- Verified that the project builds and runs with `mvn spring-boot:run`.
- Guided user through XAMPP and MySQL setup for local development.

---

## Next Steps
- Implement user authentication and security features.
- Build out account management, transactions, loans, and investment modules.
- Add more Thymeleaf templates and improve UI/UX.
- Set up automated testing and CI/CD pipelines.

---

*This changelog will be updated as further changes are made to the project.*
