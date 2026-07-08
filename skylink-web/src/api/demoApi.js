const DEMO_BOOKINGS_KEY = "skylink_demo_bookings";
const DEMO_PROFILE_KEY = "skylink_demo_profile";
const SELECTED_BOOKING_KEY = "skylink_selected_booking";

const airports = [
  {
    id: 1,
    iataCode: "DEL",
    icaoCode: "VIDP",
    airportName: "Indira Gandhi International Airport",
    city: "Delhi",
    country: "India",
    countryCode: "IN",
    aliases: ["New Delhi", "NCR"],
    latitude: 28.5562,
    longitude: 77.1,
  },
  {
    id: 2,
    iataCode: "BOM",
    icaoCode: "VABB",
    airportName: "Chhatrapati Shivaji Maharaj International Airport",
    city: "Mumbai",
    country: "India",
    countryCode: "IN",
    aliases: ["Bombay"],
    latitude: 19.0896,
    longitude: 72.8656,
  },
  {
    id: 3,
    iataCode: "BLR",
    icaoCode: "VOBL",
    airportName: "Kempegowda International Airport",
    city: "Bengaluru",
    country: "India",
    countryCode: "IN",
    aliases: ["Bangalore"],
    latitude: 13.1986,
    longitude: 77.7066,
  },
  {
    id: 4,
    iataCode: "HYD",
    icaoCode: "VOHS",
    airportName: "Rajiv Gandhi International Airport",
    city: "Hyderabad",
    country: "India",
    countryCode: "IN",
    aliases: [],
    latitude: 17.24,
    longitude: 78.4294,
  },
  {
    id: 5,
    iataCode: "CCU",
    icaoCode: "VECC",
    airportName: "Netaji Subhas Chandra Bose International Airport",
    city: "Kolkata",
    country: "India",
    countryCode: "IN",
    aliases: ["Calcutta"],
    latitude: 22.6547,
    longitude: 88.4467,
  },
  {
    id: 6,
    iataCode: "MAA",
    icaoCode: "VOMM",
    airportName: "Chennai International Airport",
    city: "Chennai",
    country: "India",
    countryCode: "IN",
    aliases: ["Madras"],
    latitude: 12.9941,
    longitude: 80.1709,
  },
  {
    id: 7,
    iataCode: "LHR",
    icaoCode: "EGLL",
    airportName: "Heathrow Airport",
    city: "London",
    country: "United Kingdom",
    countryCode: "GB",
    aliases: ["LON", "Heathrow", "UK"],
    latitude: 51.47,
    longitude: -0.4543,
  },
  {
    id: 8,
    iataCode: "LGW",
    icaoCode: "EGKK",
    airportName: "Gatwick Airport",
    city: "London",
    country: "United Kingdom",
    countryCode: "GB",
    aliases: ["LON", "Gatwick", "UK"],
    latitude: 51.1537,
    longitude: -0.1821,
  },
  {
    id: 9,
    iataCode: "JFK",
    icaoCode: "KJFK",
    airportName: "John F. Kennedy International Airport",
    city: "New York",
    country: "United States",
    countryCode: "US",
    aliases: ["NYC", "New York City", "Kennedy", "USA"],
    latitude: 40.6413,
    longitude: -73.7781,
  },
  {
    id: 10,
    iataCode: "EWR",
    icaoCode: "KEWR",
    airportName: "Newark Liberty International Airport",
    city: "New York",
    country: "United States",
    countryCode: "US",
    aliases: ["NYC", "Newark", "New Jersey", "USA"],
    latitude: 40.6895,
    longitude: -74.1745,
  },
  {
    id: 11,
    iataCode: "DXB",
    icaoCode: "OMDB",
    airportName: "Dubai International Airport",
    city: "Dubai",
    country: "United Arab Emirates",
    countryCode: "AE",
    aliases: ["UAE"],
    latitude: 25.2532,
    longitude: 55.3657,
  },
  {
    id: 12,
    iataCode: "SIN",
    icaoCode: "WSSS",
    airportName: "Singapore Changi Airport",
    city: "Singapore",
    country: "Singapore",
    countryCode: "SG",
    aliases: ["Changi"],
    latitude: 1.3644,
    longitude: 103.9915,
  },
];

const routeDistances = {
  "DEL-BOM": 1148,
  "DEL-BLR": 1740,
  "DEL-HYD": 1260,
  "DEL-CCU": 1310,
  "DEL-MAA": 1760,
  "BOM-BLR": 835,
  "BOM-HYD": 620,
  "BOM-CCU": 1660,
  "BOM-MAA": 1030,
  "BLR-HYD": 500,
  "BLR-CCU": 1560,
  "BLR-MAA": 290,
  "HYD-CCU": 1180,
  "HYD-MAA": 520,
  "CCU-MAA": 1360,
};

const aircraft = [
  {
    aircraftId: 101,
    aircraftRegistrationNumber: "VT-SLK",
    aircraftManufacturer: "Airbus",
    aircraftModel: "A320neo",
  },
  {
    aircraftId: 102,
    aircraftRegistrationNumber: "VT-SKY",
    aircraftManufacturer: "Boeing",
    aircraftModel: "737 MAX 8",
  },
  {
    aircraftId: 103,
    aircraftRegistrationNumber: "VT-LNK",
    aircraftManufacturer: "Airbus",
    aircraftModel: "A321neo",
  },
];

const fareMultipliers = {
  ECONOMY: 1,
  PREMIUM_ECONOMY: 1.45,
  BUSINESS: 2.6,
  FIRST: 4.2,
};

const getJsonBody = (config) => {
  if (!config.data) {
    return {};
  }

  if (typeof config.data === "string") {
    try {
      return JSON.parse(config.data);
    } catch {
      return {};
    }
  }

  return config.data;
};

const demoResponse = (config, data, status = 200) => {
  return Promise.resolve({
    data,
    status,
    statusText: status >= 400 ? "Error" : "OK",
    headers: {},
    config,
    request: null,
  });
};

const demoError = (config, message, status = 404) => {
  return Promise.reject({
    response: {
      data: {
        message,
      },
      status,
      statusText: "Error",
      headers: {},
      config,
    },
    config,
    request: null,
    message,
  });
};

const normalizePath = (url = "") => {
  return url.split("?")[0].replace(/^\/api/, "") || "/";
};

const findAirport = (iataCode) => {
  return airports.find(
    (airport) =>
      airport.iataCode === String(iataCode || "").trim().toUpperCase()
  );
};

const getRouteDistance = (sourceIataCode, destinationIataCode) => {
  const forwardKey = `${sourceIataCode}-${destinationIataCode}`;
  const reverseKey = `${destinationIataCode}-${sourceIataCode}`;

  if (routeDistances[forwardKey] || routeDistances[reverseKey]) {
    return routeDistances[forwardKey] || routeDistances[reverseKey];
  }

  const source = findAirport(sourceIataCode);
  const destination = findAirport(destinationIataCode);
  const hasCoordinates = (airport) =>
    Number.isFinite(airport?.latitude) && Number.isFinite(airport?.longitude);

  if (!hasCoordinates(source) || !hasCoordinates(destination)) {
    return 900;
  }

  const toRadians = (degrees) => (degrees * Math.PI) / 180;
  const earthRadiusKm = 6371;
  const latitudeDistance = toRadians(destination.latitude - source.latitude);
  const longitudeDistance = toRadians(destination.longitude - source.longitude);
  const sourceLatitude = toRadians(source.latitude);
  const destinationLatitude = toRadians(destination.latitude);
  const haversine =
    Math.sin(latitudeDistance / 2) ** 2 +
    Math.cos(sourceLatitude) *
      Math.cos(destinationLatitude) *
      Math.sin(longitudeDistance / 2) ** 2;

  return Math.round(
    earthRadiusKm *
      2 *
      Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine))
  );
};

const getAirportSearchText = (airport) =>
  [
    airport.iataCode,
    airport.icaoCode,
    airport.airportName,
    airport.city,
    airport.country,
    airport.countryCode,
    ...(airport.aliases || []),
  ]
    .join(" ")
    .toLowerCase();

const scoreAirportSearch = (airport, query) => {
  const normalizedQuery = query.toLowerCase();
  const iataCode = airport.iataCode.toLowerCase();
  const city = airport.city.toLowerCase();
  const aliases = (airport.aliases || []).map((alias) => alias.toLowerCase());
  const searchText = getAirportSearchText(airport);

  if (iataCode === normalizedQuery) {
    return 0;
  }

  if (aliases.includes(normalizedQuery)) {
    return 1;
  }

  if (city === normalizedQuery) {
    return 2;
  }

  if (city.startsWith(normalizedQuery) || iataCode.startsWith(normalizedQuery)) {
    return 3;
  }

  if (aliases.some((alias) => alias.startsWith(normalizedQuery))) {
    return 4;
  }

  if (searchText.includes(normalizedQuery)) {
    return 5;
  }

  return Number.POSITIVE_INFINITY;
};

const toIsoLocalDateTime = (date, time) => {
  return `${date}T${time}:00`;
};

const addMinutesToIso = (isoDateTime, minutes) => {
  const date = new Date(isoDateTime);
  date.setMinutes(date.getMinutes() + minutes);
  return date.toISOString().slice(0, 19);
};

const buildFare = (flightScheduleId, flightNumber, route, fareClass, index) => {
  const baseFare = Math.round(
    (3200 + route.distanceKm * 3.1 + index * 700) *
      fareMultipliers[fareClass]
  );

  return {
    id: flightScheduleId * 10 + index + 1,
    flightScheduleId,
    flightNumber,
    sourceIataCode: route.source.iataCode,
    destinationIataCode: route.destination.iataCode,
    fareClass,
    baseFare,
    availableSeats: 18 - index * 2,
    active: true,
    createdAt: `${route.travelDate}T05:00:00`,
    updatedAt: `${route.travelDate}T05:00:00`,
  };
};

const buildDemoFlights = (sourceIataCode, destinationIataCode, travelDate) => {
  const source = findAirport(sourceIataCode) || airports[0];
  const destination = findAirport(destinationIataCode) || airports[1];
  const selectedDate =
    travelDate || new Date().toISOString().slice(0, 10);
  const distanceKm = getRouteDistance(
    source.iataCode,
    destination.iataCode
  );
  const estimatedDurationMinutes = Math.round(distanceKm / 12.5 + 45);
  const departures = ["07:35", "13:20", "20:05"];

  return departures.map((departureTime, index) => {
    const flightScheduleId =
      Number(`${source.id}${destination.id}${index + 1}`);
    const flightNumber = `SL ${210 + source.id * 10 + destination.id + index}`;
    const selectedAircraft = aircraft[index % aircraft.length];
    const departureDateTime = toIsoLocalDateTime(
      selectedDate,
      departureTime
    );
    const route = {
      source,
      destination,
      distanceKm,
      travelDate: selectedDate,
    };

    return {
      flightScheduleId,
      flightNumber,
      flightRouteId: source.id * 100 + destination.id,
      sourceIataCode: source.iataCode,
      sourceAirportName: source.airportName,
      sourceCity: source.city,
      sourceCountryCode: source.countryCode,
      destinationIataCode: destination.iataCode,
      destinationAirportName: destination.airportName,
      destinationCity: destination.city,
      destinationCountryCode: destination.countryCode,
      distanceKm,
      estimatedDurationMinutes,
      ...selectedAircraft,
      departureTime: departureDateTime,
      arrivalTime: addMinutesToIso(
        departureDateTime,
        estimatedDurationMinutes
      ),
      status: "SCHEDULED",
      fares: ["ECONOMY", "PREMIUM_ECONOMY", "BUSINESS", "FIRST"].map(
        (fareClass, fareIndex) =>
          buildFare(
            flightScheduleId,
            flightNumber,
            route,
            fareClass,
            fareIndex
          )
      ),
    };
  });
};

const getDemoProfile = () => {
  const storedProfile = localStorage.getItem(DEMO_PROFILE_KEY);

  if (storedProfile) {
    try {
      return JSON.parse(storedProfile);
    } catch {
      localStorage.removeItem(DEMO_PROFILE_KEY);
    }
  }

  return {
    id: 1,
    fullName: "SkyLink Demo Passenger",
    firstName: "SkyLink",
    lastName: "Passenger",
    email: "demo@skylink.local",
    mobileNumber: "+91 9876543210",
    whatsappNumber: "+91 9876543210",
    role: "USER",
    emailVerified: true,
    mobileVerified: true,
    whatsappVerified: false,
  };
};

const saveDemoProfile = (profile) => {
  localStorage.setItem(DEMO_PROFILE_KEY, JSON.stringify(profile));
  return profile;
};

const getDemoBookings = () => {
  const storedBookings = localStorage.getItem(DEMO_BOOKINGS_KEY);

  if (!storedBookings) {
    return [];
  }

  try {
    return JSON.parse(storedBookings);
  } catch {
    localStorage.removeItem(DEMO_BOOKINGS_KEY);
    return [];
  }
};

const saveDemoBookings = (bookings) => {
  localStorage.setItem(DEMO_BOOKINGS_KEY, JSON.stringify(bookings));
  return bookings;
};

const buildSeats = (flightScheduleId, fareClass) => {
  const rowsByClass = {
    FIRST: [1, 2],
    BUSINESS: [3, 4, 5],
    PREMIUM_ECONOMY: [6, 7, 8, 9],
    ECONOMY: [10, 11, 12, 13, 14, 15],
  };
  const rows = rowsByClass[fareClass] || rowsByClass.ECONOMY;
  const bookedSeats = new Set(
    getDemoBookings()
      .filter(
        (booking) =>
          booking.flightScheduleId === Number(flightScheduleId) &&
          booking.status !== "CANCELLED"
      )
      .flatMap((booking) =>
        (booking.passengers || []).map((passenger) => passenger.seatNumber)
      )
  );

  return rows.flatMap((rowNumber) =>
    ["A", "B", "C", "D", "E", "F"].map((seatLetter, index) => {
      const seatNumber = `${rowNumber}${seatLetter}`;
      const naturallyBooked =
        rowNumber % 3 === 0 && ["B", "E"].includes(seatLetter);
      const booked = bookedSeats.has(seatNumber) || naturallyBooked;

      return {
        id: Number(`${flightScheduleId}${rowNumber}${index + 1}`),
        seatNumber,
        fareClass,
        rowNumber,
        seatLetter,
        windowSeat: ["A", "F"].includes(seatLetter),
        aisleSeat: ["C", "D"].includes(seatLetter),
        extraLegroom: rowNumber === rows[0],
        booked,
        available: !booked,
      };
    })
  );
};

const createBooking = (request) => {
  let selectedBooking;

  try {
    selectedBooking = JSON.parse(
      localStorage.getItem(SELECTED_BOOKING_KEY) || "null"
    );
  } catch {
    selectedBooking = null;
  }

  const passengers = (request.passengers || []).map((passenger, index) => ({
    id: Date.now() + index,
    firstName: passenger.firstName,
    lastName: passenger.lastName,
    dateOfBirth: passenger.dateOfBirth,
    gender: passenger.gender,
    passportNumber: passenger.passportNumber,
    nationality: passenger.nationality,
    seatNumber: passenger.seatNumber,
    active: true,
  }));
  const passengerCount = Math.max(passengers.length, 1);
  const now = new Date().toISOString().slice(0, 19);
  const booking = {
    id: Date.now(),
    bookingReference: `SKY${Math.random()
      .toString(36)
      .slice(2, 10)
      .toUpperCase()}`,
    userId: 1,
    userEmail: getDemoProfile().email,
    flightScheduleId:
      selectedBooking?.flightScheduleId || Number(request.flightScheduleId),
    flightNumber: selectedBooking?.flightNumber || "SL 221",
    sourceIataCode: selectedBooking?.sourceIataCode || "DEL",
    sourceAirportName:
      selectedBooking?.sourceAirportName ||
      "Indira Gandhi International Airport",
    destinationIataCode: selectedBooking?.destinationIataCode || "BOM",
    destinationAirportName:
      selectedBooking?.destinationAirportName ||
      "Chhatrapati Shivaji Maharaj International Airport",
    departureTime:
      selectedBooking?.departureTime ||
      new Date().toISOString().slice(0, 19),
    arrivalTime:
      selectedBooking?.arrivalTime ||
      addMinutesToIso(new Date().toISOString().slice(0, 19), 135),
    fareClass: request.fareClass || selectedBooking?.fareClass || "ECONOMY",
    baseFare: selectedBooking?.baseFare || 6400,
    passengerCount,
    totalAmount: (selectedBooking?.baseFare || 6400) * passengerCount,
    status: "CONFIRMED",
    active: true,
    passengers,
    createdAt: now,
    updatedAt: now,
  };
  const bookings = [booking, ...getDemoBookings()];

  saveDemoBookings(bookings);
  return booking;
};

const loginResponse = (email) => {
  const profile = saveDemoProfile({
    ...getDemoProfile(),
    email: email || "demo@skylink.local",
    fullName:
      email && email.includes("admin")
        ? "SkyLink Demo Admin"
        : "SkyLink Demo Passenger",
    role: email && email.includes("admin") ? "ADMIN" : "USER",
  });

  return {
    token: "demo-skylink-jwt-token",
    userId: profile.id,
    email: profile.email,
    fullName: profile.fullName,
    role: profile.role,
  };
};

export const demoAdapter = async (config) => {
  const method = String(config.method || "get").toLowerCase();
  const path = normalizePath(config.url);
  const body = getJsonBody(config);
  const params = config.params || {};

  await new Promise((resolve) => window.setTimeout(resolve, 250));

  if (method === "get" && path === "/airports/search") {
    const query = String(params.query || "").trim().toLowerCase();
    const data = airports
      .map((airport) => ({
        airport,
        score: scoreAirportSearch(airport, query),
      }))
      .filter(({ score }) => Number.isFinite(score))
      .sort((first, second) => {
        if (first.score !== second.score) {
          return first.score - second.score;
        }

        return first.airport.city.localeCompare(second.airport.city);
      })
      .map(({ airport }) => airport);

    return demoResponse(config, data);
  }

  if (method === "post" && path === "/flights/search") {
    return demoResponse(
      config,
      buildDemoFlights(
        body.sourceIataCode,
        body.destinationIataCode,
        body.travelDate
      )
    );
  }

  const seatMatch = path.match(/^\/seats\/flight\/(\d+)\/class\/([^/]+)$/);

  if (method === "get" && seatMatch) {
    return demoResponse(config, buildSeats(seatMatch[1], seatMatch[2]));
  }

  if (method === "post" && path === "/auth/login") {
    return demoResponse(config, loginResponse(body.email));
  }

  if (method === "post" && path === "/auth/login/otp/send") {
    return demoResponse(config, {
      message: "Demo OTP sent. Use any OTP value to continue.",
    });
  }

  if (method === "post" && path === "/auth/login/otp/verify") {
    return demoResponse(config, loginResponse(body.identifier));
  }

  if (method === "post" && path === "/otp/send") {
    return demoResponse(config, {
      message: "Demo OTP sent. Use any OTP value to continue.",
    });
  }

  if (method === "post" && path === "/otp/verify") {
    return demoResponse(config, {
      verified: true,
      message: "Demo OTP verified.",
    });
  }

  if (method === "post" && path === "/users/register") {
    saveDemoProfile({
      ...getDemoProfile(),
      fullName: body.fullName,
      email: body.email,
      mobileNumber: body.mobileNumber,
      whatsappNumber: body.whatsappNumber,
      role: "USER",
      emailVerified: true,
    });

    return demoResponse(config, {
      message: "Demo account created.",
    }, 201);
  }

  if (method === "get" && path === "/users/me") {
    return demoResponse(config, getDemoProfile());
  }

  if (method === "post" && path === "/bookings") {
    return demoResponse(config, createBooking(body), 201);
  }

  if (method === "get" && path === "/bookings/my") {
    return demoResponse(config, getDemoBookings());
  }

  const cancelMatch = path.match(/^\/bookings\/([^/]+)\/cancel$/);

  if (method === "post" && cancelMatch) {
    const bookingReference = decodeURIComponent(cancelMatch[1]);
    const bookings = getDemoBookings();
    const bookingIndex = bookings.findIndex(
      (booking) => booking.bookingReference === bookingReference
    );

    if (bookingIndex === -1) {
      return demoError(config, "Booking not found.", 404);
    }

    bookings[bookingIndex] = {
      ...bookings[bookingIndex],
      status: "CANCELLED",
      active: false,
      updatedAt: new Date().toISOString().slice(0, 19),
    };

    saveDemoBookings(bookings);
    return demoResponse(config, bookings[bookingIndex]);
  }

  return demoError(config, "Demo endpoint is not available.", 404);
};
