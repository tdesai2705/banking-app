# Banking Application

A Spring Boot REST API for managing bank accounts with deposit and withdrawal operations.

## Features

- Create and manage bank accounts
- Deposit and withdraw funds
- View account balances
- Input validation and error handling
- H2 in-memory database
- Comprehensive test coverage

## Tech Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database
- Maven
- Docker

## API Endpoints

- `GET /api/accounts` - Get all accounts
- `GET /api/accounts/{id}` - Get account by ID
- `POST /api/accounts` - Create new account
- `POST /api/accounts/{id}/deposit` - Deposit funds
- `POST /api/accounts/{id}/withdraw` - Withdraw funds
- `DELETE /api/accounts/{id}` - Delete account

## Running Locally

```bash
# Build and run with Maven
mvn spring-boot:run

# Or build and run with Docker
docker build -t banking-app .
docker run -p 8080:8080 banking-app
```

## Running Tests

```bash
mvn test
```

## CloudBees CI/CD

This project includes:
- Custom CloudBees action for running tests (`.cloudbees/actions/run-tests`)
- Reusable workflow for building (`.cloudbees/workflows/reusable-build.yaml`)
- Main CI pipeline (`.cloudbees/workflows/ci.yaml`)

The CI pipeline runs on push to main/develop and on pull requests to main.

<!-- Smart Tests integration with correct API token - Thu Apr  9 20:05:24 IST 2026 -->
# Trigger CI after artifact registration removal
