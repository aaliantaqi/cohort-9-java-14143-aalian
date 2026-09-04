# Contact Management System

A full-stack contact management application built as a Java Fullstack internship assignment. Users can register, log in, and manage a personal address book — creating contacts with multiple labeled email addresses and phone numbers, uploading a profile photo per contact, searching and paginating their contact list, and managing their own account (profile info, password).

This is a Cohort 9 (JAVA + ReactJS) assignment project.

---

## Tech Stack

**Backend**
- Java 17
- Spring Boot 3.5.5
- Spring Data JPA + Hibernate
- Spring Security (session-based authentication, CSRF protection)
- SQL Server (production/dev database)
- H2 (in-memory database for tests)
- Maven (build tool)
- JUnit 5 + Mockito (unit testing)
- JaCoCo (test coverage reporting)
- Lombok

**Frontend**
- React (built with Vite)
- React Router
- Axios (API calls)
- React Toastify (notifications)

**Code Quality / CI**
- SonarCloud (static analysis for both Java and JavaScript)
- GitHub Actions (CI pipeline)

---

## Project Structure

The repository has two main folders at the root:

```
├── Contact Management System/       # Backend (Spring Boot)
│   ├── src/main/java/com/tenpearls/contactmanagementsystem/
│   │   ├── controllers/             # REST controllers (UserController, ContactResource, etc.)
│   │   ├── services/                # Business logic (UserService, ContactService)
│   │   ├── repositories/            # Spring Data JPA repositories
│   │   ├── domain/                  # JPA entities (Contact, etc.)
│   │   ├── model/                   # Request/response DTOs (User, LoginRequest, etc.)
│   │   ├── security/                # Spring Security configuration
│   │   └── constant/                # Shared constants
│   ├── src/test/java/...            # Unit tests, mirrors the main package structure
│   └── pom.xml
│
├── contact-management-frontend/     # Frontend (React + Vite)
│   ├── src/
│   │   ├── api/                     # Axios service layer (ContactService.jsx, ToastService.jsx)
│   │   ├── components/              # React components (Login, Registration, ContactDetail, etc.)
│   │   ├── App.jsx                  # Root component, routing, and modal-based "new contact" form
│   │   └── main.jsx
│   └── package.json
│
└── .github/workflows/sonarcloud.yml # CI pipeline: build, test, and analyze both projects
```

---

## What the App Does

**Authentication**
- Users register with a first name, last name, and either an email or a phone number (or both), plus a password.
- Login accepts either email or phone as the identifier.
- Sessions are managed server-side (Spring Security), with CSRF token protection on all state-changing requests.
- Users can view their own profile (`/api/me`) and change their password.

**Contacts**
- Each contact has a first name, last name, title, address, and status, plus a profile photo.
- A contact can have multiple email addresses and multiple phone numbers, each with its own label (Work, Home, Personal, Other).
- Contacts belong to the logged-in user — one user's contacts are never visible to another user.
- The contact list is paginated and searchable by name.
- Photos are uploaded as multipart form data and stored on the server; each contact's photo can be replaced at any time.

---

## Running the Project Locally

### Prerequisites
- Java 17 (JDK)
- Maven (or use the project's Maven wrapper if present)
- Node.js and npm
- A SQL Server instance (or update the datasource config to point at whatever database you're using locally)

### Backend

```bash
cd "Contact Management System"
mvn spring-boot:run
```

The backend starts on its configured port (check `application.properties` / `application.yml` under `src/main/resources` for the exact port and datasource settings).

### Frontend

```bash
cd contact-management-frontend
npm install
npm run dev
```

This starts the Vite dev server, typically at `http://localhost:5173`. Make sure the backend is running first, since the frontend calls the backend's REST API for everything (auth, contacts, photo uploads).

---

## Running the Tests

### Backend unit tests

From inside the `Contact Management System` folder:

```bash
mvn test
```

Or, to run the full build (compile, test, package) the same way the CI pipeline does:

```bash
mvn -B verify
```

This runs all JUnit 5 tests under `src/test/java` (using Mockito for mocking repositories and dependencies) and generates a JaCoCo coverage report at:

```
Contact Management System/target/site/jacoco/jacoco.xml
```

This same report is what SonarCloud reads to calculate test coverage (see below).

### Frontend tests

The frontend does not currently have an automated test suite (no Jest/React Testing Library setup). Testing is done manually by running the app locally. This is a known gap, not an oversight worth hiding — if the project grows further, adding a JS testing setup would be the next step.

---

## Code Quality: SonarCloud Integration

This project is analyzed by [SonarCloud](https://sonarcloud.io) on every push to `main`/`develop` and on every pull request, via the GitHub Actions workflow at `.github/workflows/sonarcloud.yml`. The pipeline:

1. Builds and runs the backend's unit tests with Maven (`mvn -B verify`), which also generates the JaCoCo coverage report.
2. Runs the SonarCloud scanner across **both** the backend (`Contact Management System/src/main/java`) and the frontend (`contact-management-frontend/src`), so both Java and JavaScript get analyzed in a single scan.
3. Reports results back to the SonarCloud dashboard, including code smells, bugs, vulnerabilities, duplication, and test coverage.

**Current status:** Quality Gate passing, 0 open issues, 0% duplication, and coverage on newly changed code above the required threshold. Overall project coverage sits around 40%, which reflects that the backend has solid unit test coverage while the frontend currently has none (see "Frontend tests" above) — this pulls the combined average down even though the tested portion (backend) is in good shape.

### About the "Not authorized" SonarCloud error on the team repo

If you look at the Actions tab on the upstream team repository (`10pshine-cohort-9/cohort-9-java-14143-aalian`), you may notice the SonarCloud Analysis check does **not** run there — this is intentional, not a bug.

Here's why: this repo's SonarCloud project (`aaliantaqi_cohort-9-java-14143-aalian`) is registered under my personal SonarCloud organization, and the `SONAR_TOKEN` secret that authenticates the scanner is only configured on my fork (`aaliantaqi/cohort-9-java-14143-aalian`) — I don't have permission to add secrets to the shared team repo. Early on, this caused the workflow to fail there with:

```
Not authorized or project not found. Please check the 'SONAR_TOKEN' environment variable...
```

Rather than let it fail (which showed up as a red ❌ that didn't actually reflect any problem with the code), the workflow now includes a guard:

```yaml
jobs:
  sonarcloud:
    if: github.repository == 'aaliantaqi/cohort-9-java-14143-aalian'
```

This means the SonarCloud job only actually runs inside my fork, where the token is available. When the same workflow file runs on the team repo (e.g., because a PR was opened), it's cleanly **skipped** instead of failing — you'll see a grey "Skipped" status rather than a red one.

The real, up-to-date SonarCloud results for this project can always be viewed on my fork's dashboard: [sonarcloud.io/summary/overall?id=aaliantaqi_cohort-9-java-14143-aalian](https://sonarcloud.io/summary/overall?id=aaliantaqi_cohort-9-java-14143-aalian).

### CI pipeline security notes

The GitHub Actions workflow uses a pinned commit SHA (not a floating version tag) for the SonarCloud scan action, and declares an explicit least-privilege `permissions` block (`contents: read`, `pull-requests: read`) rather than relying on default token permissions — both changes were made in response to an automated security review (CodeRabbit) flagging a known CVE in an older version of the scan action and overly broad default token scope.

---

## Notes for Reviewers

- Backend and frontend are decoupled — the frontend is a pure API client and can be pointed at any backend instance by adjusting its API base URL configuration.
- Passwords are hashed with BCrypt before storage; they are never returned in any API response.
- File uploads are validated for size (max 5MB) and content type (JPEG/PNG only) before being written to disk.
- Contact IDs are UUIDs, validated against a strict regex before being used to construct any request URL, to prevent malformed or malicious input from reaching the backend.
