import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  CalendarDays,
  Plane,
  RefreshCw,
  Ticket,
  XCircle,
} from "lucide-react";
import api from "../api/axiosConfig";

function MyBookingsPage() {
  const navigate = useNavigate();

  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cancellingReference, setCancellingReference] =
    useState(null);
  const [error, setError] = useState("");

  const loadBookings = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const response = await api.get("/bookings/my");

      setBookings(response.data);
    } catch (requestError) {
      console.error(requestError);

      setBookings([]);

      setError(
        requestError.response?.data?.message ||
          "Unable to load your bookings."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      loadBookings();
    }, 0);

    return () => window.clearTimeout(timer);
  }, [loadBookings]);

  const cancelBooking = async (bookingReference) => {
    const confirmed = window.confirm(
      "Are you sure you want to cancel this booking?"
    );

    if (!confirmed) {
      return;
    }

    setCancellingReference(bookingReference);
    setError("");

    try {
      await api.post(
        `/bookings/${bookingReference}/cancel`
      );

      await loadBookings();
    } catch (requestError) {
      console.error(requestError);

      setError(
        requestError.response?.data?.message ||
          "Unable to cancel booking."
      );
    } finally {
      setCancellingReference(null);
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 0,
    }).format(price);
  };

  const formatTime = (dateTime) => {
    return new Date(dateTime).toLocaleTimeString(
      "en-IN",
      {
        hour: "2-digit",
        minute: "2-digit",
        hour12: true,
      }
    );
  };

  const formatDate = (dateTime) => {
    return new Date(dateTime).toLocaleDateString(
      "en-IN",
      {
        day: "2-digit",
        month: "short",
        year: "numeric",
      }
    );
  };

  return (
    <div className="my-bookings-page">
      <nav className="booking-navbar">
        <div
          className="brand booking-brand"
          onClick={() => navigate("/")}
        >
          <div className="brand-icon">
            <Plane size={25} />
          </div>

          <div>
            <h1>SkyLink</h1>

            <span>Fly Beyond Boundaries</span>
          </div>
        </div>

        <button
          type="button"
          className="back-search-button"
          onClick={() => navigate("/")}
        >
          <ArrowLeft size={18} />
          Back to search
        </button>
      </nav>

      <main className="my-bookings-content">
        <div className="my-bookings-heading">
          <div>
            <span>YOUR JOURNEYS</span>

            <h2>My Bookings</h2>

            <p>
              View and manage your SkyLink flight
              bookings.
            </p>
          </div>

          <button
            type="button"
            className="refresh-bookings-button"
            onClick={loadBookings}
            disabled={loading}
          >
            <RefreshCw size={18} />
            Refresh
          </button>
        </div>

        {error && (
          <div className="booking-error">
            {error}
          </div>
        )}

        {loading && (
          <div className="bookings-state-card">
            <RefreshCw
              size={36}
              className="loading-icon"
            />

            <h3>Loading bookings...</h3>

            <p>
              Fetching your latest SkyLink journeys.
            </p>
          </div>
        )}

        {!loading && bookings.length === 0 && (
          <div className="bookings-state-card">
            <Ticket size={44} />

            <h3>No bookings yet</h3>

            <p>
              Search for a flight and book your first
              SkyLink journey.
            </p>

            <button
              type="button"
              className="primary-action-button"
              onClick={() => navigate("/")}
            >
              Search Flights
            </button>
          </div>
        )}

        {!loading &&
          bookings.map((booking) => {
            const isCancelled =
              booking.status === "CANCELLED";

            const isCancelling =
              cancellingReference ===
              booking.bookingReference;

            return (
              <article
                className={`my-booking-card ${
                  isCancelled
                    ? "cancelled-booking"
                    : ""
                }`}
                key={booking.id}
              >
                <div className="my-booking-top">
                  <div className="booking-reference">
                    <span>BOOKING REFERENCE</span>

                    <strong>
                      {booking.bookingReference}
                    </strong>
                  </div>

                  <span
                    className={`booking-status ${
                      isCancelled
                        ? "status-cancelled"
                        : "status-confirmed"
                    }`}
                  >
                    {booking.status}
                  </span>
                </div>

                <div className="my-booking-flight">
                  <div className="my-flight-number">
                    <div className="mini-plane">
                      <Plane size={21} />
                    </div>

                    <div>
                      <span>SKYLINK</span>

                      <strong>
                        {booking.flightNumber}
                      </strong>
                    </div>
                  </div>

                  <div className="my-booking-route">
                    <div>
                      <strong>
                        {booking.sourceIataCode}
                      </strong>

                      <span>
                        {formatTime(
                          booking.departureTime
                        )}
                      </span>
                    </div>

                    <div className="my-route-line">
                      <i />

                      <Plane size={18} />

                      <i />
                    </div>

                    <div>
                      <strong>
                        {booking.destinationIataCode}
                      </strong>

                      <span>
                        {formatTime(
                          booking.arrivalTime
                        )}
                      </span>
                    </div>
                  </div>

                  <div className="my-booking-date">
                    <CalendarDays size={18} />

                    <span>
                      {formatDate(
                        booking.departureTime
                      )}
                    </span>
                  </div>
                </div>

                <div className="my-booking-details">
                  <div>
                    <span>Fare Class</span>

                    <strong>
                      {booking.fareClass}
                    </strong>
                  </div>

                  <div>
                    <span>Passengers</span>

                    <strong>
                      {booking.passengerCount}
                    </strong>
                  </div>

                  <div>
                    <span>Total Amount</span>

                    <strong>
                      {formatPrice(
                        booking.totalAmount
                      )}
                    </strong>
                  </div>
                </div>

                {booking.passengers?.length > 0 && (
                  <div className="passenger-list">
                    <span>PASSENGERS</span>

                    {booking.passengers.map(
                      (passenger) => (
                        <div
                          className="passenger-row"
                          key={passenger.id}
                        >
                          <div>
                            <strong>
                              {passenger.firstName}{" "}
                              {passenger.lastName}
                            </strong>

                            <small>
                              {passenger.nationality} -{" "}
                              {passenger.gender}
                            </small>
                          </div>

                          <span>
                            {passenger.seatNumber ||
                              "Seat not assigned"}
                          </span>
                        </div>
                      )
                    )}
                  </div>
                )}

                {!isCancelled && (
                  <div className="booking-card-actions">
                    <button
                      type="button"
                      className="cancel-booking-button"
                      onClick={() =>
                        cancelBooking(
                          booking.bookingReference
                        )
                      }
                      disabled={isCancelling}
                    >
                      <XCircle size={18} />

                      {isCancelling
                        ? "Cancelling..."
                        : "Cancel Booking"}
                    </button>
                  </div>
                )}
              </article>
            );
          })}
      </main>
    </div>
  );
}

export default MyBookingsPage;
