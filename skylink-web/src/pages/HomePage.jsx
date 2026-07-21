import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  ArrowRightLeft,
  Bell,
  CalendarDays,
  CheckCircle2,
  ChevronDown,
  Clock3,
  CreditCard,
  Headphones,
  LogOut,
  MapPin,
  Minus,
  Plane,
  Plus,
  Search,
  ShieldCheck,
  SlidersHorizontal,
  Tag,
  User,
  UserPlus,
  UsersRound,
} from "lucide-react";
import api from "../api/axiosConfig";
import {
  clearAuth,
  getUser,
  isAuthenticated,
} from "../utils/auth";
import { skyLinkOffers } from "../data/offerData";

const SEARCH_STATE_KEY =
  "skylink_flight_search_state";

const SELECTED_BOOKING_KEY =
  "skylink_selected_booking";

const DEFAULT_SOURCE_IATA = "DEL";
const DEFAULT_DESTINATION_IATA = "BOM";
const DEFAULT_SOURCE_QUERY =
  "Delhi - Indira Gandhi International Airport (DEL)";
const DEFAULT_DESTINATION_QUERY =
  "Mumbai - Chhatrapati Shivaji International Airport (BOM)";

const specialFareOptions = [
  "Student",
  "Senior Citizen",
  "Armed Forces",
];

const cabinClassOptions = [
  "Economy",
  "Premium Economy",
  "Business",
  "First",
];

const offerCards = skyLinkOffers.slice(0, 4);

const hotelOfferCards = [
  {
    bank: "SkyLink Stays",
    title: "Best price hotels",
    text: "save more near airports",
    slug: "hdfc-domestic-emi",
  },
  {
    bank: "Weekend Deals",
    title: "Up to INR 1,200 off",
    text: "on quick city breaks",
    slug: "icici-domestic-emi",
  },
  {
    bank: "Family Stay",
    title: "Free breakfast",
    text: "on selected hotel partners",
    slug: "axis-priority-refund",
  },
];

const utilityTiles = [
  {
    title: "Flight Tracker",
    path: "/flight-status",
    Icon: Plane,
    label: "Live status",
  },
  {
    title: "Credit Card",
    path: "/travel-credit-card",
    Icon: CreditCard,
    label: "Offers",
  },
  {
    title: "Group Booking",
    path: "/group-booking",
    Icon: UsersRound,
    label: "10+ travellers",
  },
  {
    title: "Plan Trip",
    path: "/plan",
    Icon: MapPin,
    label: "Ideas",
  },
  {
    title: "Fare Alerts",
    path: "/fare-alerts",
    Icon: Bell,
    label: "Watch prices",
  },
];

const sortSummaries = {
  recommended:
    "Smart sort balances fare, travel time, departure comfort and seat availability.",
  price: "Flights are sorted from lowest fare to highest fare.",
  duration: "Flights are sorted by the shortest total journey time.",
  departure: "Flights are sorted by the earliest departure time.",
};

const getDateAfterDays = (days) => {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return date.toISOString().split("T")[0];
};

const getFallbackSearchState = () => ({
  sourceIataCode: DEFAULT_SOURCE_IATA,
  destinationIataCode: DEFAULT_DESTINATION_IATA,
  sourceQuery: DEFAULT_SOURCE_QUERY,
  destinationQuery: DEFAULT_DESTINATION_QUERY,
  travelDate: getDateAfterDays(5),
  returnDate: getDateAfterDays(9),
  tripType: "ONE_WAY",
  travellers: {
    adults: 1,
    children: 0,
    infants: 0,
  },
  cabinClass: "Economy",
  specialFare: "",
  freeCancellation: true,
  flights: [],
  searched: false,
  error: "",
});

const getInitialSearchState = () => {
  const fallbackSearchState =
    getFallbackSearchState();

  try {
    const savedSearchState =
      sessionStorage.getItem(
        SEARCH_STATE_KEY
      );

    if (!savedSearchState) {
      return fallbackSearchState;
    }

    const parsedSearchState =
      JSON.parse(savedSearchState);

    return {
      ...fallbackSearchState,
      ...parsedSearchState,
      travellers: {
        ...fallbackSearchState.travellers,
        ...(parsedSearchState.travellers || {}),
      },
      flights: Array.isArray(
        parsedSearchState.flights
      )
        ? parsedSearchState.flights
        : [],
      searched: Boolean(
        parsedSearchState.searched
      ),
      error:
        parsedSearchState.error || "",
    };
  } catch (restoreError) {
    console.error(
      "Unable to restore flight search:",
      restoreError
    );

    sessionStorage.removeItem(
      SEARCH_STATE_KEY
    );

    return fallbackSearchState;
  }
};

function HomePage() {
  const navigate = useNavigate();
  const authenticated =
    isAuthenticated();
  const currentUser =
    getUser();

  const sourceWrapperRef =
    useRef(null);
  const destinationWrapperRef =
    useRef(null);
  const travellerWrapperRef =
    useRef(null);

  const [initialSearchState] =
    useState(getInitialSearchState);

  const [sourceIataCode, setSourceIataCode] =
    useState(
      initialSearchState.sourceIataCode
    );
  const [
    destinationIataCode,
    setDestinationIataCode,
  ] = useState(
    initialSearchState.destinationIataCode
  );
  const [sourceQuery, setSourceQuery] =
    useState(
      initialSearchState.sourceQuery
    );
  const [
    destinationQuery,
    setDestinationQuery,
  ] = useState(
    initialSearchState.destinationQuery
  );
  const [
    sourceSuggestions,
    setSourceSuggestions,
  ] = useState([]);
  const [
    destinationSuggestions,
    setDestinationSuggestions,
  ] = useState([]);
  const [
    sourceDropdownOpen,
    setSourceDropdownOpen,
  ] = useState(false);
  const [
    destinationDropdownOpen,
    setDestinationDropdownOpen,
  ] = useState(false);
  const [
    sourceSearching,
    setSourceSearching,
  ] = useState(false);
  const [
    destinationSearching,
    setDestinationSearching,
  ] = useState(false);
  const [travelDate, setTravelDate] =
    useState(
      initialSearchState.travelDate
    );
  const [returnDate, setReturnDate] =
    useState(
      initialSearchState.returnDate
    );
  const [tripType, setTripType] =
    useState(
      initialSearchState.tripType
    );
  const [travellers, setTravellers] =
    useState(
      initialSearchState.travellers
    );
  const [cabinClass, setCabinClass] =
    useState(
      initialSearchState.cabinClass
    );
  const [specialFare, setSpecialFare] =
    useState(
      initialSearchState.specialFare
    );
  const [
    freeCancellation,
    setFreeCancellation,
  ] = useState(
    initialSearchState.freeCancellation
  );
  const [
    travellerPanelOpen,
    setTravellerPanelOpen,
  ] = useState(false);
  const [sortBy, setSortBy] =
    useState("recommended");
  const [fareClassFilter, setFareClassFilter] =
    useState("ALL");
  const [priceLimit, setPriceLimit] =
    useState(0);
  const [flights, setFlights] =
    useState(
      initialSearchState.flights
    );
  const [loading, setLoading] =
    useState(false);
  const [searched, setSearched] =
    useState(
      initialSearchState.searched
    );
  const [error, setError] =
    useState(
      initialSearchState.error
    );
  const [
    activeOfferTab,
    setActiveOfferTab,
  ] = useState("Flights");

  const totalTravellers =
    travellers.adults +
    travellers.children +
    travellers.infants;

  const displayedOfferCards = useMemo(() => {
    if (activeOfferTab === "Hotels") {
      return hotelOfferCards;
    }

    if (activeOfferTab === "Bank Offers") {
      return skyLinkOffers;
    }

    return offerCards;
  }, [activeOfferTab]);

  const lowestFare = useMemo(() => {
    const fares = flights.flatMap(
      (flight) =>
        Array.isArray(flight.fares)
          ? flight.fares
          : []
    );

    if (fares.length === 0) {
      return 0;
    }

    return Math.min(
      ...fares.map((fare) =>
        Number(fare.baseFare)
      )
    );
  }, [flights]);

  const highestFare = useMemo(() => {
    const fares = flights.flatMap(
      (flight) =>
        Array.isArray(flight.fares)
          ? flight.fares
          : []
    );

    if (fares.length === 0) {
      return 0;
    }

    return Math.max(
      ...fares.map((fare) =>
        Number(fare.baseFare)
      )
    );
  }, [flights]);

  const filteredFlights = useMemo(() => {
    const effectivePriceLimit =
      priceLimit || highestFare;

    const withFilteredFares = flights
      .map((flight) => {
        const filteredFares = (
          flight.fares || []
        ).filter((fare) => {
          const matchesClass =
            fareClassFilter === "ALL" ||
            fare.fareClass === fareClassFilter;

          const matchesPrice =
            !effectivePriceLimit ||
            Number(fare.baseFare) <=
              effectivePriceLimit;

          return (
            matchesClass &&
            matchesPrice
          );
        });

        return {
          ...flight,
          fares: filteredFares,
          lowestFare: filteredFares.length
            ? Math.min(
                ...filteredFares.map((fare) =>
                  Number(fare.baseFare)
                )
              )
            : Number.POSITIVE_INFINITY,
        };
      })
      .filter(
        (flight) => flight.fares.length > 0
      );

    const getAvailableSeats = (flight) =>
      Math.max(
        0,
        ...(flight.fares || []).map((fare) =>
          Number(fare.availableSeats) || 0
        )
      );

    const getRecommendedScore = (flight) => {
      const departure = new Date(flight.departureTime);
      const departureHour = Number.isNaN(departure.getTime())
        ? 12
        : departure.getHours();
      const fareScore = Number.isFinite(flight.lowestFare)
        ? flight.lowestFare * 0.7
        : (highestFare || 0) * 0.7;
      const durationScore =
        (Number(flight.estimatedDurationMinutes) || 0) * 8;
      const comfortTimePenalty = Math.abs(departureHour - 10) * 280;
      const seatBoost = getAvailableSeats(flight) * 45;

      return fareScore + durationScore + comfortTimePenalty - seatBoost;
    };

    return [...withFilteredFares].sort(
      (firstFlight, secondFlight) => {
        if (sortBy === "price") {
          return (
            firstFlight.lowestFare -
            secondFlight.lowestFare
          );
        }

        if (sortBy === "duration") {
          return (
            firstFlight.estimatedDurationMinutes -
            secondFlight.estimatedDurationMinutes
          );
        }

        if (sortBy === "departure") {
          return (
            new Date(
              firstFlight.departureTime
            ) -
            new Date(
              secondFlight.departureTime
            )
          );
        }

        return (
          getRecommendedScore(firstFlight) -
          getRecommendedScore(secondFlight)
        );
      }
    );
  }, [
    fareClassFilter,
    flights,
    highestFare,
    priceLimit,
    sortBy,
  ]);

  const fareClassFilters = useMemo(() => {
    const fareClasses = new Set();

    flights.forEach((flight) => {
      (flight.fares || []).forEach(
        (fare) =>
          fareClasses.add(fare.fareClass)
      );
    });

    return ["ALL", ...fareClasses];
  }, [flights]);

  useEffect(() => {
    const searchState = {
      sourceIataCode,
      destinationIataCode,
      sourceQuery,
      destinationQuery,
      travelDate,
      returnDate,
      tripType,
      travellers,
      cabinClass,
      specialFare,
      freeCancellation,
      flights,
      searched,
      error,
    };

    sessionStorage.setItem(
      SEARCH_STATE_KEY,
      JSON.stringify(searchState)
    );
  }, [
    cabinClass,
    destinationIataCode,
    destinationQuery,
    error,
    flights,
    freeCancellation,
    returnDate,
    searched,
    sourceIataCode,
    sourceQuery,
    specialFare,
    travelDate,
    travellers,
    tripType,
  ]);

  useEffect(() => {
    if (!initialSearchState.searched) {
      return;
    }

    const timer = window.setTimeout(() => {
      document
        .querySelector(".results-section")
        ?.scrollIntoView({
          behavior: "auto",
          block: "start",
        });
    }, 0);

    return () => window.clearTimeout(timer);
  }, [initialSearchState.searched]);

  useEffect(() => {
    const handleOutsideClick = (
      event
    ) => {
      if (
        sourceWrapperRef.current &&
        !sourceWrapperRef.current.contains(
          event.target
        )
      ) {
        setSourceDropdownOpen(false);
      }

      if (
        destinationWrapperRef.current &&
        !destinationWrapperRef.current.contains(
          event.target
        )
      ) {
        setDestinationDropdownOpen(false);
      }

      if (
        travellerWrapperRef.current &&
        !travellerWrapperRef.current.contains(
          event.target
        )
      ) {
        setTravellerPanelOpen(false);
      }
    };

    document.addEventListener(
      "mousedown",
      handleOutsideClick
    );

    return () => {
      document.removeEventListener(
        "mousedown",
        handleOutsideClick
      );
    };
  }, []);

  useEffect(() => {
    const normalizedQuery =
      sourceQuery.trim();

    if (
      normalizedQuery.length < 2 ||
      (sourceIataCode &&
        normalizedQuery.endsWith(
          `(${sourceIataCode})`
        ))
    ) {
      return;
    }

    const timer = window.setTimeout(
      async () => {
        setSourceSearching(true);
        setSourceDropdownOpen(true);

        try {
          const response = await api.get(
            "/airports/search",
            {
              params: {
                query: normalizedQuery,
              },
            }
          );

          setSourceSuggestions(
            Array.isArray(response.data)
              ? response.data.slice(0, 8)
              : []
          );
        } catch (requestError) {
          console.error(requestError);
          setSourceSuggestions([]);
        } finally {
          setSourceSearching(false);
        }
      },
      350
    );

    return () => window.clearTimeout(timer);
  }, [sourceIataCode, sourceQuery]);

  useEffect(() => {
    const normalizedQuery =
      destinationQuery.trim();

    if (
      normalizedQuery.length < 2 ||
      (destinationIataCode &&
        normalizedQuery.endsWith(
          `(${destinationIataCode})`
        ))
    ) {
      return;
    }

    const timer = window.setTimeout(
      async () => {
        setDestinationSearching(true);
        setDestinationDropdownOpen(true);

        try {
          const response = await api.get(
            "/airports/search",
            {
              params: {
                query: normalizedQuery,
              },
            }
          );

          setDestinationSuggestions(
            Array.isArray(response.data)
              ? response.data.slice(0, 8)
              : []
          );
        } catch (requestError) {
          console.error(requestError);
          setDestinationSuggestions([]);
        } finally {
          setDestinationSearching(false);
        }
      },
      350
    );

    return () => window.clearTimeout(timer);
  }, [
    destinationIataCode,
    destinationQuery,
  ]);

  const clearSearchResults = () => {
    setFlights([]);
    setSearched(false);
    setError("");
    setPriceLimit(0);
  };

  const getAirportCity = (airport) => {
    return (
      airport.city ||
      airport.municipality ||
      "Unknown City"
    );
  };

  const getAirportCountry = (airport) => {
    return (
      airport.country ||
      airport.countryName ||
      airport.countryCode ||
      ""
    );
  };

  const getAirportDisplayName = (
    airport
  ) => {
    const city =
      getAirportCity(airport);
    const airportName =
      airport.airportName || "Airport";
    const iataCode =
      airport.iataCode
        ?.trim()
        .toUpperCase() || "";

    return `${city} - ${airportName} (${iataCode})`;
  };

  const selectSourceAirport = (
    airport
  ) => {
    const iataCode =
      airport.iataCode
        .trim()
        .toUpperCase();

    clearSearchResults();
    setSourceIataCode(iataCode);
    setSourceQuery(
      getAirportDisplayName(airport)
    );
    setSourceSuggestions([]);
    setSourceDropdownOpen(false);
  };

  const selectDestinationAirport = (
    airport
  ) => {
    const iataCode =
      airport.iataCode
        .trim()
        .toUpperCase();

    clearSearchResults();
    setDestinationIataCode(iataCode);
    setDestinationQuery(
      getAirportDisplayName(airport)
    );
    setDestinationSuggestions([]);
    setDestinationDropdownOpen(false);
  };

  const handleSourceChange = (event) => {
    const nextValue =
      event.target.value;

    clearSearchResults();
    setSourceQuery(nextValue);
    setSourceIataCode("");

    if (nextValue.trim().length < 2) {
      setSourceSuggestions([]);
      setSourceDropdownOpen(false);
      return;
    }

    setSourceDropdownOpen(true);
  };

  const handleDestinationChange = (
    event
  ) => {
    const nextValue =
      event.target.value;

    clearSearchResults();
    setDestinationQuery(nextValue);
    setDestinationIataCode("");

    if (nextValue.trim().length < 2) {
      setDestinationSuggestions([]);
      setDestinationDropdownOpen(false);
      return;
    }

    setDestinationDropdownOpen(true);
  };

  const updateTravellerCount = (
    travellerType,
    direction
  ) => {
    clearSearchResults();

    setTravellers((currentTravellers) => {
      const minimum =
        travellerType === "adults" ? 1 : 0;
      const maximum =
        travellerType === "infants"
          ? currentTravellers.adults
          : 9;
      const currentValue =
        currentTravellers[travellerType];
      const nextValue =
        direction === "increase"
          ? Math.min(
              maximum,
              currentValue + 1
            )
          : Math.max(
              minimum,
              currentValue - 1
            );

      const nextTravellers = {
        ...currentTravellers,
        [travellerType]: nextValue,
      };

      if (
        travellerType === "adults" &&
        nextTravellers.infants >
          nextTravellers.adults
      ) {
        nextTravellers.infants =
          nextTravellers.adults;
      }

      return nextTravellers;
    });
  };

  const handleTravelDateChange = (
    event
  ) => {
    clearSearchResults();
    setTravelDate(event.target.value);

    if (
      returnDate &&
      event.target.value > returnDate
    ) {
      setReturnDate(event.target.value);
    }
  };

  const handleReturnDateChange = (
    event
  ) => {
    clearSearchResults();
    setReturnDate(event.target.value);
  };

  const swapAirports = () => {
    clearSearchResults();

    setSourceIataCode(
      destinationIataCode
    );
    setSourceQuery(destinationQuery);
    setDestinationIataCode(
      sourceIataCode
    );
    setDestinationQuery(sourceQuery);
    setSourceSuggestions([]);
    setDestinationSuggestions([]);
    setSourceDropdownOpen(false);
    setDestinationDropdownOpen(false);
  };

  const searchFlights = async (event) => {
    event.preventDefault();

    if (
      !sourceIataCode ||
      !destinationIataCode
    ) {
      setFlights([]);
      setSearched(false);
      setError(
        "Please select departure and destination airports from the suggestions."
      );
      return;
    }

    if (
      sourceIataCode ===
      destinationIataCode
    ) {
      setFlights([]);
      setSearched(false);
      setError(
        "Departure and destination airports must be different."
      );
      return;
    }

    setLoading(true);
    setError("");
    setSearched(false);
    setFlights([]);
    setPriceLimit(0);

    try {
      const response = await api.post(
        "/flights/search",
        {
          sourceIataCode,
          destinationIataCode,
          travelDate,
        }
      );

      const foundFlights = Array.isArray(
        response.data
      )
        ? response.data
        : [];

      setFlights(foundFlights);
      setSearched(true);
    } catch (requestError) {
      console.error(requestError);

      setFlights([]);
      setSearched(true);
      setError(
        requestError.response?.data?.message ||
          "Unable to search flights. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleFareSelection = (
    flight,
    fare
  ) => {
    if (
      fare.availableSeats < totalTravellers
    ) {
      setError(
        `Only ${fare.availableSeats} seats are available for this fare. Please reduce the traveller count or choose another fare.`
      );
      return;
    }

    const selectedBooking = {
      flightScheduleId:
        flight.flightScheduleId,
      flightNumber:
        flight.flightNumber,
      flightRouteId:
        flight.flightRouteId,
      sourceIataCode:
        flight.sourceIataCode,
      sourceAirportName:
        flight.sourceAirportName,
      sourceCity:
        flight.sourceCity,
      sourceCountryCode:
        flight.sourceCountryCode,
      destinationIataCode:
        flight.destinationIataCode,
      destinationAirportName:
        flight.destinationAirportName,
      destinationCity:
        flight.destinationCity,
      destinationCountryCode:
        flight.destinationCountryCode,
      distanceKm:
        flight.distanceKm,
      estimatedDurationMinutes:
        flight.estimatedDurationMinutes,
      aircraftId:
        flight.aircraftId,
      aircraftRegistrationNumber:
        flight.aircraftRegistrationNumber,
      aircraftManufacturer:
        flight.aircraftManufacturer,
      aircraftModel:
        flight.aircraftModel,
      departureTime:
        flight.departureTime,
      arrivalTime:
        flight.arrivalTime,
      fareId: fare.id,
      fareClass: fare.fareClass,
      baseFare: fare.baseFare,
      availableSeats:
        fare.availableSeats,
      requestedTravellers:
        travellers,
      cabinClass,
      tripType,
      returnDate:
        tripType === "ROUND_TRIP"
          ? returnDate
          : null,
    };

    localStorage.setItem(
      SELECTED_BOOKING_KEY,
      JSON.stringify(selectedBooking)
    );

    if (!isAuthenticated()) {
      navigate("/login", {
        state: {
          redirectTo: "/booking",
        },
      });
      return;
    }

    navigate("/booking");
  };

  const handleMyBookings = () => {
    if (!isAuthenticated()) {
      navigate("/login", {
        state: {
          redirectTo: "/my-bookings",
        },
      });
      return;
    }

    navigate("/my-bookings");
  };

  const handleUserDashboard = () => {
    if (!isAuthenticated()) {
      navigate("/login");
      return;
    }

    navigate("/user");
  };

  const handleLogout = () => {
    clearAuth();
    localStorage.removeItem(
      SELECTED_BOOKING_KEY
    );
    navigate("/");
  };

  const handleUtilityNavigation = (event, path, title) => {
    sessionStorage.setItem(
      "skylink_last_utility_action",
      JSON.stringify({
        title,
        path,
        openedAt: new Date().toISOString(),
      })
    );

    if (path.startsWith("#")) {
      event.preventDefault();
      document
        .querySelector(path)
        ?.scrollIntoView({
          behavior: "smooth",
          block: "start",
        });

      return;
    }
  };

  const getUserDisplayName = () => {
    if (currentUser?.fullName) {
      return currentUser.fullName;
    }

    if (
      currentUser?.firstName &&
      currentUser?.lastName
    ) {
      return `${currentUser.firstName} ${currentUser.lastName}`;
    }

    if (currentUser?.firstName) {
      return currentUser.firstName;
    }

    if (currentUser?.email) {
      return currentUser.email;
    }

    return "Passenger";
  };

  const formatTime = (dateTime) => {
    return new Date(
      dateTime
    ).toLocaleTimeString("en-IN", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    });
  };

  const formatDate = (dateTime) => {
    return new Date(
      dateTime
    ).toLocaleDateString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
  };

  const formatDuration = (minutes) => {
    const hours = Math.floor(
      minutes / 60
    );
    const remainingMinutes =
      minutes % 60;

    return `${hours}h ${remainingMinutes}m`;
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

  const renderAirportSuggestions = (
    suggestions,
    searching,
    selectAirport
  ) => {
    return (
      <div className="airport-suggestions">
        {searching && (
          <div className="airport-search-state">
            <Search size={17} />
            <span>Searching airports...</span>
          </div>
        )}

        {!searching &&
          suggestions.length === 0 && (
            <div className="airport-search-state">
              <MapPin size={17} />
              <span>No airports found</span>
            </div>
          )}

        {!searching &&
          suggestions.map((airport) => {
            const city =
              getAirportCity(airport);
            const country =
              getAirportCountry(airport);

            return (
              <button
                type="button"
                className="airport-suggestion-item"
                key={airport.id}
                onClick={() =>
                  selectAirport(airport)
                }
              >
                <div className="airport-suggestion-icon">
                  <Plane size={17} />
                </div>

                <div className="airport-suggestion-info">
                  <strong>
                    {city}
                    {country
                      ? `, ${country}`
                      : ""}
                  </strong>

                  <span>
                    {airport.airportName}
                  </span>
                </div>

                <div className="airport-suggestion-code">
                  {airport.iataCode}
                </div>
              </button>
            );
          })}
      </div>
    );
  };

  const renderTravellerRow = (
    type,
    label,
    helper
  ) => {
    const value = travellers[type];
    const minimum =
      type === "adults" ? 1 : 0;
    const maximum =
      type === "infants"
        ? travellers.adults
        : 9;

    return (
      <div className="traveller-row">
        <div>
          <strong>{label}</strong>
          <span>{helper}</span>
        </div>

        <div className="traveller-stepper">
          <button
            type="button"
            onClick={() =>
              updateTravellerCount(
                type,
                "decrease"
              )
            }
            disabled={value <= minimum}
            aria-label={`Decrease ${label}`}
          >
            <Minus size={15} />
          </button>

          <span>{value}</span>

          <button
            type="button"
            onClick={() =>
              updateTravellerCount(
                type,
                "increase"
              )
            }
            disabled={value >= maximum}
            aria-label={`Increase ${label}`}
          >
            <Plus size={15} />
          </button>
        </div>
      </div>
    );
  };

  return (
    <div className="app">
      <nav className="navbar">
        <div
          className="brand"
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

        <div className="nav-links">
          <a href="#search">Search Flights</a>

          <button
            type="button"
            className="nav-text-button"
            onClick={handleMyBookings}
          >
            My Bookings
          </button>

          <button
            type="button"
            className="nav-text-button"
            onClick={() =>
              navigate("/flight-status")
            }
          >
            Flight Status
          </button>

          {authenticated ? (
            <>
              <button
                type="button"
                className="nav-user"
                onClick={handleUserDashboard}
              >
                <div className="nav-user-icon">
                  <User size={17} />
                </div>

                <div className="nav-user-details">
                  <span>SIGNED IN AS</span>
                  <strong>
                    {getUserDisplayName()}
                  </strong>
                </div>
              </button>

              <button
                type="button"
                className="logout-button"
                onClick={handleLogout}
              >
                <LogOut size={17} />
                Logout
              </button>
            </>
          ) : (
            <>
              <button
                type="button"
                className="login-button"
                onClick={() =>
                  navigate("/login")
                }
              >
                Login
              </button>

              <button
                type="button"
                className="logout-button"
                onClick={() =>
                  navigate("/register")
                }
              >
                <UserPlus size={17} />
                Create Account
              </button>
            </>
          )}
        </div>
      </nav>

      <main>
        <section
          className="hero"
          id="search"
        >
          <div className="hero-glow hero-glow-one" />
          <div className="hero-glow hero-glow-two" />

          <div className="hero-content">
            <div className="hero-badge">
              <Plane size={16} />
              Smart Flight Booking Platform
            </div>

            <h2>
              Your journey begins
              <span> above the clouds.</span>
            </h2>

            <p>
              Search flights, compare fares and manage your journey through one
              seamless SkyLink experience.
            </p>

            <form
              className="search-card search-card-advanced"
              onSubmit={searchFlights}
            >
              <div className="trip-toggle">
                <button
                  type="button"
                  className={
                    tripType === "ONE_WAY"
                      ? "active"
                      : ""
                  }
                  onClick={() => {
                    clearSearchResults();
                    setTripType("ONE_WAY");
                  }}
                >
                  One Way
                </button>

                <button
                  type="button"
                  className={
                    tripType === "ROUND_TRIP"
                      ? "active"
                      : ""
                  }
                  onClick={() => {
                    clearSearchResults();
                    setTripType("ROUND_TRIP");
                  }}
                >
                  Round Trip
                </button>
              </div>

              <div className="primary-search-grid">
                <div
                  className="airport-search-wrapper"
                  ref={sourceWrapperRef}
                >
                  <div className="search-field">
                    <label>
                      <MapPin size={17} />
                      FROM
                    </label>

                    <input
                      value={sourceQuery}
                      onChange={handleSourceChange}
                      onFocus={() => {
                        if (
                          sourceSuggestions.length > 0
                        ) {
                          setSourceDropdownOpen(true);
                        }
                      }}
                      placeholder="Search city, airport or IATA"
                      autoComplete="off"
                      required
                    />

                    <span>Departure airport</span>
                  </div>

                  {sourceDropdownOpen &&
                    renderAirportSuggestions(
                      sourceSuggestions,
                      sourceSearching,
                      selectSourceAirport
                    )}
                </div>

                <button
                  type="button"
                  className="swap-button"
                  onClick={swapAirports}
                  aria-label="Swap airports"
                >
                  <ArrowRightLeft size={20} />
                </button>

                <div
                  className="airport-search-wrapper"
                  ref={destinationWrapperRef}
                >
                  <div className="search-field">
                    <label>
                      <MapPin size={17} />
                      TO
                    </label>

                    <input
                      value={destinationQuery}
                      onChange={
                        handleDestinationChange
                      }
                      onFocus={() => {
                        if (
                          destinationSuggestions.length >
                          0
                        ) {
                          setDestinationDropdownOpen(
                            true
                          );
                        }
                      }}
                      placeholder="Search city, airport or IATA"
                      autoComplete="off"
                      required
                    />

                    <span>Destination airport</span>
                  </div>

                  {destinationDropdownOpen &&
                    renderAirportSuggestions(
                      destinationSuggestions,
                      destinationSearching,
                      selectDestinationAirport
                    )}
                </div>

                <div className="search-field date-field">
                  <label>
                    <CalendarDays size={17} />
                    DEPARTURE
                  </label>

                  <input
                    type="date"
                    value={travelDate}
                    min={getDateAfterDays(0)}
                    onChange={
                      handleTravelDateChange
                    }
                    required
                  />

                  <span>Travel date</span>
                </div>

                {tripType === "ROUND_TRIP" && (
                  <div className="search-field date-field">
                    <label>
                      <CalendarDays size={17} />
                      RETURN
                    </label>

                    <input
                      type="date"
                      value={returnDate}
                      min={travelDate}
                      onChange={
                        handleReturnDateChange
                      }
                      required
                    />

                    <span>Return date</span>
                  </div>
                )}

                <div
                  className="traveller-wrapper"
                  ref={travellerWrapperRef}
                >
                  <button
                    type="button"
                    className="traveller-field"
                    onClick={() =>
                      setTravellerPanelOpen(
                        (currentValue) =>
                          !currentValue
                      )
                    }
                  >
                    <label>
                      <UsersRound size={17} />
                      TRAVELLERS & CLASS
                    </label>

                    <strong>
                      {totalTravellers} Traveller
                      {totalTravellers === 1
                        ? ""
                        : "s"}
                      , {cabinClass}
                    </strong>

                    <ChevronDown size={18} />
                  </button>

                  {travellerPanelOpen && (
                    <div className="traveller-panel">
                      {renderTravellerRow(
                        "adults",
                        "Adults",
                        "12 yrs or above"
                      )}
                      {renderTravellerRow(
                        "children",
                        "Children",
                        "2 - 12 yrs"
                      )}
                      {renderTravellerRow(
                        "infants",
                        "Infants",
                        "0 - 2 yrs"
                      )}

                      <div className="cabin-options">
                        <span>Class</span>
                        <div>
                          {cabinClassOptions.map(
                            (option) => (
                              <button
                                type="button"
                                className={
                                  cabinClass === option
                                    ? "active"
                                    : ""
                                }
                                key={option}
                                onClick={() => {
                                  clearSearchResults();
                                  setCabinClass(option);
                                }}
                              >
                                {option}
                              </button>
                            )
                          )}
                        </div>
                      </div>

                      <button
                        type="button"
                        className="traveller-done-button"
                        onClick={() =>
                          setTravellerPanelOpen(false)
                        }
                      >
                        Done
                      </button>
                    </div>
                  )}
                </div>

                <button
                  type="submit"
                  className="search-button"
                  disabled={loading}
                >
                  <Search size={20} />
                  {loading
                    ? "Searching..."
                    : "Search Flights"}
                </button>
              </div>

              <div className="special-fares-row">
                <span>Special fares:</span>
                {specialFareOptions.map(
                  (option) => (
                    <button
                      type="button"
                      className={
                        specialFare === option
                          ? "active"
                          : ""
                      }
                      key={option}
                      onClick={() => {
                        clearSearchResults();
                        setSpecialFare(
                          specialFare === option
                            ? ""
                            : option
                        );
                      }}
                    >
                      {option}
                    </button>
                  )
                )}
              </div>

              <label className="free-cancel-row">
                <input
                  type="checkbox"
                  checked={freeCancellation}
                  onChange={(event) => {
                    clearSearchResults();
                    setFreeCancellation(
                      event.target.checked
                    );
                  }}
                />
                <strong>
                  Always opt for Free Cancellation
                </strong>
                <span>
                  INR 0 cancellation fee with instant refund support
                </span>
              </label>
            </form>
          </div>
        </section>

        <section className="home-utility-section">
          <div className="home-utility-container">
            <h3>Do More With SkyLink</h3>

            <div className="utility-tile-grid">
              {utilityTiles.map(
                ({ title, path, Icon, label }) => (
                  <Link
                    key={title}
                    to={path}
                    state={{
                      fromUtility: title,
                    }}
                    onClick={(event) =>
                      handleUtilityNavigation(
                        event,
                        path,
                        title
                      )
                    }
                  >
                    <Icon size={26} />
                    <strong>{title}</strong>
                    <span>{label}</span>
                  </Link>
                )
              )}
            </div>
          </div>
        </section>

        <section className="offers-section" id="offers">
          <div className="offers-header">
            <h3>Today's SkyLink Offers</h3>
            <div>
              {["Flights", "Hotels", "Bank Offers"].map(
                (tab) => (
                  <button
                    type="button"
                    className={
                      activeOfferTab === tab
                        ? "active"
                        : ""
                    }
                    key={tab}
                    onClick={() =>
                      setActiveOfferTab(tab)
                    }
                  >
                    {tab}
                  </button>
                )
              )}
              <button
                type="button"
                onClick={() => navigate("/offers")}
              >
                View all
              </button>
            </div>
          </div>

          <div className="offers-grid">
            {displayedOfferCards.map((offer) => (
              <article
                className="offer-card clickable-offer-card"
                key={`${activeOfferTab}-${offer.bank}-${offer.title}`}
                onClick={() =>
                  navigate(`/offers/${offer.slug}`)
                }
              >
                <Tag size={24} />
                <span>{offer.bank}</span>
                <h4>{offer.title}</h4>
                <p>{offer.text}</p>
                <button type="button">
                  Details
                  <ArrowRightLeft size={14} />
                </button>
              </article>
            ))}
          </div>
        </section>

        <section className="results-section">
          <div className="results-container">
            {error && (
              <div className="error-message">
                {error}
              </div>
            )}

            {searched && !error && (
              <>
                <div className="results-header">
                  <div>
                    <span>
                      AVAILABLE FLIGHTS
                    </span>

                    <h3>
                      {sourceIataCode} to{" "}
                      {destinationIataCode}
                    </h3>
                  </div>

                  <p>
                    {filteredFlights.length} of{" "}
                    {flights.length} flight
                    {flights.length === 1
                      ? ""
                      : "s"}{" "}
                    shown
                  </p>
                </div>

                {flights.length > 0 && (
                  <div className="results-tools">
                    <div className="sort-panel">
                      <div className="sort-tabs">
                        <button
                          type="button"
                          className={
                            sortBy === "recommended"
                              ? "active"
                              : ""
                          }
                          onClick={() =>
                            setSortBy("recommended")
                          }
                        >
                          Smart
                          <span>Recommended</span>
                        </button>

                        <button
                          type="button"
                          className={
                            sortBy === "price"
                              ? "active"
                              : ""
                          }
                          onClick={() =>
                            setSortBy("price")
                          }
                        >
                          Price
                          <span>Low to High</span>
                        </button>

                        <button
                          type="button"
                          className={
                            sortBy === "duration"
                              ? "active"
                              : ""
                          }
                          onClick={() =>
                            setSortBy("duration")
                          }
                        >
                          Fastest
                          <span>Shortest First</span>
                        </button>

                        <button
                          type="button"
                          className={
                            sortBy === "departure"
                              ? "active"
                              : ""
                          }
                          onClick={() =>
                            setSortBy("departure")
                          }
                        >
                          Departure
                          <span>Earliest First</span>
                        </button>
                      </div>

                      <p
                        className="sort-feedback"
                        role="status"
                      >
                        {sortSummaries[sortBy]}
                      </p>
                    </div>

                    <div className="filter-panel">
                      <div className="filter-title">
                        <SlidersHorizontal size={18} />
                        <strong>Filters</strong>
                      </div>

                      <div className="filter-chip-row">
                        {fareClassFilters.map(
                          (fareClass) => (
                            <button
                              type="button"
                              className={
                                fareClassFilter ===
                                fareClass
                                  ? "active"
                                  : ""
                              }
                              key={fareClass}
                              onClick={() =>
                                setFareClassFilter(
                                  fareClass
                                )
                              }
                            >
                              {fareClass === "ALL"
                                ? "All classes"
                                : fareClass}
                            </button>
                          )
                        )}
                      </div>

                      {highestFare > 0 && (
                        <label className="price-range">
                          <span>
                            Price up to{" "}
                            {formatPrice(
                              priceLimit ||
                                highestFare
                            )}
                          </span>
                          <input
                            type="range"
                            min={lowestFare}
                            max={highestFare}
                            value={
                              priceLimit ||
                              highestFare
                            }
                            onChange={(event) =>
                              setPriceLimit(
                                Number(
                                  event.target.value
                                )
                              )
                            }
                          />
                        </label>
                      )}
                    </div>
                  </div>
                )}
              </>
            )}

            {searched &&
              !error &&
              filteredFlights.map((flight) => (
                <article
                  className="flight-card"
                  key={flight.flightScheduleId}
                >
                  <div className="flight-main">
                    <div className="flight-brand">
                      <div className="mini-plane">
                        <Plane size={22} />
                      </div>

                      <div>
                        <span>SKYLINK</span>
                        <strong>
                          {flight.flightNumber}
                        </strong>
                      </div>
                    </div>

                    <div className="flight-route">
                      <div className="airport-time">
                        <strong>
                          {formatTime(
                            flight.departureTime
                          )}
                        </strong>

                        <h4>
                          {flight.sourceIataCode}
                        </h4>

                        <span>
                          {flight.sourceCity}
                        </span>
                      </div>

                      <div className="route-line">
                        <span>
                          {formatDuration(
                            flight.estimatedDurationMinutes
                          )}
                        </span>

                        <div>
                          <i />
                          <Plane size={18} />
                          <i />
                        </div>

                        <small>NON STOP</small>
                      </div>

                      <div className="airport-time">
                        <strong>
                          {formatTime(
                            flight.arrivalTime
                          )}
                        </strong>

                        <h4>
                          {
                            flight.destinationIataCode
                          }
                        </h4>

                        <span>
                          {flight.destinationCity}
                        </span>
                      </div>
                    </div>

                    <div className="flight-date">
                      <CalendarDays size={18} />
                      <span>
                        {formatDate(
                          flight.departureTime
                        )}
                      </span>
                    </div>
                  </div>

                  <div className="flight-perks">
                    <span>
                      <CheckCircle2 size={16} />
                      Free cancellation{" "}
                      {freeCancellation
                        ? "selected"
                        : "available"}
                    </span>
                    <span>
                      <Clock3 size={16} />
                      {formatDuration(
                        flight.estimatedDurationMinutes
                      )}
                    </span>
                    <span>
                      <Headphones size={16} />
                      Priority support
                    </span>
                    <span>
                      <ShieldCheck size={16} />
                      Secure booking
                    </span>
                  </div>

                  <div className="fare-grid">
                    {flight.fares.map((fare) => (
                      <div
                        className="fare-card"
                        key={fare.id}
                      >
                        <div>
                          <span className="fare-class">
                            {fare.fareClass}
                          </span>

                          <strong>
                            {formatPrice(
                              fare.baseFare
                            )}
                          </strong>

                          <small>
                            per passenger
                          </small>
                        </div>

                        <div className="fare-action">
                          <span>
                            {fare.availableSeats} seats
                            available
                          </span>

                          <button
                            type="button"
                            disabled={
                              fare.availableSeats <
                              totalTravellers
                            }
                            title={
                              fare.availableSeats <
                              totalTravellers
                                ? `Only ${fare.availableSeats} seats are available for ${totalTravellers} travellers`
                                : "Continue to passenger details"
                            }
                            onClick={() =>
                              handleFareSelection(
                                flight,
                                fare
                              )
                            }
                          >
                            {fare.availableSeats <
                            totalTravellers
                              ? "Not enough seats"
                              : "Select"}
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </article>
              ))}

            {searched &&
              !error &&
              flights.length > 0 &&
              filteredFlights.length === 0 && (
                <div className="empty-state">
                  <SlidersHorizontal size={42} />
                  <h3>No flights match filters</h3>
                  <p>
                    Increase the price limit or choose another fare class.
                  </p>
                </div>
              )}

            {searched &&
              !error &&
              flights.length === 0 && (
                <div className="empty-state">
                  <Plane size={42} />
                  <h3>No flights found</h3>
                  <p>
                    Try another route or choose a different travel date.
                  </p>
                </div>
              )}
          </div>
        </section>
      </main>
    </div>
  );
}

export default HomePage;
