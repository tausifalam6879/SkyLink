import {
  Navigate,
  Route,
  Routes,
  useLocation,
} from "react-router-dom";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import BookingPage from "./pages/BookingPage";
import MyBookingsPage from "./pages/MyBookingsPage";
import UserDashboardPage from "./pages/UserDashboardPage";
import FlightStatusPage from "./pages/FlightStatusPage";
import GroupBookingPage from "./pages/GroupBookingPage";
import TripPlannerPage from "./pages/TripPlannerPage";
import OffersPage from "./pages/OffersPage";
import OfferDetailPage from "./pages/OfferDetailPage";
import CreditCardPage from "./pages/CreditCardPage";
import FareAlertsPage from "./pages/FareAlertsPage";
import { isAuthenticated } from "./utils/auth";

function ProtectedRoute({ children }) {
  const location = useLocation();

  if (!isAuthenticated()) {
    return (
      <Navigate
        to="/login"
        replace
        state={{
          redirectTo:
            location.pathname +
            location.search,
        }}
      />
    );
  }

  return children;
}

function App() {
  return (
    <Routes>
      <Route
        path="/"
        element={<HomePage />}
      />

      <Route
        path="/flights"
        element={<HomePage />}
      />

      <Route
        path="/login"
        element={<LoginPage />}
      />

      <Route
        path="/register"
        element={<RegisterPage />}
      />

      <Route
        path="/flight-status"
        element={<FlightStatusPage />}
      />

      <Route
        path="/group-booking"
        element={<GroupBookingPage />}
      />

      <Route
        path="/support/group"
        element={<GroupBookingPage />}
      />

      <Route
        path="/plan"
        element={<TripPlannerPage />}
      />

      <Route
        path="/offers"
        element={<OffersPage />}
      />

      <Route
        path="/offers/campaigns/iciciemi-campaign"
        element={<OffersPage />}
      />

      <Route
        path="/offers/:offerSlug"
        element={<OfferDetailPage />}
      />

      <Route
        path="/travel-credit-card"
        element={<CreditCardPage />}
      />

      <Route
        path="/fare-alerts"
        element={<FareAlertsPage />}
      />

      <Route
        path="/booking"
        element={
          <ProtectedRoute>
            <BookingPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/my-bookings"
        element={
          <ProtectedRoute>
            <MyBookingsPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/bookings"
        element={
          <ProtectedRoute>
            <MyBookingsPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/user"
        element={
          <ProtectedRoute>
            <UserDashboardPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/account"
        element={
          <ProtectedRoute>
            <UserDashboardPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="*"
        element={
          <Navigate to="/" replace />
        }
      />
    </Routes>
  );
}

export default App;
