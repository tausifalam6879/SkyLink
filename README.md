# SkyLink

Full-stack airline booking and travel management platform built with React, Vite, Spring Boot, MySQL, JWT authentication, OTP account flows, flight search, seat selection, booking management, airport data import, and travel support pages.

[![Frontend](https://img.shields.io/badge/Frontend-React%20%2B%20Vite-61DAFB?style=for-the-badge&logo=react&logoColor=black)](skylink-web)
[![Backend](https://img.shields.io/badge/Backend-Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](services/skylink-api)
[![Database](https://img.shields.io/badge/Database-MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](#database-setup)
[![Auth](https://img.shields.io/badge/Auth-JWT%20%2B%20OTP-111827?style=for-the-badge)](#authentication-flow)

Repository: [https://github.com/tausifalam6879/SkyLink](https://github.com/tausifalam6879/SkyLink)

Live demo: [https://tausifalam6879.github.io/SkyLink/](https://tausifalam6879.github.io/SkyLink/)

The live demo is a GitHub Pages frontend preview. Static pages, navigation, offers, fare alerts, trip planning, group booking UI, flight status UI, and the core interface can be viewed directly in the browser. Dynamic operations such as real login, OTP, flight search from the database, seat locking, and booking creation require the Spring Boot API and MySQL database to be running locally or deployed separately.

## What This Project Does

SkyLink simulates a modern airline booking platform. It gives users a travel website experience where they can search routes, compare fare options, create an account, log in securely, choose seats, book flights, manage bookings, track flight status, explore bank/travel offers, request group travel, plan trips, and manage their account from protected pages.

On the backend, SkyLink models realistic airline data with airports, aircraft, routes, flight schedules, fare classes, seats, passengers, users, OTP verification records, and booking records. The API follows a layered Spring Boot structure with controllers, DTOs, services, repositories, entities, security filters, and global exception handling.

## Key Features

- React/Vite airline frontend with polished travel pages and route-based navigation.
- JWT-based login and protected frontend routes.
- Secure registration flow with BCrypt password hashing.
- OTP send and verify endpoints for account and login flows.
- Forgot password and reset password backend support.
- User dashboard with authenticated profile data.
- Flight search flow connected to backend APIs.
- Airport search by airport name, city, IATA code, ICAO code, and nearby location.
- Flight route, aircraft, flight schedule, and fare APIs.
- Fare class support for economy, premium economy, business, and first class style booking flows.
- Seat map and seat availability APIs.
- Booking creation with passenger details, selected seats, fare validation, and generated booking reference.
- Booking history for authenticated users.
- Booking cancellation that releases seats and restores fare availability.
- Offer listing and detailed offer pages.
- Flight status page for travel tracking UI.
- Fare alerts page with local saved alerts.
- Group booking request page with local request persistence.
- Trip planner page for itinerary-style planning.
- Travel credit card page.
- Importer and seeder classes for airports, aircraft, routes, schedules, and seats.
- Environment-variable based secret management.
- GitHub Pages deployment workflow for frontend preview.
- Detailed setup and project documentation in `docs/`.

## Live Demo

Demo URL:

[https://tausifalam6879.github.io/SkyLink/](https://tausifalam6879.github.io/SkyLink/)

What works in the hosted frontend preview:

- Home page and navigation.
- Offers and offer details.
- Fare alert UI.
- Group booking UI.
- Trip planner UI.
- Credit card page.
- Flight status UI.
- Login/register screens as frontend pages.

What needs the backend:

- Real user registration.
- Real login and JWT token creation.
- OTP email delivery and verification.
- Database-backed airport search.
- Database-backed flight search.
- Seat availability.
- Booking creation.
- Booking history.
- Booking cancellation.
- User profile API.

For the complete working system, run the backend and frontend locally using the quick start below.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Frontend | React 19, JavaScript, Vite, React Router, Axios, Lucide React, CSS |
| Backend | Java 21, Spring Boot 3.5, Spring Web, Spring Security, Spring Data JPA, Validation, Spring Mail |
| Database | MySQL, Hibernate ORM |
| Authentication | JWT, BCrypt, OTP workflow |
| Data Import | Apache Commons CSV, bundled airport/airline/aircraft/route data |
| Build Tools | npm, Vite, Maven Wrapper |
| Dev Tools | Git, GitHub, ESLint, REST API clients such as Postman |
| Deployment | GitHub Pages for frontend preview, Java hosting for backend |

## Architecture

```text
User Browser
    |
    | React pages, protected routes, Axios requests
    v
SkyLink Web App
    |
    | JSON over HTTP with optional Authorization: Bearer <token>
    v
Spring Boot REST API
    |
    | Controller -> Service -> Repository
    v
MySQL Database
```

### Frontend Responsibilities

- Render public and protected travel pages.
- Manage browser routing through React Router.
- Store JWT auth state through local auth utilities.
- Attach JWT tokens through the shared Axios client.
- Redirect unauthorized users to the login page.
- Provide booking, dashboard, offers, trip planning, and support interfaces.

### Backend Responsibilities

- Validate API requests.
- Register and authenticate users.
- Hash passwords with BCrypt.
- Generate and validate JWT tokens.
- Handle OTP send and verify flows.
- Manage airports, aircraft, routes, schedules, fares, seats, bookings, and passengers.
- Protect authenticated endpoints through Spring Security.
- Keep booking inventory consistent by marking seats and updating fare availability.

## Project Structure

```text
SkyLink/
|-- .github/
|   `-- workflows/
|       `-- deploy-frontend.yml
|-- docs/
|   `-- PROJECT_DOCUMENTATION.md
|-- skylink-web/
|   |-- public/
|   |-- src/
|   |   |-- api/              Axios API client
|   |   |-- data/             Static offer data
|   |   |-- pages/            React route pages
|   |   |-- utils/            Auth helpers
|   |   |-- App.jsx           Route definitions
|   |   `-- main.jsx          React entry point
|   |-- package.json
|   |-- vite.config.js
|   `-- README.md
`-- services/
    `-- skylink-api/
        |-- src/main/java/com/skylink/
        |   |-- config/       Security and application config
        |   |-- controller/   REST controllers
        |   |-- dto/          Request and response DTOs
        |   |-- entity/       JPA entities
        |   |-- exception/    Global exception handling
        |   |-- filter/       JWT authentication filter
        |   |-- importer/     Data seed/import helpers
        |   |-- repository/   Spring Data JPA repositories
        |   |-- service/      Business logic
        |   `-- util/         JWT utility
        |-- src/main/resources/
        |   |-- application.yaml
        |   `-- data/         Airports, airlines, aircraft, routes data
        |-- pom.xml
        `-- README.md
```

## Application Pages

| Route | Page |
| --- | --- |
| `/` | Home page with flight search and travel modules |
| `/flights` | Flight search alias |
| `/login` | Login page |
| `/register` | Registration page |
| `/booking` | Protected booking flow |
| `/my-bookings` | Protected booking history |
| `/bookings` | Booking history alias |
| `/user` | Protected user dashboard |
| `/account` | User dashboard alias |
| `/flight-status` | Flight tracking page |
| `/group-booking` | Group travel request |
| `/support/group` | Group travel alias |
| `/plan` | Trip planner |
| `/offers` | Offer listing |
| `/offers/:offerSlug` | Offer details |
| `/travel-credit-card` | Travel credit card page |
| `/fare-alerts` | Fare alerts page |

## Backend Modules

| Module | Purpose |
| --- | --- |
| Auth | Login, OTP login, forgot password, reset password |
| Users | Registration, current user profile |
| Airports | Search and lookup airport data |
| Aircraft | Aircraft creation and lookup |
| Flight Routes | Connect source and destination airports |
| Flight Schedules | Manage scheduled flights |
| Flight Fares | Manage fare classes, base fares, and availability |
| Seats | Check and update seat availability |
| Bookings | Create, fetch, and cancel bookings |
| Importers | Seed airport, aircraft, route, schedule, and seat data |

## Authentication Flow

```text
User submits login/register request
        |
        v
Spring Boot validates credentials
        |
        v
JWT token is generated
        |
        v
React stores auth token
        |
        v
Axios attaches Authorization header
        |
        v
JwtAuthenticationFilter validates token
        |
        v
Protected endpoint returns data
```

## Booking Flow

```text
Search flights
    |
Select flight schedule and fare class
    |
Enter passenger details and seat numbers
    |
Submit booking request
    |
Validate logged-in user
    |
Validate schedule, fare, seats, and passenger count
    |
Create booking reference
    |
Save booking and passengers
    |
Mark selected seats as booked
    |
Reduce fare availability
    |
Return booking confirmation
```

## Quick Start

### 1. Clone the Repository

```powershell
git clone https://github.com/tausifalam6879/SkyLink.git
cd SkyLink
```

### 2. Create the Database

```sql
CREATE DATABASE skylink_db;
```

### 3. Start the Backend

```powershell
cd services/skylink-api
$env:SKYLINK_DB_URL="jdbc:mysql://localhost:3306/skylink_db"
$env:SKYLINK_DB_USERNAME="root"
$env:SKYLINK_DB_PASSWORD="your_mysql_password"
$env:SKYLINK_MAIL_USERNAME="your_email@gmail.com"
$env:SKYLINK_MAIL_PASSWORD="your_gmail_app_password"
$env:SKYLINK_JWT_SECRET="replace_with_a_long_random_secret_at_least_64_characters"
.\mvnw.cmd spring-boot:run
```

Backend runs on:

```text
http://localhost:8081
```

Basic test endpoints:

```text
GET http://localhost:8081/
GET http://localhost:8081/test
```

### 4. Start the Frontend

```powershell
cd skylink-web
npm install
$env:VITE_API_BASE_URL="http://localhost:8081/api"
npm run dev
```

Frontend runs on:

```text
http://localhost:5173
```

## Configuration

### Backend Environment Variables

| Variable | Purpose |
| --- | --- |
| `SKYLINK_DB_URL` | MySQL JDBC URL, for example `jdbc:mysql://localhost:3306/skylink_db` |
| `SKYLINK_DB_USERNAME` | MySQL username |
| `SKYLINK_DB_PASSWORD` | MySQL password |
| `SKYLINK_MAIL_USERNAME` | SMTP email username |
| `SKYLINK_MAIL_PASSWORD` | SMTP email app password |
| `SKYLINK_JWT_SECRET` | Long random JWT signing secret |

### Frontend Environment Variables

| Variable | Purpose |
| --- | --- |
| `VITE_API_BASE_URL` | Backend API URL. Default is `http://localhost:8081/api` |
| `VITE_BASE_PATH` | Optional Vite base path. Used by GitHub Pages as `/SkyLink/` |

## API Overview

### Auth and Users

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/users/register` | Register a user |
| `POST` | `/api/auth/login` | Login with email and password |
| `POST` | `/api/auth/login/otp/send` | Send login OTP |
| `POST` | `/api/auth/login/otp/verify` | Verify login OTP |
| `POST` | `/api/auth/forgot-password` | Request password reset |
| `POST` | `/api/auth/reset-password` | Reset password |
| `GET` | `/api/users/me` | Get current authenticated user |

### Airports and Flight Search

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/airports/search` | Search airports |
| `GET` | `/api/airports/iata/{iataCode}` | Find airport by IATA code |
| `GET` | `/api/airports/icao/{icaoCode}` | Find airport by ICAO code |
| `GET` | `/api/airports/nearby` | Find nearby airports |
| `POST` | `/api/flights/search` | Search flights |

### Flight Inventory

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/aircraft` | Create aircraft |
| `GET` | `/api/aircraft` | List aircraft |
| `GET` | `/api/aircraft/active` | List active aircraft |
| `POST` | `/api/flight-routes` | Create flight route |
| `POST` | `/api/flight-routes/resolve` | Resolve route |
| `GET` | `/api/flight-routes` | List routes |
| `POST` | `/api/flight-schedules` | Create schedule |
| `GET` | `/api/flight-schedules` | List schedules |
| `GET` | `/api/flight-schedules/search` | Search schedules |
| `POST` | `/api/flight-fares` | Create fare |
| `GET` | `/api/flight-fares` | List fares |
| `GET` | `/api/seats` | Get seat availability |

### Bookings

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/bookings` | Create booking |
| `GET` | `/api/bookings/my` | Get current user's bookings |
| `GET` | `/api/bookings/{bookingReference}` | Get booking details |
| `POST` | `/api/bookings/{bookingReference}/cancel` | Cancel booking |

## Database Model

Important entities:

- `User`
- `OtpVerification`
- `Airport`
- `Aircraft`
- `FlightRoute`
- `FlightSchedule`
- `FlightFare`
- `Seat`
- `Booking`
- `Passenger`

Relationship summary:

```text
User 1 -> many Bookings
Booking 1 -> many Passengers
Booking many -> 1 FlightSchedule
Booking many -> 1 FlightFare
FlightSchedule many -> 1 FlightRoute
FlightSchedule many -> 1 Aircraft
FlightRoute many -> 1 Source Airport
FlightRoute many -> 1 Destination Airport
FlightSchedule 1 -> many Seats
```

## Verification Commands

Frontend:

```powershell
cd skylink-web
npm run build
npm run lint
```

Backend:

```powershell
cd services/skylink-api
.\mvnw.cmd test
```

Note: backend tests currently load the Spring Boot context with JPA/MySQL configuration, so valid database access is required.

## Deployment

### Frontend

The repository includes a GitHub Actions workflow for deploying the Vite frontend to GitHub Pages:

```text
.github/workflows/deploy-frontend.yml
```

Frontend deployment URL:

```text
https://tausifalam6879.github.io/SkyLink/
```

### Backend

Deploy the Spring Boot API to a Java 21 compatible platform such as:

- Render
- Railway
- AWS Elastic Beanstalk
- Azure App Service
- DigitalOcean App Platform

Use a managed MySQL database and configure all required environment variables in the hosting dashboard.

## Documentation

- [Full Project Documentation](docs/PROJECT_DOCUMENTATION.md)
- [Frontend Documentation](skylink-web/README.md)
- [Backend Documentation](services/skylink-api/README.md)

## Security Notes

- Do not commit real database passwords, SMTP passwords, JWT secrets, or `.env` files.
- Credentials are loaded through environment variables.
- Rotate any credential that was ever committed or shared publicly.
- Use HTTPS in production.
- Restrict CORS origins before production deployment.
- Use strong JWT secrets.
- Add rate limiting for login and OTP endpoints before public production use.

## Current Status

Implemented:

- React frontend pages and routing.
- Spring Boot API structure.
- JWT security filter.
- User registration and authentication APIs.
- OTP-related APIs.
- Airport, aircraft, route, schedule, fare, seat, and booking modules.
- Booking creation and cancellation logic.
- Documentation and GitHub Pages frontend deployment workflow.

Still recommended before production:

- Deploy backend and database publicly.
- Add admin dashboard for managing flight inventory.
- Add payment gateway integration.
- Add ticket PDF/email confirmation.
- Add CI tests with a dedicated test database or Testcontainers.
- Harden CORS and rate limiting.

## About

SkyLink is a portfolio-ready full-stack project that demonstrates airline booking workflows, protected user features, Spring Boot API design, relational data modeling, and React frontend development.

## License

No license file has been added yet. Add a license before using this project in production or distributing it publicly.
