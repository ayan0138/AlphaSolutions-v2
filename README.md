![Build Status](https://github.com/ayan0138/AlphaSolutions-v2/actions/workflows/maven.yml/badge.svg)

# README – Setup Guide for AlphaSolutions-v2

This document provides a step-by-step guide to running the application both locally and via Azure App Service. It is intended for developers, testers, and reviewers who want to install, test, or extend the system.

## Repository and Live Version
- **GitHub Repository**: https://github.com/ayan0138/AlphaSolutions-v2  
- **Live Deployment (Azure)**: https://calcura-hreeezfah6gycscy.swedencentral-01.azurewebsites.net/

All code is version-controlled using Git and hosted on GitHub. The application uses CI/CD via GitHub Actions to automate building and deployment to Azure.

## Prerequisites
Before running the system locally, make sure the following tools are installed:

- Java SE 17
- IntelliJ IDEA Ultimate 2025.1
- Apache Maven 3.3.1 or later
- MySQL Server + MySQL Workbench (v8.0+)
- Git
- Modern web browser (e.g. Chrome, Edge, Firefox)

## Local Setup – Step by Step

1. **Clone the repository**
```bash
git clone https://github.com/ayan0138/AlphaSolutions-v2.git
```

2. **Open the project in IntelliJ IDEA**
- Choose *Open Project*, navigate to the `AlphaSolutions-v2` folder
- IntelliJ will automatically import Maven and download dependencies

3. **Create the MySQL database**
- Open MySQL Workbench
- Run the `schema.sql` script found in `/resources/sql` to create the database:
```sql
CREATE DATABASE calcura;
USE calcura;
-- Execute table structure and test data here
```

4. **Update the database login in `application.properties`**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/calcura
spring.datasource.username=root
spring.datasource.password=yourPassword
```

**Note:** The provided `application.properties` file is preconfigured to use environment variables (`JDBC_DATABASE_URL`, `JDBC_USERNAME`, `JDBC_PASSWORD`) for security reasons. When running the project locally, you may temporarily replace these with explicit values as shown above.

5. **Run the application**
- Locate `AlphaSolutionsV2Application.java`
- Right-click → *Run*
- Visit: `http://localhost:8080`

## Cloud Deployment (Azure)
The application uses GitHub Actions to deploy automatically to Azure when changes are pushed to the `main` branch:

- Build process defined in `maven.yml`
- Deployment handled via `deploy-azure.yml`

This allows seamless and reliable updates to the live application without manual uploads.

## Demo Login Credentials
For testing purposes, use the following demo login:
- **Username**: admin  
- **Password**: admin123

The application supports multiple roles (admin/user) with appropriate access control.

## Testing and CI
The system includes:
- Unit tests and integration tests using JUnit 5 + Spring Boot Test
- CI validation via GitHub Actions

To run tests locally:
```bash
mvn test
```

## Known Issues and Improvements
- Some validation messages are not consistently displayed in the browser
- Session expiration may cause redirects without explanation
- Navigation could be improved with better "Back" buttons and confirmation feedback

Functionality and security have been prioritized. UI/UX improvements and enhanced test coverage are planned for future iterations.

## License and Further Development
This project was developed as part of a 2nd-semester exam at KEA (Copenhagen School of Design and Technology). The codebase is open for reuse and further development with appropriate citation. SQL and technical documentation are available in the GitHub repository.

For enterprise reuse, it's recommended to start by adjusting roles, database models, and user flows based on your specific organizational needs.
