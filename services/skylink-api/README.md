# SkyLink API

Spring Boot REST API for SkyLink. It manages users, authentication, OTP flows, airports, aircraft, routes, schedules, fares, seats, and bookings.

## Stack

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Security
- Spring Data JPA
- Bean Validation
- Spring Mail
- MySQL
- JWT

## Configuration

The API reads secrets and local connection details from environment variables.

| Variable | Purpose |
| --- | --- |
| `SKYLINK_DB_URL` | MySQL JDBC URL, for example `jdbc:mysql://localhost:3306/skylink_db` |
| `SKYLINK_DB_USERNAME` | MySQL username |
| `SKYLINK_DB_PASSWORD` | MySQL password |
| `SKYLINK_MAIL_USERNAME` | SMTP/Gmail username |
| `SKYLINK_MAIL_PASSWORD` | SMTP/Gmail app password |
| `SKYLINK_JWT_SECRET` | Long random JWT signing secret, at least 64 characters |

PowerShell example:

```powershell
$env:SKYLINK_DB_URL="jdbc:mysql://localhost:3306/skylink_db"
$env:SKYLINK_DB_USERNAME="root"
$env:SKYLINK_DB_PASSWORD="your_mysql_password"
$env:SKYLINK_MAIL_USERNAME="your_email@gmail.com"
$env:SKYLINK_MAIL_PASSWORD="your_gmail_app_password"
$env:SKYLINK_JWT_SECRET="replace_with_a_long_random_secret_at_least_64_characters"
```

## Database

Create the local database before starting the API:

```sql
CREATE DATABASE skylink_db;
```

Hibernate is configured with `ddl-auto: update`, so application entities can update the schema during development.

## Run

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs on `http://localhost:8081`.

## Test

```powershell
.\mvnw.cmd test
```

## API Areas

- Auth: `/api/auth`
- Users: `/api/users`
- Airports: `/api/airports`
- Aircraft: `/api/aircraft`
- Flight routes: `/api/flight-routes`
- Flight schedules: `/api/flight-schedules`
- Flight fares: `/api/flight-fares`
- Flight search: `/api/flights`
- Bookings: `/api/bookings`
- Seats: `/api/seats`
- OTP: `/api/otp`

## Security

Never commit real credentials. Keep database passwords, mail app passwords, and JWT secrets in environment variables or a secret manager. If a real credential was pushed to any remote repository, rotate it immediately.
