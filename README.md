# SkyLink

SkyLink is a full-stack flight booking and travel management platform built with React, Vite, Spring Boot, MySQL, and JWT authentication. The project simulates a modern airline booking system where users can search flights, register or log in, select fares and seats, create bookings, manage their trips, track flight status, explore offers, and use travel support tools from a single web application.

Repository: [https://github.com/tausifalam6879/SkyLink](https://github.com/tausifalam6879/SkyLink)

## Live Demo

A public hosted demo is not configured yet. The project can be run as a live local demo with the frontend and backend running together:

- Frontend local demo: `http://localhost:5173`
- Backend API: `http://localhost:8081`
- API base URL used by the frontend: `http://localhost:8081/api`

Quick start:

```powershell
# Terminal 1: start the backend
cd services/skylink-api
$env:SKYLINK_DB_URL="jdbc:mysql://localhost:3306/skylink_db"
$env:SKYLINK_DB_USERNAME="root"
$env:SKYLINK_DB_PASSWORD="your_mysql_password"
$env:SKYLINK_MAIL_USERNAME="your_email@gmail.com"
$env:SKYLINK_MAIL_PASSWORD="your_gmail_app_password"
$env:SKYLINK_JWT_SECRET="replace_with_a_long_random_secret_at_least_64_characters"
.\mvnw.cmd spring-boot:run

# Terminal 2: start the frontend
cd skylink-web
$env:VITE_API_BASE_URL="http://localhost:8081/api"
npm install
npm run dev
```

For a hosted live demo, deploy `skylink-web` to Vercel, Netlify, or GitHub Pages, deploy `services/skylink-api` to Render, Railway, AWS, Azure, or another Java hosting provider, and connect the API to a managed MySQL database. Set `VITE_API_BASE_URL` to the deployed API URL.

## What This Project Does

SkyLink models the core flow of an airline booking platform:

1. Visitors search for flights by route, travel date, passenger count, and fare preferences.
2. Users create an account or log in with email/password or OTP-based flows.
3. Authenticated users select flights, fare classes, passenger details, and seats.
4. The backend validates available seats and fares before confirming a booking.
5. Users can view, manage, and cancel their bookings from protected account screens.
6. Supporting pages provide offers, fare alerts, group booking, trip planning, credit card information, and flight status tracking.

## Main Features

- User registration and login
- JWT-based stateless authentication
- OTP send and verify flows
- Forgot password and reset password flows
- Protected user dashboard
- Flight search and route lookup
- Airport search by city, airport name, IATA, ICAO, and nearby coordinates
- Aircraft, route, schedule, and fare management APIs
- Seat map availability and seat booking
- Booking creation with passenger details
- Booking history and booking cancellation
- Fare class support
- Offer listing and offer detail pages
- Flight status page
- Group booking request page
- Trip planner page
- Fare alerts page
- Travel credit card page
- Seed/import support for global airport, airline, aircraft, route, and schedule data

## Tech Stack

| Layer | Tools and Languages |
| --- | --- |
| Frontend | React 19, JavaScript, Vite, React Router, Axios, Lucide React, CSS |
| Backend | Java 21, Spring Boot 3.5, Spring Web, Spring Security, Spring Data JPA, Spring Mail, Bean Validation |
| Database | MySQL |
| Authentication | JWT, BCrypt password hashing, OTP workflows |
| Build Tools | npm, Vite, Maven Wrapper |
| Developer Tools | Git, GitHub, ESLint, Postman-ready API structure |

## Architecture

```text
Browser / React UI
        |
        | Axios HTTP requests with optional JWT token
        v
Spring Boot REST API
        |
        | Controllers -> Services -> Repositories
        v
MySQL Database
```

Frontend responsibilities:

- Render user-facing pages and workflows.
- Store and attach JWT tokens through the shared Axios client.
- Redirect users to login when protected requests return `401`.
- Call backend APIs for authentication, flight search, bookings, seats, and profiles.

Backend responsibilities:

- Validate requests.
- Authenticate users.
- Issue and verify JWT tokens.
- Enforce protected routes.
- Manage flight, fare, seat, passenger, and booking data.
- Send OTP and password reset email flows through SMTP.

## Project Structure

```text
SkyLink/
|-- README.md
|-- docs/
|   `-- PROJECT_DOCUMENTATION.md
|-- skylink-web/
|   |-- src/
|   |   |-- api/
|   |   |-- data/
|   |   |-- pages/
|   |   |-- utils/
|   |   |-- App.jsx
|   |   `-- main.jsx
|   |-- public/
|   |-- package.json
|   `-- README.md
`-- services/
    `-- skylink-api/
        |-- src/main/java/com/skylink/
        |   |-- config/
        |   |-- controller/
        |   |-- dto/
        |   |-- entity/
        |   |-- exception/
        |   |-- filter/
        |   |-- importer/
        |   |-- repository/
        |   |-- service/
        |   `-- util/
        |-- src/main/resources/
        |-- pom.xml
        `-- README.md
```

## Frontend Pages

| Route | Purpose |
| --- | --- |
| `/` and `/flights` | Home page and flight search experience |
| `/login` | User login |
| `/register` | New account registration |
| `/booking` | Protected booking flow |
| `/my-bookings` and `/bookings` | Protected booking management |
| `/user` and `/account` | Protected user dashboard |
| `/flight-status` | Flight tracking page |
| `/group-booking` and `/support/group` | Group travel request page |
| `/plan` | Trip planner page |
| `/offers` | Offer listing |
| `/offers/:offerSlug` | Offer details |
| `/travel-credit-card` | Travel credit card page |
| `/fare-alerts` | Fare alert page |

## Backend API Overview

Primary API areas:

- Auth: `/api/auth`
- Users: `/api/users`
- OTP: `/api/otp`
- Airports: `/api/airports`
- Aircraft: `/api/aircraft`
- Flight routes: `/api/flight-routes`
- Flight schedules: `/api/flight-schedules`
- Flight fares: `/api/flight-fares`
- Flight search: `/api/flights`
- Seats: `/api/seats`
- Bookings: `/api/bookings`

Common endpoints:

```text
POST /api/users/register
POST /api/auth/login
POST /api/auth/login/otp/send
POST /api/auth/login/otp/verify
POST /api/auth/forgot-password
POST /api/auth/reset-password
GET  /api/users/me
GET  /api/airports/search
GET  /api/airports/iata/{iataCode}
GET  /api/airports/nearby
POST /api/flights/search
GET  /api/flight-schedules
GET  /api/flight-schedules/search
GET  /api/flight-fares/flight/{flightScheduleId}
GET  /api/seats
POST /api/bookings
GET  /api/bookings/my
GET  /api/bookings/{bookingReference}
POST /api/bookings/{bookingReference}/cancel
```

## Prerequisites

- Node.js 20 or newer
- npm
- Java 21
- MySQL 8 or compatible
- Git

The backend includes the Maven Wrapper, so a separate Maven installation is optional.

## Database Setup

Create a local MySQL database:

```sql
CREATE DATABASE skylink_db;
```

The backend uses Hibernate with `ddl-auto: update`, so entity tables can be created or updated automatically during development.

## Backend Setup

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

Required backend environment variables:

| Variable | Description |
| --- | --- |
| `SKYLINK_DB_URL` | JDBC URL for the MySQL database |
| `SKYLINK_DB_USERNAME` | MySQL username |
| `SKYLINK_DB_PASSWORD` | MySQL password |
| `SKYLINK_MAIL_USERNAME` | SMTP email username |
| `SKYLINK_MAIL_PASSWORD` | SMTP email or Gmail app password |
| `SKYLINK_JWT_SECRET` | Long random secret used to sign JWT tokens |

## Frontend Setup

```powershell
cd skylink-web
npm install
$env:VITE_API_BASE_URL="http://localhost:8081/api"
npm run dev
```

Production build:

```powershell
npm run build
npm run preview
```

## How Authentication Works

1. A user registers or logs in through the React frontend.
2. The backend validates credentials and returns a JWT.
3. The frontend stores the JWT through the auth utility.
4. The shared Axios client attaches the JWT as `Authorization: Bearer <token>`.
5. Spring Security and `JwtAuthenticationFilter` validate the token.
6. Protected routes such as bookings and user profile APIs require a valid authenticated user.

## How Booking Works

1. The user searches flights from the frontend.
2. The backend returns matching schedules and fares.
3. The user selects a fare class, passenger details, and seat numbers.
4. `BookingService` validates the selected flight schedule, fare availability, passenger list, and selected seats.
5. The system creates a booking reference, saves passengers, marks seats as booked, and reduces fare seat availability.
6. Cancellation releases seats and increases available fare inventory again.

## Security Notes

- Real credentials must never be committed.
- Database password, mail password, and JWT secret are loaded through environment variables.
- If any real credential was ever shared or pushed publicly, rotate it immediately.
- Production deployments should replace wildcard controller CORS annotations with strict trusted origins.
- Use HTTPS in production.

## Testing and Verification

Frontend build:

```powershell
cd skylink-web
npm run build
```

Backend tests:

```powershell
cd services/skylink-api
.\mvnw.cmd test
```

Note: backend tests require valid database configuration because the current Spring Boot context loads JPA and MySQL configuration.

## More Documentation

Read the detailed project documentation here:

- [Full Project Documentation](docs/PROJECT_DOCUMENTATION.md)
- [Frontend Documentation](skylink-web/README.md)
- [Backend Documentation](services/skylink-api/README.md)

## License

No license file has been added yet. Add a license before using this project in production or distributing it publicly.
