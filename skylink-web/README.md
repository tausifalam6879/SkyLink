# SkyLink Web

React/Vite frontend for the SkyLink flight booking platform.

## Stack

- React 19
- Vite
- React Router
- Axios
- Lucide React icons
- ESLint

## Setup

```powershell
npm install
```

By default the app calls the backend at `http://localhost:8081/api`. Override it with:

```powershell
$env:VITE_API_BASE_URL="http://localhost:8081/api"
```

## Development

```powershell
npm run dev
```

Open the local URL printed by Vite, usually `http://localhost:5173`.

## Build

```powershell
npm run build
npm run preview
```

## Lint

```powershell
npm run lint
```

## Main Routes

- `/` and `/flights` - home and flight search
- `/login` - user login
- `/register` - account creation
- `/booking` - protected booking flow
- `/my-bookings` and `/bookings` - protected booking management
- `/user` and `/account` - protected user dashboard
- `/flight-status` - flight tracking
- `/group-booking` and `/support/group` - group travel request
- `/plan` - trip planner
- `/offers` and `/offers/:offerSlug` - offer listing and details
- `/travel-credit-card` - SkyLink credit card page
- `/fare-alerts` - fare alerts page

## API Client

The shared Axios client is defined in `src/api/axiosConfig.js`. It:

- reads `VITE_API_BASE_URL`
- attaches JWT tokens from local auth utilities
- redirects to `/login` when the API returns `401`
