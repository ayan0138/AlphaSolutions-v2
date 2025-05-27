# Contributing to AlphaSolutions-v2

Thank you for your interest in contributing to this project!

This web application was built as a 2nd-semester group project at KEA (Datamatiker).  
Although it is a school project, we follow real-world Git and CI/CD workflows.

---

## 👥 Team Members

- **Najib Nawabi** – Scrum Master (Sprint 1), Backend Developer – [@NajibJivo](https://github.com/NajibJivo)
- **Ayan Ahmed** – GitHub Owner, Backend Developer – [@ayan0138](https://github.com/ayan0138)
- **Lawand Marouf** – QA & Testing Responsible – [@Lawand5](https://github.com/Lawand5)
- **Marcus  Lieber Heilstrup** – Frontend Developer – [@MarcusLieberH](https://github.com/MarcusLieberH)

---

## 🧑‍💻 Branch Naming Convention

Please use clear and structured branch names:

- `feature/user-login` – new features
- `bugfix/login-error` – bug fixes
- `hotfix/deploy-failure` – production issues
- `test/user-controller-tests` – test-related branches
- `refactor/task-service` – code cleanup or structure changes

---

## 🧹 Code Style Guidelines

### Java
- Use **camelCase** for variables and methods.
- Class names must be **PascalCase** (e.g., `ProjectService`).
- Avoid long methods – use helper methods to improve readability.
- No hardcoded values – use constants where relevant.
- Follow the MVC pattern (Model, View, Controller).
- Use `final` where applicable.
- Avoid duplicate logic – DRY principle applies.

### HTML / Thymeleaf
- Use semantic HTML (`<section>`, `<nav>`, `<article>`, etc.)
- Indent with 2 spaces.
- Keep Thymeleaf expressions readable:  
  ❌ `th:if="${x!=null}"`  
  ✅ `th:if="${x != null}"`
- Use fragments for navbar, footer, etc.

### CSS
- Use external stylesheets or global `style.css`
- Reuse class names when possible (avoid inline styles)
- Keep a consistent naming convention (e.g., `project-card`, `btn-primary`)

---

## ✅ Commit Messages

Write descriptive and concise commit messages in English:

- `Add: login page styling and validation`
- `Fix: SQL error in TaskRepository`
- `Update: controller redirect logic`
- `Test: add unit tests for ProjectService`

---

## ⚙️ CI/CD Pipeline

The project is deployed automatically to Azure using GitHub Actions:

- **Trigger**: Any push to `main`
- **Workflow File**: `.github/workflows/deploy.yml`
- **Pipeline Stages**:
  - Build with Maven
  - Run tests
  - Deploy `.jar` to Azure Web App

💡 Tip: You can manually trigger deploys using the “Run workflow” button in GitHub Actions tab.

---

## 🧪 Testing Standards

- Use JUnit and Mockito for unit tests.
- Use `@WebMvcTest` + `MockMvc` for controller testing.
- Use `test-schema.sql` and H2 in-memory DB for integration testing.
- Ensure each test is **isolated** and has no side effects.

---

## 📦 Project Structure (Spring Boot)

The project uses:

- **JDBC** for data access (no Spring Data JPA)
- **MVC** design pattern
- **Custom authentication** with Spring Security and `UserDetailsService`
- MySQL in production, H2 in test environment

Directory structure:
├── controller/
├── model/
├── repository/
├── service/
├── templates/
├── static/
└── resources/


---

## ❗ Pull Requests

Before submitting a pull request:

- Ensure all new features have relevant tests.
- Lint and format your code.
- Add comments where needed.
- Make sure CI/CD pipeline passes.

---

## 💬 Questions?

Reach out to the GitHub owner [@ayan0138](https://github.com/ayan0138) or Scrum Master [@NajibJivo](https://github.com/NajibJivo) for more info.

---

Thanks again for contributing! 🙌

