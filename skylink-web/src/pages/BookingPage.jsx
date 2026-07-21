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

const travellerLabels = {
  ADULT: "Adult",
  CHILD: "Child",
  INFANT: "Infant",
};

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

const normalizeTravellerCount = (
  value,
  fallback = 0
) => {
  const parsedValue = Number(value);

  if (!Number.isFinite(parsedValue)) {
    return fallback;
  }

  return Math.max(
    0,
    Math.floor(parsedValue)
  );
};

const getRequestedPassengerTypes = (
  selectedBooking
) => {
  const requestedTravellers =
    selectedBooking?.requestedTravellers || {};
  const adultCount = Math.max(
    1,
    normalizeTravellerCount(
      requestedTravellers.adults,
      1
    )
  );
  const childCount = normalizeTravellerCount(
    requestedTravellers.children
  );
  const infantCount = normalizeTravellerCount(
    requestedTravellers.infants
  );

  return [
    ...Array(adultCount).fill("ADULT"),
    ...Array(childCount).fill("CHILD"),
    ...Array(infantCount).fill("INFANT"),
  ];
};

const createPassengerForm = (
  travellerType
) => ({
  travellerType,
  firstName: "",
  lastName: "",
  dateOfBirth: "",
  gender: "MALE",
  passportNumber: "",
  nationality: "INDIAN",
  seatNumber: "",
});

function BookingPage() {
  const navigate = useNavigate();

  const [selectedBooking] =
    useState(getStoredSelectedBooking);

  const [passengers, setPassengers] =
    useState(() =>
      getRequestedPassengerTypes(
        selectedBooking
      ).map(createPassengerForm)
    );

  const [activePassengerIndex, setActivePassengerIndex] =
    useState(0);

  const [seats, setSeats] =
    useState([]);

  const [seatLoading, setSeatLoading] =
    useState(false);

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState("");

  const [confirmedBooking, setConfirmedBooking] =
    useState(null);

  const passengerCount = passengers.length;
  const selectedSeatNumbers = passengers
    .map((passenger) => passenger.seatNumber)
    .filter(Boolean);
  const allSeatsSelected = passengers.every(
    (passenger) => Boolean(passenger.seatNumber)
  );
  const bookingTotal =
    Number(selectedBooking?.baseFare || 0) *
    passengerCount;

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

  const updatePassenger = (
    passengerIndex,
    field,
    value
  ) => {
    setPassengers((currentPassengers) =>
      currentPassengers.map(
        (passenger, index) =>
          index === passengerIndex
            ? {
                ...passenger,
                [field]: value,
              }
            : passenger
      )
    );
  };

  const createBooking = async (event) => {
    event.preventDefault();

    if (!selectedBooking) {
      return;
    }

    if (!allSeatsSelected) {
      setError(
        "Please select one seat for every passenger before confirming your booking."
      );

      return;
    }

    if (
      new Set(selectedSeatNumbers).size !==
      passengerCount
    ) {
      setError(
        "Each passenger must have a different seat."
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

          passengers: passengers.map(
            (passenger) => ({
              firstName:
                passenger.firstName.trim(),

              lastName:
                passenger.lastName.trim(),

              dateOfBirth:
                passenger.dateOfBirth,

              gender: passenger.gender,

              passportNumber:
                passenger.passportNumber.trim()
                  ? passenger.passportNumber
                      .trim()
                      .toUpperCase()
                  : null,

              nationality:
                passenger.nationality
                  .trim()
                  .toUpperCase(),

              seatNumber:
                passenger.seatNumber,
            })
          ),
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

    const assignedPassengerIndex =
      passengers.findIndex(
        (passenger) =>
          passenger.seatNumber ===
          seat.seatNumber
      );

    if (
      assignedPassengerIndex !== -1 &&
      assignedPassengerIndex !==
        activePassengerIndex
    ) {
      setError(
        `Seat ${seat.seatNumber} is already assigned to Passenger ${
          assignedPassengerIndex + 1
        }.`
      );

      return;
    }

    const nextPassengers = passengers.map(
      (passenger, index) =>
        index === activePassengerIndex
          ? {
              ...passenger,
              seatNumber:
                assignedPassengerIndex ===
                activePassengerIndex
                  ? ""
                  : seat.seatNumber,
            }
          : passenger
    );

    setPassengers(nextPassengers);
    setError("");

    if (
      assignedPassengerIndex ===
      activePassengerIndex
    ) {
      return;
    }

    const nextPassengerIndex =
      nextPassengers.findIndex(
        (passenger, index) =>
          index > activePassengerIndex &&
          !passenger.seatNumber
      );
    const firstUnassignedPassengerIndex =
      nextPassengers.findIndex(
        (passenger) =>
          !passenger.seatNumber
      );

    if (nextPassengerIndex !== -1) {
      setActivePassengerIndex(
        nextPassengerIndex
      );
    } else if (
      firstUnassignedPassengerIndex !== -1
    ) {
      setActivePassengerIndex(
        firstUnassignedPassengerIndex
      );
    }
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

    const assignedPassengerIndex =
      passengers.findIndex(
        (passenger) =>
          passenger.seatNumber ===
          seat.seatNumber
      );
    const selected =
      assignedPassengerIndex !== -1;

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
          assignedPassengerIndex !== -1
            ? `${seat.seatNumber} - Passenger ${
                assignedPassengerIndex + 1
              }`
            : unavailable
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
                <span>Seats</span>

                <strong>
                  {(
                    confirmedBooking.passengers ||
                    passengers
                  )
                    .map(
                      (passenger) =>
                        passenger.seatNumber
                    )
                    .filter(Boolean)
                    .join(", ")}
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
            {passengers.map(
              (passenger, passengerIndex) => {
                const passengerNumber =
                  passengerIndex + 1;
                const travellerLabel =
                  travellerLabels[
                    passenger.travellerType
                  ];

                return (
                  <section
                    className="passenger-details-section"
                    key={passengerNumber}
                  >
                    <div className="passenger-card-heading">
                      <div>
                        <UserRound size={21} />
                      </div>

                      <div>
                        <span>
                          PASSENGER {passengerNumber}
                          {" - "}
                          {travellerLabel.toUpperCase()}
                        </span>

                        <h3>
                          {passengerIndex === 0
                            ? "Primary passenger"
                            : `${travellerLabel} passenger`}
                        </h3>
                      </div>
                    </div>

                    <div className="passenger-form-grid">
                      <div className="booking-input-group">
                        <label
                          htmlFor={`firstName-${passengerIndex}`}
                        >
                          First name
                        </label>

                        <input
                          id={`firstName-${passengerIndex}`}
                          value={passenger.firstName}
                          onChange={(event) =>
                            updatePassenger(
                              passengerIndex,
                              "firstName",
                              event.target.value
                            )
                          }
                          placeholder="First name"
                          required
                        />
                      </div>

                      <div className="booking-input-group">
                        <label
                          htmlFor={`lastName-${passengerIndex}`}
                        >
                          Last name
                        </label>

                        <input
                          id={`lastName-${passengerIndex}`}
                          value={passenger.lastName}
                          onChange={(event) =>
                            updatePassenger(
                              passengerIndex,
                              "lastName",
                              event.target.value
                            )
                          }
                          placeholder="Last name"
                          required
                        />
                      </div>

                      <div className="booking-input-group">
                        <label
                          htmlFor={`dateOfBirth-${passengerIndex}`}
                        >
                          Date of birth
                        </label>

                        <input
                          id={`dateOfBirth-${passengerIndex}`}
                          type="date"
                          value={passenger.dateOfBirth}
                          onChange={(event) =>
                            updatePassenger(
                              passengerIndex,
                              "dateOfBirth",
                              event.target.value
                            )
                          }
                          required
                        />
                      </div>

                      <div className="booking-input-group">
                        <label
                          htmlFor={`gender-${passengerIndex}`}
                        >
                          Gender
                        </label>

                        <select
                          id={`gender-${passengerIndex}`}
                          value={passenger.gender}
                          onChange={(event) =>
                            updatePassenger(
                              passengerIndex,
                              "gender",
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
                        <label
                          htmlFor={`nationality-${passengerIndex}`}
                        >
                          Nationality
                        </label>

                        <input
                          id={`nationality-${passengerIndex}`}
                          value={passenger.nationality}
                          onChange={(event) =>
                            updatePassenger(
                              passengerIndex,
                              "nationality",
                              event.target.value
                            )
                          }
                          placeholder="INDIAN"
                          required
                        />
                      </div>

                      <div className="booking-input-group">
                        <label
                          htmlFor={`passportNumber-${passengerIndex}`}
                        >
                          Passport number
                        </label>

                        <input
                          id={`passportNumber-${passengerIndex}`}
                          value={passenger.passportNumber}
                          onChange={(event) =>
                            updatePassenger(
                              passengerIndex,
                              "passportNumber",
                              event.target.value
                            )
                          }
                          placeholder="Optional"
                        />
                      </div>
                    </div>
                  </section>
                );
              }
            )}

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
                  each of the {passengerCount}
                  {" "}passengers.
                </p>
              </div>

              <div
                className="passenger-seat-tabs"
                role="tablist"
                aria-label="Choose passenger for seat assignment"
              >
                {passengers.map(
                  (passenger, passengerIndex) => (
                    <button
                      type="button"
                      role="tab"
                      aria-selected={
                        activePassengerIndex ===
                        passengerIndex
                      }
                      className={
                        activePassengerIndex ===
                        passengerIndex
                          ? "active"
                          : ""
                      }
                      key={passengerIndex}
                      onClick={() => {
                        setActivePassengerIndex(
                          passengerIndex
                        );
                        setError("");
                      }}
                    >
                      <span>
                        Passenger {passengerIndex + 1}
                      </span>

                      <strong>
                        {passenger.seatNumber ||
                          "Choose seat"}
                      </strong>
                    </button>
                  )
                )}
              </div>

              <div className="active-seat-passenger">
                Selecting seat for
                <strong>
                  Passenger {activePassengerIndex + 1}
                  {" - "}
                  {
                    travellerLabels[
                      passengers[activePassengerIndex]
                        .travellerType
                    ]
                  }
                </strong>
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

              {selectedSeatNumbers.length > 0 && (
                <div className="selected-seat-summary">
                  <span>
                    {selectedSeatNumbers.length}/
                    {passengerCount} SEATS SELECTED
                  </span>

                  <strong>
                    {selectedSeatNumbers.join(", ")}
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
                !allSeatsSelected
              }
            >
              {loading
                ? "Confirming booking..."
                : allSeatsSelected
                  ? `Confirm Booking - ${passengerCount} Passenger${
                      passengerCount === 1 ? "" : "s"
                    } - ${formatPrice(
                      bookingTotal
                    )}`
                  : `Select ${
                      passengerCount -
                      selectedSeatNumbers.length
                    } more seat${
                      passengerCount -
                        selectedSeatNumbers.length ===
                      1
                        ? ""
                        : "s"
                    } to continue`}
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
                  {passengerCount} passenger
                  {passengerCount === 1
                    ? ""
                    : "s"}
                  {" × "}
                  {formatPrice(
                    selectedBooking.baseFare
                  )}
                </small>
              </div>

              <strong>
                {formatPrice(
                  bookingTotal
                )}
              </strong>
            </div>

            {passengers.map(
              (passenger, passengerIndex) =>
                passenger.seatNumber ? (
                  <div
                    className="summary-price-row"
                    key={passengerIndex}
                  >
                    <div>
                      <span>Selected seat</span>

                      <small>
                        Passenger {passengerIndex + 1}
                      </small>
                    </div>

                    <strong>
                      {passenger.seatNumber}
                    </strong>
                  </div>
                ) : null
            )}

            <div className="summary-total">
              <span>Total</span>

              <strong>
                {formatPrice(
                  bookingTotal
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
