import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  CalendarDays,
  ChevronDown,
  Clock3,
  MapPin,
  Plane,
  Radar,
  Search,
} from "lucide-react";

const airlines = [
  { code: "SL", name: "SkyLink Air" },
  { code: "AI", name: "Air India" },
  { code: "6E", name: "IndiGo" },
  { code: "IX", name: "Air India Express" },
  { code: "QP", name: "Akasa Air" },
  { code: "SG", name: "SpiceJet" },
  { code: "9I", name: "Alliance Air" },
];

const statusSamples = [
  {
    flightNumber: "SL 204",
    airline: "SkyLink Air",
    route: "DEL to BOM",
    departure: "10:35 AM",
    arrival: "12:50 PM",
    gate: "A12",
    belt: "B4",
    status: "On Time",
    progress: 62,
  },
  {
    flightNumber: "6E 918",
    airline: "IndiGo",
    route: "BLR to DXB",
    departure: "02:10 PM",
    arrival: "05:35 PM",
    gate: "C7",
    belt: "TBA",
    status: "Boarding",
    progress: 28,
  },
  {
    flightNumber: "AI 512",
    airline: "Air India",
    route: "CCU to DEL",
    departure: "06:45 PM",
    arrival: "09:10 PM",
    gate: "D3",
    belt: "B2",
    status: "Scheduled",
    progress: 8,
  },
];

function FlightStatusPage() {
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState("flight");
  const [flightNumber, setFlightNumber] = useState("");
  const [source, setSource] = useState("");
  const [destination, setDestination] = useState("");
  const [airlineQuery, setAirlineQuery] = useState("");
  const [selectedAirline, setSelectedAirline] = useState(null);
  const [airlineOpen, setAirlineOpen] = useState(false);
  const [travelDate, setTravelDate] = useState(
    new Date().toISOString().split("T")[0]
  );
  const [searched, setSearched] = useState(false);

  const filteredAirlines = useMemo(() => {
    const normalized = airlineQuery.trim().toLowerCase();

    if (!normalized) {
      return airlines;
    }

    return airlines.filter(
      (airline) =>
        airline.name.toLowerCase().includes(normalized) ||
        airline.code.toLowerCase().includes(normalized)
    );
  }, [airlineQuery]);

  const matchedStatus = useMemo(() => {
    if (!searched) {
      return statusSamples[0];
    }

    const normalizedFlight = flightNumber.trim().toUpperCase();

    if (normalizedFlight) {
      return (
        statusSamples.find((item) =>
          item.flightNumber
            .replace(/\s/g, "")
            .includes(normalizedFlight.replace(/\s/g, ""))
        ) || {
          ...statusSamples[0],
          flightNumber: normalizedFlight,
          airline: selectedAirline?.name || "SkyLink Air",
          status: "Scheduled",
          progress: 12,
        }
      );
    }

    if (activeTab === "scheduled" && selectedAirline) {
      return {
        ...statusSamples[2],
        airline: selectedAirline.name,
        flightNumber: `${selectedAirline.code} 402`,
        status: "Scheduled",
        progress: 10,
      };
    }

    const routeQuery = `${source.trim()} to ${destination.trim()}`.toUpperCase();

    return (
      statusSamples.find((item) => item.route.toUpperCase().includes(routeQuery)) ||
      {
        ...statusSamples[1],
        route: `${source || "DEL"} to ${destination || "BOM"}`,
        airline: selectedAirline?.name || "SkyLink Air",
      }
    );
  }, [
    activeTab,
    destination,
    flightNumber,
    searched,
    selectedAirline,
    source,
  ]);

  const selectAirline = (airline) => {
    setSelectedAirline(airline);
    setAirlineQuery(`${airline.code} - ${airline.name}`);
    setAirlineOpen(false);
  };

  const handleSearch = (event) => {
    event.preventDefault();
    setSearched(true);
  };

  const focusAirline = (airline) => {
    setActiveTab("scheduled");
    selectAirline(airline);
    setSearched(true);
  };

  return (
    <div className="my-bookings-page utility-page flight-status-page">
      <nav className="booking-navbar">
        <div className="brand booking-brand" onClick={() => navigate("/")}>
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

      <main className="utility-content">
        <section className="utility-hero">
          <div>
            <span>LIVE FLIGHT TRACKER</span>
            <h2>Track flights by number, route or airline schedule.</h2>
            <p>
              Preview status, gate, baggage belt and journey progress in one
              SkyLink tracker.
            </p>
          </div>

          <Radar size={58} />
        </section>

        <section className="status-layout flight-status-layout">
          <div>
            <form className="status-search-card" onSubmit={handleSearch}>
              <div className="status-tabs">
                {[
                  ["flight", "Flight No."],
                  ["route", "Route"],
                  ["scheduled", "Scheduled"],
                ].map(([tab, label]) => (
                  <button
                    type="button"
                    className={activeTab === tab ? "active" : ""}
                    key={tab}
                    onClick={() => setActiveTab(tab)}
                  >
                    {label}
                  </button>
                ))}
              </div>

              {activeTab === "flight" && (
                <div className="utility-input-grid single">
                  <label>
                    <Plane size={18} />
                    <span>Flight number</span>
                    <input
                      value={flightNumber}
                      onChange={(event) => setFlightNumber(event.target.value)}
                      placeholder="SL 204"
                      required
                    />
                  </label>
                </div>
              )}

              {activeTab === "route" && (
                <div className="utility-input-grid">
                  <label>
                    <MapPin size={18} />
                    <span>From</span>
                    <input
                      value={source}
                      onChange={(event) => setSource(event.target.value)}
                      placeholder="DEL"
                      required
                    />
                  </label>

                  <label>
                    <MapPin size={18} />
                    <span>To</span>
                    <input
                      value={destination}
                      onChange={(event) => setDestination(event.target.value)}
                      placeholder="BOM"
                      required
                    />
                  </label>
                </div>
              )}

              {activeTab === "scheduled" && (
                <div className="airline-selector">
                  <label>
                    <Plane size={18} />
                    <span>Airline</span>
                    <div>
                      <input
                        value={airlineQuery}
                        onFocus={() => setAirlineOpen(true)}
                        onChange={(event) => {
                          setAirlineQuery(event.target.value);
                          setSelectedAirline(null);
                          setAirlineOpen(true);
                        }}
                        placeholder="Search airline"
                        required
                      />
                      <ChevronDown size={17} />
                    </div>
                  </label>

                  {airlineOpen && (
                    <div className="airline-options">
                      {filteredAirlines.map((airline) => (
                        <button
                          type="button"
                          key={airline.code}
                          onClick={() => selectAirline(airline)}
                        >
                          <span>{airline.code}</span>
                          {airline.name}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              )}

              <label className="utility-date-field">
                <CalendarDays size={18} />
                <span>Date</span>
                <input
                  type="date"
                  value={travelDate}
                  onChange={(event) => setTravelDate(event.target.value)}
                  required
                />
              </label>

              <button type="submit" className="utility-primary-button">
                <Search size={18} />
                Check status
              </button>
            </form>

            <article className="status-result-card">
              <div className="status-chip">{matchedStatus.status}</div>

              <div className="status-flight-head">
                <div>
                  <span>{matchedStatus.airline}</span>
                  <h3>{matchedStatus.flightNumber}</h3>
                </div>

                <strong>{matchedStatus.route}</strong>
              </div>

              <div className="status-progress">
                <div style={{ width: `${matchedStatus.progress}%` }} />
              </div>

              <div className="status-info-grid">
                <div>
                  <Clock3 size={18} />
                  <span>Departure</span>
                  <strong>{matchedStatus.departure}</strong>
                </div>

                <div>
                  <Clock3 size={18} />
                  <span>Arrival</span>
                  <strong>{matchedStatus.arrival}</strong>
                </div>

                <div>
                  <MapPin size={18} />
                  <span>Gate</span>
                  <strong>{matchedStatus.gate}</strong>
                </div>

                <div>
                  <MapPin size={18} />
                  <span>Belt</span>
                  <strong>{matchedStatus.belt}</strong>
                </div>
              </div>
            </article>
          </div>

          <aside className="airline-sidebar">
            <h3>Domestic airlines</h3>
            {airlines.slice(1).map((airline) => (
              <button
                type="button"
                key={airline.code}
                onClick={() => focusAirline(airline)}
              >
                <span>{airline.code}</span>
                {airline.name}
              </button>
            ))}
          </aside>
        </section>

        <section className="faq-panel">
          <h3>Flight status guide</h3>

          <div>
            <strong>Scheduled</strong>
            <span>Flight is planned and not airborne yet.</span>
          </div>

          <div>
            <strong>Boarding</strong>
            <span>Passengers are being accepted at the gate.</span>
          </div>

          <div>
            <strong>On Time</strong>
            <span>Expected departure and arrival are currently stable.</span>
          </div>
        </section>
      </main>
    </div>
  );
}

export default FlightStatusPage;
