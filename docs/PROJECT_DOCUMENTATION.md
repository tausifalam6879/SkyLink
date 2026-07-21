# SkyLink developer notes

This document records how SkyLink is put together and why a few implementation choices were made. The root [README](../README.md) contains the setup commands and public project overview; this file stays closer to the code.

## Scope

SkyLink has two execution modes:

- `skylink-web` can run against the Spring Boot API;
- the GitHub Pages build can use `demoApi.js` when no backend URL is configured.

The real API owns authentication, airport data, generated flight inventory, seats, passengers, and bookings. The demo adapter mirrors enough of that API contract to make the static deployment interactive, but its data only lives in the current browser.

The supporting travel pages are intentionally lighter. Flight status uses sample data, and fare alerts, group-booking requests, and planner state stay in the browser. They are UI experiments rather than completed backend modules.

## Entry points

Frontend startup:

```text
skylink-web/index.html
    -> src/main.jsx
    -> BrowserRouter
    -> src/App.jsx
    -> route page
```

Backend startup:

```text
SkylinkApiApplication
    -> Spring component scanning
    -> Security and repository configuration
    -> startup importers and seeders
    -> REST controllers
```

The shared frontend HTTP client is `src/api/axiosConfig.js`. It reads `VITE_API_BASE_URL`, attaches the stored JWT, and clears the browser auth state after a `401` response. The same client selects the demo adapter for the GitHub Pages build.

## Backend package boundaries

The backend uses a layered structure rather than exposing JPA entities directly from controllers.

| Package | Responsibility |
| --- | --- |
| `controller` | HTTP routes, parameters, status codes, and request validation |
| `dto` | Request and response contracts |
| `service` | Business rules, transactions, and entity-to-DTO mapping |
| `repository` | Spring Data queries and database locks |
| `entity` | MySQL tables, relationships, constraints, and timestamps |
| `config` | Security, CORS, and startup configuration |
| `filter` | JWT extraction and request authentication |
| `importer` | Dataset loading and generated inventory |
| `exception` | Common error responses |
| `util` | JWT signing and parsing |

Constructor injection is used throughout the main services and controllers. This keeps dependencies visible and allows Spring to construct each component without service-locator calls.

## Authentication notes

### Password path

```text
LoginPage
    -> POST /api/auth/login
    -> AuthController
    -> UserService.findByEmail
    -> BCrypt password check
    -> JwtUtil.generateToken
    -> frontend saveAuth
```

Passwords are stored as BCrypt hashes. The API does not create an HTTP session; the signed JWT is sent back on every protected request as a Bearer token.

`JwtAuthenticationFilter` reads the token subject and creates a Spring Security authentication object. The current filter assigns `ROLE_USER` to every valid token. The `role` stored on `User` therefore is not yet a complete authorization model. Inventory-management routes should receive an explicit admin policy before the API is exposed publicly.

### OTP path

`OtpService` creates a six-digit value with `SecureRandom`. OTP records are separated by identifier, type, and purpose, which prevents a registration OTP from being used as a login or password-reset OTP.

Current controls:

- five-minute expiry;
- 60-second resend cooldown;
- five failed verification attempts;
- five resends for a matching identifier and purpose.

Email delivery goes through Spring Mail and the Gmail SMTP configuration in `application.yaml`.

There is still hardening work to do: OTP values should be stored as hashes, successful OTPs should be consumed explicitly, and the public auth endpoints need rate limiting.

## Airport data

The backend imports the large `airports.csv` dataset into MySQL. `AirportRepository.searchAirports` searches active airports with usable IATA codes and ranks exact code/city matches before broad name matches. The API limits autocomplete results to 20.

Nearby-airport search is a native query because the distance expression is easier to express with database trigonometric functions. `AirportService` recalculates the returned distance with the Haversine formula and rounds it for the response.

The hosted demo does not load the full CSV. `scripts/generate-frontend-airports.mjs` filters it to useful IATA airports and writes `skylink-web/public/data/airports.json`.

Data provenance is documented in the root README. Airport records follow the OurAirports format; airline, aircraft-type, and route records follow OpenFlights formats.

## Route, schedule, and fare model

A route and a schedule are separate concepts:

- `FlightRoute` connects a source airport to a destination airport;
- `FlightSchedule` assigns a flight number, date/time, and aircraft to that route;
- `FlightFare` stores price and remaining capacity for one schedule and fare class;
- `Seat` stores the physical seat map for one schedule.

When a route is created, `FlightRouteService` calculates distance from the airport coordinates. Estimated duration uses an average speed of 800 km/h plus a 30-minute overhead. It is a scheduling approximation, not live operational data.

`FlightScheduleService` checks whether an aircraft already has an overlapping schedule before assigning it. The overlap query looks for an existing departure before the proposed arrival and an existing arrival after the proposed departure.

The real backend supports these fare classes:

- `ECONOMY`
- `BUSINESS`
- `FIRST_CLASS`

The demo adapter has a separate sample model and also shows premium economy. This difference is visible in the UI but is not part of the real backend enum.

## Search-time inventory generation

`FlightSearchService` first searches for active `SCHEDULED` flights on the requested route and date. If it finds none, it can create inventory on demand:

1. resolve or create the route;
2. select active SkyLink aircraft whose registration begins with `VT-SLA`;
3. calculate how many departures the route should receive;
4. skip aircraft with an overlapping assignment;
5. save the schedule;
6. create fares from the aircraft's class capacities;
7. generate the seat map.

Short domestic routes receive more departure slots than long routes. The available time pool is fixed, so the generated schedules are predictable enough for a demonstration.

Sample economy pricing:

```text
max(2200, 1500 + distanceKm * 4.25)
```

Business is `2.35` times economy and first class is `4.50` times economy. `BigDecimal` is used when the values become stored fares so money is not persisted with binary floating-point rounding.

One trade-off of on-demand generation is that a read-like search request may write new schedules, fares, and seats. It keeps the demo populated without pre-generating every global route/date combination, but a production design would normally separate inventory planning from customer search.

## Booking transaction

The main booking path is:

```text
HomePage fare selection
    -> selected booking in localStorage
    -> BookingPage seat request
    -> POST /api/bookings
    -> JwtAuthenticationFilter
    -> BookingController
    -> BookingService.createBooking
    -> repositories and SeatService
    -> BookingResponse
```

`BookingService.createBooking` runs in a transaction. It performs the following checks and writes:

1. resolve the authenticated user from `SecurityContextHolder`;
2. load and validate the active schedule;
3. lock the matching fare row with `PESSIMISTIC_WRITE`;
4. verify passenger count and remaining fare capacity;
5. reject empty, duplicate, wrong-class, or already-booked seats;
6. calculate `baseFare * passengerCount`;
7. save the booking and passengers;
8. mark the seats as booked;
9. reduce the fare's remaining capacity.

The fare-row lock serializes changes for the same schedule and class. Since seat validation occurs while that lock is held, two bookings in the same class do not validate against the same stale inventory state.

Cancellation also runs in a transaction. It checks ownership, locks the fare, releases each passenger seat, restores the capacity, and marks the booking and passengers inactive.

The booking reference is generated from a UUID-derived value with a `SKY` prefix and checked for uniqueness before use.

## Relational model

```text
User 1 -------- N Booking
Booking 1 ----- N Passenger
Booking N ----- 1 FlightSchedule
Booking N ----- 1 FlightFare
FlightSchedule N ----- 1 FlightRoute
FlightSchedule N ----- 1 Aircraft
FlightRoute N -------- 1 source Airport
FlightRoute N -------- 1 destination Airport
FlightSchedule 1 ----- N FlightFare
FlightSchedule 1 ----- N Seat
```

Notable constraints:

- unique user email and mobile number;
- unique aircraft registration;
- unique source/destination route pair;
- unique flight number and departure combination;
- one fare per schedule and fare class;
- one seat number per schedule;
- unique booking reference.

Most `ManyToOne` relationships are lazy. With `spring.jpa.open-in-view=false`, code that needs relationship data must access it inside a service transaction or map it before leaving that boundary.

## Startup data

Several `CommandLineRunner` components prepare development data:

- airport importers load airport records;
- `AircraftDataGenerator` builds the larger SkyLink fleet;
- `AircraftFleetSeeder` adds a small predefined set when missing;
- `FlightRouteDataImporter` converts valid OpenFlights route pairs;
- `FlightScheduleDataGenerator` creates a limited seven-day startup window;
- `SeatDataGenerator` fills missing seat maps.

The startup schedule generator deliberately limits the number of routes and aircraft. Generating schedules for every imported global route would make startup slow and fill the database with inventory that may never be searched.

The importer set has grown over time and could be simplified. In particular, airport and aircraft initialization should eventually have one owner each, explicit profiles, and a fully documented ordering scheme.

## Frontend state and storage

The frontend does not use a global state library. Page-local React state handles form inputs and API results.

Browser storage is used for a few cross-page concerns:

| Key/area | Storage | Reason |
| --- | --- | --- |
| JWT and cached user | `localStorage` | Keep the demo login across reloads |
| Selected flight/fare | `localStorage` | Carry selection through login to booking |
| Search form/results | `sessionStorage` | Restore a search within the current tab |
| Demo bookings/profile | `localStorage` | Persist the browser-only demo |
| Fare alerts/group requests | `localStorage` | Support UI-only features |

Storing JWTs in `localStorage` keeps the client simple but exposes the token to JavaScript. An HttpOnly secure-cookie design would be preferable for a hardened deployment.

## Error handling

`GlobalExceptionHandler` returns a consistent `ErrorResponse` for validation, mail, runtime, and unexpected errors. The frontend reads the response message and shows it near the relevant form.

The current handler maps every `RuntimeException` to `400 Bad Request`. More specific exception types should eventually distinguish authentication, authorization, missing records, and conflicts with `401`, `403`, `404`, and `409` responses.

## Tests

The test profile uses H2 in MySQL compatibility mode and recreates the schema for the test run. `SkylinkApiApplicationTests.contextLoads` confirms that Spring can discover the components, create repositories, initialize the schema, and start the application context.

That test is intentionally small and does not prove booking correctness. The most useful next tests would cover:

- registration and both login paths;
- OTP expiry, retry, and reuse cases;
- airport search ranking;
- search-time schedule generation;
- two concurrent attempts to book the final seat;
- transaction rollback after a booking failure;
- cancellation ownership and inventory restoration.

Frontend verification currently consists of the production Vite build and ESLint. A browser-level booking test would add more confidence than snapshot-heavy component tests.

## Deployment notes

The GitHub Actions workflow builds `skylink-web` with `/SkyLink/` as its base path and enables demo mode. It copies `index.html` to `404.html` so that direct visits to client-side routes still load the React application on GitHub Pages.

Running the real system publicly would additionally require:

- a Java 21 host;
- a managed MySQL database;
- SMTP and JWT secrets in the host's secret store;
- a production `VITE_API_BASE_URL`;
- HTTPS and explicit CORS origins.

## Work still open

The following items are deliberately described as unfinished rather than implied by the UI:

- multi-passenger and round-trip booking;
- payment, refunds, ticket documents, and confirmation emails;
- admin authorization and an inventory-management interface;
- real flight-status data;
- server-side fare alerts and group-booking requests;
- token refresh, auth rate limiting, and stronger OTP lifecycle rules;
- Flyway or Liquibase migrations;
- meaningful service, API, and concurrency test coverage.

These boundaries are useful when discussing the project: they separate the flows that are implemented end to end from screens that currently demonstrate a product direction.
