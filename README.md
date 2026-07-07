# SkyLink

SkyLink is a full-stack flight booking and travel management platform. It combines a React/Vite web app with a Spring Boot API for authentication, airport search, flight schedules, fares, bookings, seats, and user profiles.

## Project Structure

```text
SkyLink/
├── skylink-web/              # React + Vite frontend
├── services/skylink-api/     # Spring Boot REST API
├── database/                 # Database resources, if used locally
├── docs/                     # Project documentation
├── diagrams/                 # Architecture and flow diagrams
├── postman/                  # API collections, if exported
└── scripts/                  # Utility scripts
```

## Tech Stack

- Frontend: React 19, Vite, React Router, Axios, Lucide React
- Backend: Java 21, Spring Boot 3.5, Spring Web, Spring Security, Spring Data JPA, Validation, Mail
- Database: MySQL
- Authentication: JWT with email/password and OTP flows

## Features

- User registration, login, JWT session handling, OTP login, forgot/reset password
- Flight search, airport search, flight routes, schedules, fares, seats, and bookings
- Protected user dashboard and booking management screens
- Offer pages, fare alerts, group booking, trip planning, credit card, and flight status pages
- Seed/import support for airport, aircraft, route, and schedule data

## Prerequisites

- Node.js 20 or newer
- npm
- Java 21
- Maven, or the included Maven wrapper
- MySQL 8 or compatible

## Backend Setup

1. Create a local MySQL database:

```sql
CREATE DATABASE skylink_db;
```

2. Set the required environment variables:

```powershell
$env:SKYLINK_DB_URL="jdbc:mysql://localhost:3306/skylink_db"
$env:SKYLINK_DB_USERNAME="root"
$env:SKYLINK_DB_PASSWORD="your_mysql_password"
$env:SKYLINK_MAIL_USERNAME="your_email@gmail.com"
$env:SKYLINK_MAIL_PASSWORD="your_gmail_app_password"
$env:SKYLINK_JWT_SECRET="replace_with_a_long_random_secret_at_least_64_characters"
```

3. Start the API:

```powershell
cd services/skylink-api
.\mvnw.cmd spring-boot:run
```

The API runs on `http://localhost:8081`.

## Frontend Setup

1. Install dependencies:

```powershell
cd skylink-web
npm install
```

2. Optional: set the API URL if it differs from the default:

```powershell
$env:VITE_API_BASE_URL="http://localhost:8081/api"
```

3. Start the app:

```powershell
npm run dev
```

The Vite app runs on the URL printed in the terminal, usually `http://localhost:5173`.

## Useful Commands

```powershell
# Frontend
cd skylink-web
npm run dev
npm run build
npm run lint

# Backend
cd services/skylink-api
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

## API Overview

Primary backend routes include:

- `POST /api/users/register`
- `POST /api/auth/login`
- `POST /api/auth/login/otp/send`
- `POST /api/auth/login/otp/verify`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `GET /api/users/me`
- `GET /api/airports/search`
- `GET /api/airports/iata/{iataCode}`
- `POST /api/flights/search`
- `GET /api/flight-schedules`
- `GET /api/flight-schedules/search`
- `GET /api/flight-fares/flight/{flightScheduleId}`
- `POST /api/bookings`
- `GET /api/bookings/my`
- `POST /api/bookings/{bookingReference}/cancel`
- `GET /api/seats`

## Security Notes

- Do not commit real database passwords, mail app passwords, JWT secrets, or `.env` files.
- Production deployments should use strong environment-managed secrets.
- Rotate any credential that was ever committed or shared publicly.

## GitHub Workflow

```powershell
git status
git add .
git commit -m "docs: document SkyLink setup"
git push origin main
```

If the remote repository is new, create it on GitHub first and then add the remote:

```powershell
git remote add origin https://github.com/<your-username>/<repo-name>.git
git branch -M main
git push -u origin main
```
