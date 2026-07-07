# SkyLink Project Documentation

## 1. Project Summary

SkyLink is a full-stack airline booking and travel management platform. It is designed as a practical web application that demonstrates how a real flight booking system can be structured across a modern frontend, secure backend API, relational database, and authenticated user workflows.

The application allows users to explore flights, authenticate, book tickets, select seats, manage bookings, view travel offers, track flight status, request group travel, plan trips, and manage account information. The backend exposes REST APIs for domain objects such as users, airports, aircraft, routes, flight schedules, fares, seats, passengers, and bookings.

## 2. Project Goals

The project is built to demonstrate:

- A realistic full-stack airline booking workflow.
- React-based frontend routing and protected pages.
- Spring Boot REST API design.
- JWT-based stateless authentication.
- Relational data modeling with Spring Data JPA and MySQL.
- Booking inventory handling for seats and fare availability.
- Email/OTP-style authentication support.
- Clean separation between controllers, services, repositories, DTOs, and entities.
- Environment-based secret management.

## 3. Live Demo

### Local Live Demo

The project currently supports a local live demo.

Backend:

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

Frontend:

```powershell
cd skylink-web
npm install
$env:VITE_API_BASE_URL="http://localhost:8081/api"
npm run dev
```

Open:

```text
http://localhost:5173
```

### Hosted Demo Deployment Plan

A hosted demo can be created with this deployment setup:

- Frontend: Vercel, Netlify, or GitHub Pages.
- Backend: Render, Railway, AWS Elastic Beanstalk, Azure App Service, or any Java 21 compatible host.
- Database: Managed MySQL from Railway, PlanetScale, AWS RDS, Azure Database for MySQL, or another MySQL-compatible provider.
- Environment variables: configure all backend secrets on the hosting provider and set `VITE_API_BASE_URL` for the frontend build.

## 4. Technology Stack

### Frontend

- JavaScript
- React 19
- Vite
- React Router
- Axios
- Lucide React
- CSS
- ESLint

### Backend

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Security
- Spring Data JPA
- Bean Validation
- Spring Mail
- Lombok
- JJWT
- Apache Commons CSV

### Database

- MySQL
- Hibernate ORM

### Development and Build Tools

- Git
- GitHub
- npm
- Maven Wrapper
- Postman-compatible REST APIs

## 5. High-Level Architecture

```text
User Browser
    |
    | React pages, protected routes, Axios API calls
    v
SkyLink Web App
    |
    | JSON over HTTP
    v
Spring Boot REST API
    |
    | Controller -> Service -> Repository
    v
MySQL Database
```

The frontend and backend are separated into independent applications:

- `skylink-web` contains the browser UI.
- `services/skylink-api` contains the backend REST API.

This separation allows independent development, testing, and deployment.

## 6. Frontend Documentation

### Frontend Responsibilities

The frontend is responsible for:

- Rendering the complete user interface.
- Managing browser routes.
- Protecting pages that require login.
- Calling backend APIs through Axios.
- Storing and attaching JWT tokens.
- Redirecting unauthenticated users to login.
- Presenting flight search, booking, account, offers, and support workflows.

### Important Frontend Folders

```text
skylink-web/src/
|-- api/
|   `-- axiosConfig.js
|-- data/
|   `-- offerData.js
|-- pages/
|-- utils/
|   `-- auth.js
|-- App.jsx
`-- main.jsx
```

### Frontend Routing

The application uses React Router. Public pages include home, login, register, offers, flight status, group booking, trip planner, fare alerts, and credit card pages. Protected pages include booking, my bookings, and user dashboard.

Important routes:

| Route | Description |
| --- | --- |
| `/` | Main home and flight search page |
| `/flights` | Flight search alias |
| `/login` | User login |
| `/register` | User registration |
| `/booking` | Protected booking page |
| `/my-bookings` | Protected user booking history |
| `/bookings` | Protected booking history alias |
| `/user` | Protected user dashboard |
| `/account` | Protected account alias |
| `/flight-status` | Flight status tracking |
| `/group-booking` | Group travel request |
| `/support/group` | Group travel alias |
| `/plan` | Trip planner |
| `/offers` | Offer listing |
| `/offers/:offerSlug` | Offer details |
| `/travel-credit-card` | Travel credit card page |
| `/fare-alerts` | Fare alert page |

### API Client

`src/api/axiosConfig.js` creates the shared Axios client. It:

- Reads `VITE_API_BASE_URL`.
- Uses `http://localhost:8081/api` by default.
- Attaches JWT tokens from local auth utilities.
- Logs users out and redirects to `/login` when the backend returns `401`.

## 7. Backend Documentation

### Backend Responsibilities

The backend is responsible for:

- Authenticating users.
- Hashing passwords.
- Creating and validating JWT tokens.
- Sending and verifying OTP flows.
- Managing user profiles.
- Searching airports and flights.
- Managing aircraft, routes, schedules, fares, and seats.
- Creating and cancelling bookings.
- Keeping seat and fare availability consistent.
- Returning structured DTO responses to the frontend.

### Backend Package Structure

```text
services/skylink-api/src/main/java/com/skylink/
|-- config/
|-- controller/
|-- dto/
|-- entity/
|-- exception/
|-- filter/
|-- importer/
|-- repository/
|-- service/
`-- util/
```

### Package Responsibilities

| Package | Purpose |
| --- | --- |
| `config` | Spring Security, CORS, and application configuration |
| `controller` | REST endpoints |
| `dto` | Request and response models |
| `entity` | JPA database entities |
| `exception` | Global error handling |
| `filter` | JWT request authentication filter |
| `importer` | Data import and seed helpers |
| `repository` | Spring Data JPA repositories |
| `service` | Business logic |
| `util` | Shared utilities such as JWT handling |

## 8. Core Backend Domains

### User and Authentication

The system supports user registration, login, OTP login, forgot password, reset password, and profile retrieval. Passwords are hashed using BCrypt. JWT tokens are used for stateless authentication.

### Airport

Airport APIs support search and lookup by IATA/ICAO codes and nearby coordinates. Airport data is stored as database entities and can be seeded from bundled data files.

### Aircraft

Aircraft APIs manage aircraft details and active aircraft lookup. Aircraft data supports flight schedule and seat generation workflows.

### Flight Route

Flight routes connect source and destination airports. Route APIs allow route creation, resolution, and listing.

### Flight Schedule

Flight schedules represent actual flights with flight numbers, departure time, arrival time, active status, route, and aircraft associations.

### Flight Fare

Flight fares represent fare classes, base fare, available seats, and active status for a flight schedule. Fare availability is reduced when bookings are confirmed and increased when bookings are cancelled.

### Seat

Seat APIs expose seat availability by flight and fare class. Booking logic validates selected seats and marks them as booked.

### Booking

Booking APIs allow authenticated users to create bookings, view their bookings, get booking details by reference, and cancel bookings. Booking cancellation releases seats and restores fare availability.

## 9. Authentication Flow

```text
User submits login/register request
        |
        v
Backend validates request
        |
        v
Backend creates JWT
        |
        v
Frontend stores JWT
        |
        v
Axios attaches Authorization header
        |
        v
JwtAuthenticationFilter validates token
        |
        v
Protected API request is allowed
```

Protected resources require:

```text
Authorization: Bearer <jwt_token>
```

## 10. Booking Flow

```text
Search flights
    |
Select flight and fare class
    |
Choose passenger details and seats
    |
Submit booking request
    |
Validate authenticated user
    |
Validate flight schedule is active
    |
Lock and validate fare availability
    |
Validate selected seats
    |
Create booking reference
    |
Save booking and passengers
    |
Mark seats as booked
    |
Reduce available fare seats
    |
Return booking confirmation
```

Cancellation flow:

```text
Find booking by reference
    |
Validate authenticated owner
    |
Release booked passenger seats
    |
Increase fare availability
    |
Mark booking as cancelled
```

## 11. API Reference Summary

### Authentication and Users

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/users/register` | Register a new user |
| `POST` | `/api/auth/login` | Login with email and password |
| `POST` | `/api/auth/login/otp/send` | Send login OTP |
| `POST` | `/api/auth/login/otp/verify` | Verify login OTP |
| `POST` | `/api/auth/forgot-password` | Start password reset |
| `POST` | `/api/auth/reset-password` | Reset password |
| `GET` | `/api/users/me` | Get current authenticated user |

### Flight Search and Airports

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/flights/search` | Search available flights |
| `GET` | `/api/airports/search` | Search airports |
| `GET` | `/api/airports/iata/{iataCode}` | Find airport by IATA code |
| `GET` | `/api/airports/icao/{icaoCode}` | Find airport by ICAO code |
| `GET` | `/api/airports/nearby` | Find nearby airports |

### Flight Management

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/aircraft` | Create aircraft |
| `GET` | `/api/aircraft` | List aircraft |
| `GET` | `/api/aircraft/active` | List active aircraft |
| `POST` | `/api/flight-routes` | Create route |
| `POST` | `/api/flight-routes/resolve` | Resolve route |
| `GET` | `/api/flight-routes` | List routes |
| `POST` | `/api/flight-schedules` | Create schedule |
| `GET` | `/api/flight-schedules` | List schedules |
| `GET` | `/api/flight-schedules/search` | Search schedules |
| `POST` | `/api/flight-fares` | Create fare |
| `GET` | `/api/flight-fares` | List fares |

### Seats and Bookings

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/seats` | Get seat availability |
| `POST` | `/api/bookings` | Create booking |
| `GET` | `/api/bookings/my` | Get current user's bookings |
| `GET` | `/api/bookings/{bookingReference}` | Get booking detail |
| `POST` | `/api/bookings/{bookingReference}/cancel` | Cancel booking |

## 12. Database Model Overview

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

Relationship overview:

```text
User 1 -> many Bookings
Booking 1 -> many Passengers
Booking many -> 1 FlightSchedule
Booking many -> 1 FlightFare
FlightSchedule many -> 1 FlightRoute
FlightSchedule many -> 1 Aircraft
FlightRoute many -> 1 source Airport
FlightRoute many -> 1 destination Airport
FlightSchedule 1 -> many Seats
```

## 13. Environment Variables

Backend:

| Variable | Required | Description |
| --- | --- | --- |
| `SKYLINK_DB_URL` | Yes | MySQL JDBC connection URL |
| `SKYLINK_DB_USERNAME` | Yes | MySQL username |
| `SKYLINK_DB_PASSWORD` | Yes | MySQL password |
| `SKYLINK_MAIL_USERNAME` | Yes | SMTP username |
| `SKYLINK_MAIL_PASSWORD` | Yes | SMTP password or Gmail app password |
| `SKYLINK_JWT_SECRET` | Yes | Long JWT signing secret |

Frontend:

| Variable | Required | Description |
| --- | --- | --- |
| `VITE_API_BASE_URL` | No | Backend API URL. Defaults to `http://localhost:8081/api` |

## 14. Security Design

Security features:

- BCrypt password hashing.
- JWT-based stateless sessions.
- Spring Security filter chain.
- Protected booking and profile endpoints.
- Request DTO validation.
- Environment variable based secrets.

Security recommendations for production:

- Use HTTPS.
- Rotate any exposed credentials.
- Store secrets in a managed secret store.
- Use strict CORS origins.
- Use a managed production database user with limited permissions.
- Add rate limiting for login and OTP endpoints.
- Add audit logging for booking creation and cancellation.

## 15. Setup Guide

### Clone

```powershell
git clone https://github.com/tausifalam6879/SkyLink.git
cd SkyLink
```

### Backend

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

### Frontend

```powershell
cd skylink-web
npm install
$env:VITE_API_BASE_URL="http://localhost:8081/api"
npm run dev
```

## 16. Testing

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

The current backend test loads the Spring application context and needs valid database access because JPA and MySQL are part of the default application configuration.

## 17. Deployment Notes

### Frontend Deployment

Use a static hosting provider:

- Vercel
- Netlify
- GitHub Pages
- Cloudflare Pages

Build command:

```text
npm run build
```

Output directory:

```text
dist
```

Set:

```text
VITE_API_BASE_URL=https://your-api-domain.example.com/api
```

### Backend Deployment

Use a Java 21 compatible hosting provider:

- Render
- Railway
- AWS Elastic Beanstalk
- Azure App Service
- DigitalOcean App Platform

Configure all backend environment variables in the hosting dashboard. Connect the app to a MySQL database and allow network access from the backend host.

## 18. Current Limitations

- No public hosted demo URL is configured yet.
- Backend tests currently require a running MySQL-compatible database.
- Production-grade payment gateway integration is not included.
- Admin UI for managing aircraft, routes, schedules, and fares is not yet separated from the API layer.
- CORS should be tightened further before production deployment.

## 19. Future Improvements

- Public hosted live demo.
- Admin dashboard for airline operations.
- Payment gateway integration.
- Ticket PDF generation.
- Email ticket confirmation.
- Advanced flight filters and sorting.
- Role-based access control.
- Refresh token support.
- Docker Compose setup for frontend, backend, and MySQL.
- CI/CD pipeline with GitHub Actions.
- Integration tests using a test database or Testcontainers.

## 20. Conclusion

SkyLink is a complete full-stack project that demonstrates practical airline booking concepts with a React frontend and Spring Boot backend. It includes authentication, flight search, booking, seat handling, data import, protected user workflows, and deployment-ready configuration practices.
