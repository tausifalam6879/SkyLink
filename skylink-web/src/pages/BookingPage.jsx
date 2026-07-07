import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  CalendarDays,
  CheckCircle2,
  Plane,
  UserRound,
} from "lucide-react";
import api from "../api/axiosConfig";

const SELECTED_BOOKING_KEY =
  "skylink_selected_booking";

const getStoredSelectedBooking = () => {
  const storedBooking =
    localStorage.getItem(
      SELECTED_BOOKING_KEY
    );

  if (!storedBooking) {
    return null;
  }

  try {
    return JSON.parse(storedBooking);
  } catch {
    localStorage.removeItem(
      SELECTED_BOOKING_KEY
    );

    return null;
  }
};

function BookingPage() {
  const navigate = useNavigate();

  const [selectedBooking] =
    useState(getStoredSelectedBooking);

  const [firstName, setFirstName] =
    useState("");

  const [lastName, setLastName] =
    useState("");

  const [dateOfBirth, setDateOfBirth] =
    useState("");

  const [gender, setGender] =
    useState("MALE");

  const [passportNumber, setPassportNumber] =
    useState("");

  const [nationality, setNationality] =
    useState("INDIAN");

  const [seats, setSeats] =
    useState([]);

  const [selectedSeatNumber, setSelectedSeatNumber] =
    useState("");

  const [seatLoading, setSeatLoading] =
    useState(false);

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState("");

  const [confirmedBooking, setConfirmedBooking] =
    useState(null);

  useEffect(() => {
    if (
      !selectedBooking &&
      !confirmedBooking
    ) {
      navigate("/", {
        replace: true,
      });
    }
  }, [
    confirmedBooking,
    navigate,
    selectedBooking,
  ]);

  useEffect(() => {
    if (
      !selectedBooking?.flightScheduleId ||
      !selectedBooking?.fareClass
    ) {
      return;
    }

    const loadSeats = async () => {
      setSeatLoading(true);
      setError("");

      try {
        const response = await api.get(
          `/seats/flight/${selectedBooking.flightScheduleId}/class/${selectedBooking.fareClass}`
        );

        setSeats(response.data);
      } catch (requestError) {
        console.error(requestError);

        setSeats([]);

        setError(
          requestError.response?.data?.message ||
            "Unable to load seats. Please try again."
        );
      } finally {
        setSeatLoading(false);
      }
    };

    loadSeats();
  }, [selectedBooking]);

  const seatsByRow = useMemo(() => {
    return seats.reduce(
      (groupedSeats, seat) => {
        const rowNumber =
          seat.rowNumber;

        if (!groupedSeats[rowNumber]) {
          groupedSeats[rowNumber] = [];
        }

        groupedSeats[rowNumber].push(
          seat
        );

        return groupedSeats;
      },
      {}
    );
  }, [seats]);

  const sortedRows = useMemo(() => {
    return Object.keys(seatsByRow)
      .map(Number)
      .sort(
        (firstRow, secondRow) =>
          firstRow - secondRow
      );
  }, [seatsByRow]);

  const createBooking = async (event) => {
    event.preventDefault();

    if (!selectedBooking) {
      return;
    }

    if (!selectedSeatNumber) {
      setError(
        "Please select a seat before confirming your booking."
      );

      return;
    }

    setLoading(true);
    setError("");

    try {
      const response = await api.post(
        "/bookings",
        {
          flightScheduleId:
            selectedBooking.flightScheduleId,

          fareClass:
            selectedBooking.fareClass,

          passengers: [
            {
              firstName:
                firstName.trim(),

              lastName:
                lastName.trim(),

              dateOfBirth,

              gender,

              passportNumber:
                passportNumber.trim()
                  ? passportNumber
                      .trim()
                      .toUpperCase()
                  : null,

              nationality:
                nationality
                  .trim()
                  .toUpperCase(),

              seatNumber:
                selectedSeatNumber,
            },
          ],
        }
      );

      setConfirmedBooking(
        response.data
      );

      localStorage.removeItem(
        SELECTED_BOOKING_KEY
      );

      window.scrollTo({
        top: 0,
        behavior: "smooth",
      });
    } catch (requestError) {
      console.error(requestError);

      setError(
        requestError.response?.data?.message ||
          "Unable to create booking. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  const selectSeat = (seat) => {
    if (
      seat.booked ||
      !seat.available
    ) {
      return;
    }

    setSelectedSeatNumber(
      seat.seatNumber
    );

    setError("");
  };

  const getSeatByLetter = (
    rowSeats,
    seatLetter
  ) => {
    return rowSeats.find(
      (seat) =>
        seat.seatLetter === seatLetter
    );
  };

  const renderSeat = (
    rowSeats,
    seatLetter
  ) => {
    const seat =
      getSeatByLetter(
        rowSeats,
        seatLetter
      );

    if (!seat) {
      return (
        <div
          className="seat-placeholder"
          key={seatLetter}
        />
      );
    }

    const selected =
      selectedSeatNumber ===
      seat.seatNumber;

    const unavailable =
      seat.booked ||
      !seat.available;

    const classNames = [
      "aircraft-seat",
      selected
        ? "selected-seat"
        : "",
      unavailable
        ? "booked-seat"
        : "",
      seat.extraLegroom
        ? "extra-legroom-seat"
        : "",
    ]
      .filter(Boolean)
      .join(" ");

    return (
      <button
        type="button"
        key={seat.id}
        className={classNames}
        disabled={unavailable}
        onClick={() =>
          selectSeat(seat)
        }
        title={
          unavailable
            ? `${seat.seatNumber} is unavailable`
            : `${seat.seatNumber}${
                seat.windowSeat
                  ? " - Window"
                  : seat.aisleSeat
                    ? " - Aisle"
                    : ""
              }${
                seat.extraLegroom
                  ? " - Extra legroom"
                  : ""
              }`
        }
      >
        {seat.seatLetter}
      </button>
    );
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat(
      "en-IN",
      {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0,
      }
    ).format(price);
  };

  const formatTime = (dateTime) => {
    return new Date(
      dateTime
    ).toLocaleTimeString(
      "en-IN",
      {
        hour: "2-digit",
        minute: "2-digit",
        hour12: true,
      }
    );
  };

  const formatDate = (dateTime) => {
    return new Date(
      dateTime
    ).toLocaleDateString(
      "en-IN",
      {
        day: "2-digit",
        month: "short",
        year: "numeric",
      }
    );
  };

  if (
    !selectedBooking &&
    !confirmedBooking
  ) {
    return null;
  }

  if (confirmedBooking) {
    return (
      <div className="booking-page">
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

              <span>
                Fly Beyond Boundaries
              </span>
            </div>
          </div>
        </nav>

        <main className="confirmation-section">
          <div className="confirmation-card">
            <div className="confirmation-icon">
              <CheckCircle2 size={48} />
            </div>

            <span className="confirmation-label">
              BOOKING CONFIRMED
            </span>

            <h2>
              You're ready to fly!
            </h2>

            <p>
              Your SkyLink booking has been
              successfully confirmed.
            </p>

            <div className="booking-reference-box">
              <span>
                BOOKING REFERENCE
              </span>

              <strong>
                {
                  confirmedBooking.bookingReference
                }
              </strong>
            </div>

            <div className="confirmation-route">
              <div>
                <span>
                  {
                    confirmedBooking.sourceIataCode
                  }
                </span>

                <small>
                  {
                    confirmedBooking.sourceAirportName
                  }
                </small>
              </div>

              <Plane size={24} />

              <div>
                <span>
                  {
                    confirmedBooking.destinationIataCode
                  }
                </span>

                <small>
                  {
                    confirmedBooking.destinationAirportName
                  }
                </small>
              </div>
            </div>

            <div className="confirmation-details">
              <div>
                <span>Flight</span>

                <strong>
                  {
                    confirmedBooking.flightNumber
                  }
                </strong>
              </div>

              <div>
                <span>Fare Class</span>

                <strong>
                  {
                    confirmedBooking.fareClass
                  }
                </strong>
              </div>

              <div>
                <span>Seat</span>

                <strong>
                  {selectedSeatNumber}
                </strong>
              </div>

              <div>
                <span>Total Amount</span>

                <strong>
                  {formatPrice(
                    confirmedBooking.totalAmount
                  )}
                </strong>
              </div>
            </div>

            <div className="confirmation-actions">
              <button
                type="button"
                className="secondary-action-button"
                onClick={() =>
                  navigate("/")
                }
              >
                Search More Flights
              </button>

              <button
                type="button"
                className="primary-action-button"
                onClick={() =>
                  navigate(
                    "/my-bookings"
                  )
                }
              >
                View My Bookings
              </button>
            </div>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="booking-page">
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

            <span>
              Fly Beyond Boundaries
            </span>
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

      <main className="booking-content">
        <div className="booking-heading">
          <span>
            COMPLETE YOUR BOOKING
          </span>

          <h2>
            Passenger details & seat
          </h2>

          <p>
            Enter passenger information and
            choose your preferred seat.
          </p>
        </div>

        <div className="booking-layout">
          <form
            className="passenger-form-card"
            onSubmit={createBooking}
          >
            <div className="passenger-card-heading">
              <div>
                <UserRound size={21} />
              </div>

              <div>
                <span>PASSENGER 1</span>

                <h3>
                  Primary passenger
                </h3>
              </div>
            </div>

            <div className="passenger-form-grid">
              <div className="booking-input-group">
                <label htmlFor="firstName">
                  First name
                </label>

                <input
                  id="firstName"
                  value={firstName}
                  onChange={(event) =>
                    setFirstName(
                      event.target.value
                    )
                  }
                  placeholder="Md"
                  required
                />
              </div>

              <div className="booking-input-group">
                <label htmlFor="lastName">
                  Last name
                </label>

                <input
                  id="lastName"
                  value={lastName}
                  onChange={(event) =>
                    setLastName(
                      event.target.value
                    )
                  }
                  placeholder="Tausif"
                  required
                />
              </div>

              <div className="booking-input-group">
                <label htmlFor="dateOfBirth">
                  Date of birth
                </label>

                <input
                  id="dateOfBirth"
                  type="date"
                  value={dateOfBirth}
                  onChange={(event) =>
                    setDateOfBirth(
                      event.target.value
                    )
                  }
                  required
                />
              </div>

              <div className="booking-input-group">
                <label htmlFor="gender">
                  Gender
                </label>

                <select
                  id="gender"
                  value={gender}
                  onChange={(event) =>
                    setGender(
                      event.target.value
                    )
                  }
                  required
                >
                  <option value="MALE">
                    Male
                  </option>

                  <option value="FEMALE">
                    Female
                  </option>

                  <option value="OTHER">
                    Other
                  </option>
                </select>
              </div>

              <div className="booking-input-group">
                <label htmlFor="nationality">
                  Nationality
                </label>

                <input
                  id="nationality"
                  value={nationality}
                  onChange={(event) =>
                    setNationality(
                      event.target.value
                    )
                  }
                  placeholder="INDIAN"
                  required
                />
              </div>

              <div className="booking-input-group">
                <label htmlFor="passportNumber">
                  Passport number
                </label>

                <input
                  id="passportNumber"
                  value={passportNumber}
                  onChange={(event) =>
                    setPassportNumber(
                      event.target.value
                    )
                  }
                  placeholder="Optional"
                />
              </div>
            </div>

            <div className="seat-selection-section">
              <div className="seat-selection-heading">
                <span>
                  SELECT YOUR SEAT
                </span>

                <h3>
                  {selectedBooking.fareClass}
                  {" "}cabin
                </h3>

                <p>
                  Choose one available seat for
                  this passenger.
                </p>
              </div>

              <div className="seat-legend">
                <div>
                  <span className="seat-legend-box available-legend" />
                  Available
                </div>

                <div>
                  <span className="seat-legend-box selected-legend" />
                  Selected
                </div>

                <div>
                  <span className="seat-legend-box booked-legend" />
                  Booked
                </div>

                <div>
                  <span className="seat-legend-box extra-legroom-legend" />
                  Extra legroom
                </div>
              </div>

              {seatLoading ? (
                <div className="seat-loading-state">
                  Loading seat map...
                </div>
              ) : seats.length === 0 ? (
                <div className="seat-loading-state">
                  No seats available for this
                  fare class.
                </div>
              ) : (
                <div className="aircraft-seat-map">
                  <div className="aircraft-front">
                    <Plane size={25} />

                    <span>
                      FRONT OF AIRCRAFT
                    </span>
                  </div>

                  <div className="seat-column-labels">
                    <span>A</span>
                    <span>B</span>
                    <span>C</span>

                    <strong />

                    <span>D</span>
                    <span>E</span>
                    <span>F</span>
                  </div>

                  <div className="seat-rows">
                    {sortedRows.map(
                      (rowNumber) => {
                        const rowSeats =
                          seatsByRow[
                            rowNumber
                          ];

                        return (
                          <div
                            className="seat-row"
                            key={rowNumber}
                          >
                            <div className="seat-side">
                              {["A", "B", "C"].map(
                                (seatLetter) =>
                                  renderSeat(
                                    rowSeats,
                                    seatLetter
                                  )
                              )}
                            </div>

                            <span className="seat-row-number">
                              {rowNumber}
                            </span>

                            <div className="seat-side">
                              {["D", "E", "F"].map(
                                (seatLetter) =>
                                  renderSeat(
                                    rowSeats,
                                    seatLetter
                                  )
                              )}
                            </div>
                          </div>
                        );
                      }
                    )}
                  </div>
                </div>
              )}

              {selectedSeatNumber && (
                <div className="selected-seat-summary">
                  <span>
                    SELECTED SEAT
                  </span>

                  <strong>
                    {selectedSeatNumber}
                  </strong>
                </div>
              )}
            </div>

            {error && (
              <div className="booking-error">
                {error}
              </div>
            )}

            <button
              type="submit"
              className="confirm-booking-button"
              disabled={
                loading ||
                seatLoading ||
                !selectedSeatNumber
              }
            >
              {loading
                ? "Confirming booking..."
                : selectedSeatNumber
                  ? `Confirm Booking - Seat ${selectedSeatNumber} - ${formatPrice(
                      selectedBooking.baseFare
                    )}`
                  : "Select a seat to continue"}
            </button>
          </form>

          <aside className="booking-summary-card">
            <span className="summary-label">
              YOUR FLIGHT
            </span>

            <div className="summary-flight-number">
              <div className="mini-plane">
                <Plane size={21} />
              </div>

              <div>
                <span>SKYLINK</span>

                <strong>
                  {
                    selectedBooking.flightNumber
                  }
                </strong>
              </div>
            </div>

            <div className="summary-route">
              <div>
                <strong>
                  {
                    selectedBooking.sourceIataCode
                  }
                </strong>

                <span>
                  {
                    selectedBooking.sourceCity
                  }
                </span>

                <small>
                  {formatTime(
                    selectedBooking.departureTime
                  )}
                </small>
              </div>

              <Plane size={21} />

              <div>
                <strong>
                  {
                    selectedBooking.destinationIataCode
                  }
                </strong>

                <span>
                  {
                    selectedBooking.destinationCity
                  }
                </span>

                <small>
                  {formatTime(
                    selectedBooking.arrivalTime
                  )}
                </small>
              </div>
            </div>

            <div className="summary-date">
              <CalendarDays size={18} />

              <span>
                {formatDate(
                  selectedBooking.departureTime
                )}
              </span>
            </div>

            <div className="summary-price-row">
              <div>
                <span>
                  {
                    selectedBooking.fareClass
                  }
                </span>

                <small>
                  1 passenger
                </small>
              </div>

              <strong>
                {formatPrice(
                  selectedBooking.baseFare
                )}
              </strong>
            </div>

            {selectedSeatNumber && (
              <div className="summary-price-row">
                <div>
                  <span>Selected seat</span>

                  <small>
                    Passenger 1
                  </small>
                </div>

                <strong>
                  {selectedSeatNumber}
                </strong>
              </div>
            )}

            <div className="summary-total">
              <span>Total</span>

              <strong>
                {formatPrice(
                  selectedBooking.baseFare
                )}
              </strong>
            </div>
          </aside>
        </div>
      </main>
    </div>
  );
}

export default BookingPage;
