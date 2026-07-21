# SkyLink

SkyLink is a full-stack flight booking project I built to work through the parts of an airline application that are easy to underestimate: airport search, flight inventory, authentication, seat availability, booking consistency, and cancellation.

The UI is a React application and the API is a Spring Boot service backed by MySQL. A browser-only demo is also available so the main journey can be tried without running the backend.

[![Frontend](https://img.shields.io/badge/Frontend-React%20%2B%20Vite-61DAFB?style=flat-square&logo=react&logoColor=black)](skylink-web)
[![Backend](https://img.shields.io/badge/Backend-Spring%20Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white)](services/skylink-api)
[![Database](https://img.shields.io/badge/Database-MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)](#run-the-full-stack-locally)

- Live frontend demo: [tausifalam6879.github.io/SkyLink](https://tausifalam6879.github.io/SkyLink/)
- Detailed developer notes: [docs/PROJECT_DOCUMENTATION.md](docs/PROJECT_DOCUMENTATION.md)

## What I focused on

The project started as a flight-search interface and grew into a complete booking flow. The most interesting implementation work is in four areas:

- searching a large airport dataset by city, airport name, IATA code, or ICAO code;
- generating schedules, fares, and seat maps when a route has no inventory for the requested date;
- supporting password and email-OTP authentication with stateless JWT security;
- creating and cancelling bookings without losing track of seat or fare availability.

The backend follows a conventional controller-service-repository structure. I kept request/response DTOs separate from JPA entities so that the API contract does not depend directly on the database model.

## Hosted demo and local backend

The GitHub Pages deployment cannot run Java or MySQL. For that reason, the hosted version uses an Axios demo adapter that reproduces the main frontend journey inside the browser.

| Area | Hosted demo | Full local setup |
| --- | --- | --- |
| Airport search | Static airport JSON | MySQL airport data |
| Flight search | Generated sample flights | Spring Boot search and schedule generation |
| Login and OTP | Demo credentials; any OTP | BCrypt, signed JWT, and SMTP email |
| Seats | Browser-generated seat map | Database-backed seat inventory |
| Bookings | Saved in `localStorage` | Saved in MySQL for the authenticated user |
| Cancellation | Updates browser data | Releases seats and restores fare inventory |

The demo is useful for reviewing the UI. The local setup is the actual client-server implementation.

## Main user flow

1. Search for departure and destination airports.
2. Choose a travel date and search for flights.
3. Compare available fare classes and select a flight.
4. Log in with a password or email OTP.
5. Choose a seat and enter passenger details.
6. Create the booking and receive a booking reference.
7. Review or cancel the booking from **My Bookings**.

The application also contains offers, fare-alert, group-booking, trip-planner, flight-status, and travel-card screens. These supporting screens are currently frontend features; their server-side workflows are listed under [Current boundaries](#current-boundaries).

## Technology

| Layer | Tools |
| --- | --- |
| Frontend | React 19, JavaScript, Vite, React Router, Axios, Lucide React, CSS |
| Backend | Java 21, Spring Boot 3.5, Spring Web, Spring Security, Spring Data JPA |
| Authentication | BCrypt, JWT, email OTP, Spring Mail |
| Database | MySQL with Hibernate ORM |
| Data import | Apache Commons CSV, OurAirports data, OpenFlights data |
| Tests | JUnit, Spring Boot Test, H2 test profile, ESLint |
| Deployment | GitHub Actions and GitHub Pages for the frontend demo |

## Architecture

```text
React page
    |
    | Axios request (Bearer token when logged in)
    v
Spring Security / JWT filter
    |
    v
REST controller
    |
    v
Service and transaction boundary
    |
    v
Spring Data repository
    |
    v
MySQL
```

The frontend and backend are separate applications. They can be developed and deployed independently as long as `VITE_API_BASE_URL` points to the API.

## Repository layout

```text
SkyLink/
|-- .github/workflows/          GitHub Pages deployment
|-- docs/                       Developer notes
|-- scripts/                    Airport-data conversion utility
|-- skylink-web/                React/Vite frontend
`-- services/skylink-api/       Spring Boot REST API
```

Important frontend files:

- `src/App.jsx` defines public and protected routes.
- `src/api/axiosConfig.js` configures the API URL, JWT interceptor, and demo adapter.
- `src/api/demoApi.js` implements the browser-only demo.
- `src/pages/HomePage.jsx` contains airport and flight search.
- `src/pages/BookingPage.jsx` contains seat selection and booking creation.
- `src/utils/auth.js` stores and clears the browser auth state.

Important backend packages:

- `controller` exposes the REST endpoints;
- `service` contains business rules and transaction boundaries;
- `repository` contains Spring Data JPA queries;
- `entity` maps the relational model;
- `dto` defines request and response contracts;
- `filter`, `config`, and `util` implement JWT security;
- `importer` loads airports, routes, aircraft, schedules, and seats.

## How flight search works

`POST /api/flights/search` accepts a source IATA code, destination IATA code, and travel date.

The service first looks for active scheduled flights for that route and date. If none exist, it resolves the route, finds an available SkyLink aircraft, creates a small set of schedules, calculates fares, and generates a seat map. Schedule frequency is based on route distance and whether the journey is domestic.

The economy fare currently uses this project formula:

```text
max(2200, 1500 + distanceKm * 4.25)
```

Business and first-class fares are calculated from the economy fare. This is sample pricing logic rather than a real airline revenue-management model.

## How booking consistency is handled

Booking creation runs inside a Spring transaction. Before changing inventory, the service locks the selected `FlightFare` row with `PESSIMISTIC_WRITE`. It then validates the passenger count and seats, creates the booking and passengers, marks the seats as booked, and reduces the available-fare count.

Cancellation performs the reverse operation: it checks that the booking belongs to the logged-in user, releases passenger seats, restores fare availability, and marks the booking as cancelled.

The fare lock serializes bookings within the same flight and fare class. A larger production system would normally add short-lived seat holds, idempotency keys, and more focused concurrency tests.

## Authentication

The API supports:

- registration with a BCrypt-hashed password;
- registration after email OTP verification;
- password login;
- email OTP login;
- forgot-password and reset-password flows;
- authenticated profile and booking endpoints.

On login, the API signs a JWT whose subject is the user's email. The frontend stores the token and the shared Axios client adds it to later requests. `JwtAuthenticationFilter` validates the token before protected controllers run.

## Run the full stack locally

### Requirements

- Node.js 20 or newer
- Java 21 or newer
- MySQL 8 or a compatible MySQL server

### 1. Create the database

```sql
CREATE DATABASE skylink_db;
```

### 2. Start the API

From PowerShell:

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

The API listens on `http://localhost:8081`.

### 3. Start the web app

In a second terminal:

```powershell
cd skylink-web
npm install
$env:VITE_API_BASE_URL="http://localhost:8081/api"
npm run dev
```

Vite normally starts at `http://localhost:5173`.

## API map

Authentication and profile:

| Method | Endpoint |
| --- | --- |
| `POST` | `/api/users/register` |
| `POST` | `/api/auth/login` |
| `POST` | `/api/auth/login/otp/send` |
| `POST` | `/api/auth/login/otp/verify` |
| `POST` | `/api/auth/forgot-password` |
| `POST` | `/api/auth/reset-password` |
| `GET` | `/api/users/me` |

Search and inventory:

| Method | Endpoint |
| --- | --- |
| `GET` | `/api/airports/search?query=...` |
| `GET` | `/api/airports/iata/{iataCode}` |
| `GET` | `/api/airports/icao/{icaoCode}` |
| `GET` | `/api/airports/nearby?latitude=...&longitude=...` |
| `POST` | `/api/flights/search` |
| `GET` | `/api/seats/flight/{flightScheduleId}` |
| `GET` | `/api/seats/flight/{flightScheduleId}/class/{fareClass}` |
| `GET` | `/api/seats/flight/{flightScheduleId}/class/{fareClass}/available-count` |

Bookings:

| Method | Endpoint |
| --- | --- |
| `POST` | `/api/bookings` |
| `GET` | `/api/bookings/my` |
| `GET` | `/api/bookings/{bookingReference}` |
| `POST` | `/api/bookings/{bookingReference}/cancel` |

Aircraft, route, schedule, and fare management endpoints are also present under `/api/aircraft`, `/api/flight-routes`, `/api/flight-schedules`, and `/api/flight-fares`. They currently require authentication but do not yet have a separate admin role policy.

## Data sources

The project includes two external aviation datasets:

- Airport data follows the [OurAirports](https://ourairports.com/data/) CSV format. OurAirports publishes its downloadable airport data in the public domain.
- Airline, aircraft-type, and route files follow the [OpenFlights](https://openflights.org/data.php) dataset format and remain subject to the licensing terms published by OpenFlights.

`scripts/generate-frontend-airports.mjs` creates the smaller JSON dataset used by the hosted demo. Dataset records are not presented as original SkyLink data.

## Verification

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

The current backend test uses an H2 in-memory database and checks that the Spring application context starts. It is a smoke test, not full booking-flow coverage.

## Current boundaries

The repository is a working project, but it is not presented as a production airline system. The current boundaries are:

- the booking page submits one passenger even though traveller count is available in search;
- round-trip selection stores a return date but only the outbound flight is searched;
- the real backend supports `ECONOMY`, `BUSINESS`, and `FIRST_CLASS`; the hosted demo also shows a premium-economy option;
- flight status, trip planning, group-booking requests, and fare alerts are frontend demonstrations;
- there is no payment, refund, ticket PDF, or ticket-confirmation email workflow;
- inventory-management endpoints need admin authorization before public deployment;
- JWT refresh, OTP hardening, rate limiting, and database migrations are still to be added;
- automated coverage currently consists of frontend lint/build checks and a backend context smoke test.

These are the next areas I would work on before treating SkyLink as a deployable service rather than a learning and demonstration project.

## License

No open-source license has been selected for the SkyLink source code. Third-party datasets remain subject to the terms stated by their respective publishers.
