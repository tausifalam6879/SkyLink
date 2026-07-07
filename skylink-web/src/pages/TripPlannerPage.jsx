import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  Building2,
  CloudRain,
  CloudSun,
  Landmark,
  Map,
  Mountain,
  Plane,
  Search,
  ShoppingBag,
  Snowflake,
  Sparkles,
  Star,
  Sun,
  ThermometerSun,
  Umbrella,
  Wand2,
} from "lucide-react";

const categories = [
  "All",
  "Religious",
  "Cultural",
  "Nature",
  "Food",
  "Festivals",
  "Historical",
  "Shopping",
  "Beaches",
  "Mountains",
  "Outdoors",
  "Nightlife",
  "Luxury",
];

const popularFilters = ["Domestic", "International", "Visa-free"];
const weatherFilters = [
  "Rain",
  "No rain",
  "Snow",
  "Low AQI",
  "Colder",
  "Warmer",
];

const destinations = [
  ["Jaipur", "Rajasthan, India", "Historical", "Domestic", 2850, 3200, 2, ["No rain", "Warmer"], "visual-jaipur"],
  ["Denpasar", "Bali, Indonesia", "Beaches", "International", 24000, 4900, 8, ["Rain", "Warmer", "Visa-free"], "visual-bali"],
  ["Lonavala", "Maharashtra, India", "Nature", "Domestic", 13200, 3667, 2, ["Rain", "Low AQI"], "visual-lonavala"],
  ["Ho Chi Minh", "Vietnam", "Food", "International", 19000, 10000, 7, ["Warmer", "Visa-free"], "visual-hochi"],
  ["Bandel", "West Bengal, India", "Religious", "Domestic", 7000, 2000, 1, ["No rain"], "visual-kolkata"],
  ["Lavasa", "Maharashtra, India", "Outdoors", "Domestic", 15000, 3333, 3, ["Low AQI", "Rain"], "visual-lonavala"],
  ["Almaty", "Kazakhstan", "Mountains", "International", 20500, 5000, 7, ["Colder", "Low AQI"], "visual-almaty"],
  ["Khandala", "Maharashtra, India", "Nature", "Domestic", 15000, 3100, 2, ["Rain", "Low AQI"], "visual-lonavala"],
  ["Kuala Lumpur", "Malaysia", "Shopping", "International", 15000, 10000, 6, ["Warmer", "Visa-free"], "visual-dubai"],
  ["Kolad", "Maharashtra, India", "Outdoors", "Domestic", 16000, 3000, 3, ["Rain"], "visual-lonavala"],
  ["Satara", "Maharashtra, India", "Historical", "Domestic", 13000, 2000, 3, ["No rain"], "visual-jaipur"],
  ["Fagu", "Himachal Pradesh, India", "Mountains", "Domestic", 10000, 3000, 4, ["Snow", "Colder"], "visual-almaty"],
  ["Sumatra", "Indonesia", "Nature", "International", 16000, 3600, 8, ["Rain", "Warmer"], "visual-bali"],
  ["Bangkok", "Thailand", "Nightlife", "International", 11000, 5200, 6, ["Warmer", "Visa-free"], "visual-dubai"],
  ["Mahe", "Seychelles", "Luxury", "International", 13000, 4000, 9, ["Warmer", "Visa-free"], "visual-bali"],
  ["Chiang Rai", "Thailand", "Cultural", "International", 16000, 3600, 6, ["Low AQI", "Visa-free"], "visual-hochi"],
  ["Putrajaya", "Malaysia", "Cultural", "International", 15000, 4000, 6, ["Low AQI", "Visa-free"], "visual-dubai"],
  ["Sabah", "Malaysia", "Outdoors", "International", 19000, 5000, 7, ["Rain", "Visa-free"], "visual-bali"],
  ["Johor Bahru", "Malaysia", "Shopping", "International", 21000, 10000, 6, ["Warmer", "Visa-free"], "visual-dubai"],
  ["Bandipur", "Nepal", "Mountains", "International", 7000, 3000, 3, ["Colder", "Low AQI"], "visual-almaty"],
  ["Melaka", "Malaysia", "Festivals", "International", 18000, 5300, 6, ["Warmer", "Visa-free"], "visual-hochi"],
  ["Kolkata", "West Bengal, India", "Cultural", "Domestic", 5200, 2600, 1, ["Warmer"], "visual-kolkata"],
].map(
  ([
    name,
    region,
    category,
    type,
    flightPrice,
    stayPrice,
    travelTime,
    tags,
    gradient,
  ]) => ({
    name,
    region,
    category,
    type,
    flightPrice,
    stayPrice,
    travelTime,
    tags,
    gradient,
  })
);

const getCategoryIcon = (category) => {
  switch (category) {
    case "Religious":
      return Star;
    case "Cultural":
      return Sparkles;
    case "Nature":
      return CloudSun;
    case "Food":
      return Building2;
    case "Festivals":
      return Wand2;
    case "Historical":
      return Landmark;
    case "Shopping":
      return ShoppingBag;
    case "Beaches":
      return Umbrella;
    case "Mountains":
      return Mountain;
    case "Outdoors":
      return Sun;
    case "Nightlife":
      return ThermometerSun;
    case "Luxury":
      return Sparkles;
    default:
      return Map;
  }
};

const formatPrice = (price) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(price);

function TripPlannerPage() {
  const navigate = useNavigate();

  const [origin, setOrigin] = useState("Jamshedpur");
  const [month, setMonth] = useState("July");
  const [activeCategory, setActiveCategory] = useState("All");
  const [budget, setBudget] = useState(25000);
  const [travelTime, setTravelTime] = useState(12);
  const [query, setQuery] = useState("");
  const [selectedTypes, setSelectedTypes] = useState([]);
  const [selectedWeather, setSelectedWeather] = useState([]);
  const [selectedDestination, setSelectedDestination] = useState(null);
  const [aiShortlistOpen, setAiShortlistOpen] = useState(false);
  const [feedbackOpen, setFeedbackOpen] = useState(false);
  const [feedbackSent, setFeedbackSent] = useState(false);

  const filteredDestinations = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    return destinations.filter((destination) => {
      const matchesCategory =
        activeCategory === "All" || destination.category === activeCategory;
      const matchesBudget = destination.flightPrice <= budget;
      const matchesTime = destination.travelTime <= travelTime;
      const matchesType =
        selectedTypes.length === 0 ||
        selectedTypes.includes(destination.type) ||
        (selectedTypes.includes("Visa-free") &&
          destination.tags.includes("Visa-free"));
      const matchesWeather =
        selectedWeather.length === 0 ||
        selectedWeather.every((tag) => destination.tags.includes(tag));
      const matchesQuery =
        !normalizedQuery ||
        destination.name.toLowerCase().includes(normalizedQuery) ||
        destination.region.toLowerCase().includes(normalizedQuery);

      return (
        matchesCategory &&
        matchesBudget &&
        matchesTime &&
        matchesType &&
        matchesWeather &&
        matchesQuery
      );
    });
  }, [
    activeCategory,
    budget,
    query,
    selectedTypes,
    selectedWeather,
    travelTime,
  ]);

  const aiShortlist = filteredDestinations.slice(0, 3);

  const toggleFilter = (value, setter) => {
    setter((current) =>
      current.includes(value)
        ? current.filter((item) => item !== value)
        : [...current, value]
    );
  };

  const clearFilters = () => {
    setActiveCategory("All");
    setBudget(25000);
    setTravelTime(12);
    setQuery("");
    setSelectedTypes([]);
    setSelectedWeather([]);
    setSelectedDestination(null);
    setAiShortlistOpen(false);
  };

  const chooseDestination = (destination) => {
    setSelectedDestination(destination);
  };

  const bookDestination = (destination) => {
    sessionStorage.setItem(
      "skylink_plan_destination",
      JSON.stringify(destination)
    );
    navigate("/");
  };

  return (
    <div className="my-bookings-page utility-page planner-page">
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

      <main className="planner-shell rich-planner-shell">
        <aside className="planner-filters">
          <div className="planner-filter-head">
            <h2>Filters</h2>
            <button type="button" onClick={clearFilters}>
              Clear
            </button>
          </div>

          <label>
            From
            <input
              value={origin}
              onChange={(event) => setOrigin(event.target.value)}
            />
          </label>

          <label>
            Travel month
            <select
              value={month}
              onChange={(event) => setMonth(event.target.value)}
            >
              <option>July</option>
              <option>August</option>
              <option>September</option>
              <option>October</option>
            </select>
          </label>

          <div className="planner-chip-group">
            <span>Popular</span>
            {popularFilters.map((filter) => (
              <button
                type="button"
                className={selectedTypes.includes(filter) ? "active" : ""}
                key={filter}
                onClick={() => toggleFilter(filter, setSelectedTypes)}
              >
                {filter}
              </button>
            ))}
          </div>

          <div className="budget-control">
            <span>Budget</span>
            <input
              type="range"
              min="5000"
              max="50000"
              step="1000"
              value={budget}
              onChange={(event) => setBudget(Number(event.target.value))}
            />
            <strong>{formatPrice(budget)}</strong>
          </div>

          <div className="budget-control">
            <span>Travel time</span>
            <input
              type="range"
              min="1"
              max="12"
              value={travelTime}
              onChange={(event) => setTravelTime(Number(event.target.value))}
            />
            <strong>{travelTime}hr max</strong>
          </div>

          <div className="planner-chip-group">
            <span>Weather and AQI</span>
            {weatherFilters.map((filter) => {
              const Icon =
                filter === "Rain"
                  ? CloudRain
                  : filter === "Snow"
                    ? Snowflake
                    : filter === "Warmer"
                      ? ThermometerSun
                      : CloudSun;

              return (
                <button
                  type="button"
                  className={
                    selectedWeather.includes(filter) ? "active" : ""
                  }
                  key={filter}
                  onClick={() => toggleFilter(filter, setSelectedWeather)}
                >
                  <Icon size={15} />
                  {filter}
                </button>
              );
            })}
          </div>

          <button
            type="button"
            className="planner-feedback-button"
            onClick={() => setFeedbackOpen((current) => !current)}
          >
            Feedback
          </button>

          {feedbackOpen && (
            <div className="planner-feedback-box">
              <textarea placeholder="Tell us what trip ideas you want next." />
              <button
                type="button"
                onClick={() => setFeedbackSent(true)}
              >
                {feedbackSent ? "Sent" : "Send feedback"}
              </button>
            </div>
          )}
        </aside>

        <section className="planner-results">
          <div className="planner-topbar">
            <div className="planner-searchbar">
              <Search size={22} />
              <input
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="Search destination"
              />
            </div>

            <button
              type="button"
              className="planner-ai-button"
              onClick={() => setAiShortlistOpen((current) => !current)}
            >
              <Wand2 size={18} />
              Create trip with AI
            </button>
          </div>

          <div className="category-tabs planner-category-tabs">
            {categories.map((category) => {
              const Icon = getCategoryIcon(category);

              return (
                <button
                  type="button"
                  className={activeCategory === category ? "active" : ""}
                  key={category}
                  onClick={() => setActiveCategory(category)}
                >
                  <Icon size={20} />
                  {category}
                </button>
              );
            })}
          </div>

          <div className="planner-context">
            <div>
              <span>TRIP IDEAS FROM</span>
              <h2>{origin || "Your city"}</h2>
            </div>

            <p>
              {filteredDestinations.length} {month} picks under{" "}
              {formatPrice(budget)}
            </p>
          </div>

          {aiShortlistOpen && (
            <div className="ai-shortlist-panel">
              <strong>Smart shortlist</strong>
              <div>
                {aiShortlist.map((destination) => (
                  <button
                    type="button"
                    key={destination.name}
                    onClick={() => chooseDestination(destination)}
                  >
                    {destination.name}
                    <span>
                      {formatPrice(destination.flightPrice)} flight
                    </span>
                  </button>
                ))}
              </div>
            </div>
          )}

          <div className="destination-grid expanded-destination-grid">
            {filteredDestinations.map((destination) => (
              <article
                className={`destination-card ${
                  selectedDestination?.name === destination.name
                    ? "selected"
                    : ""
                }`}
                key={destination.name}
                onClick={() => chooseDestination(destination)}
              >
                <div className={`destination-visual ${destination.gradient}`}>
                  <span>{destination.category}</span>
                  <h3>{destination.name}</h3>
                  <p>{destination.region}</p>
                </div>

                <div className="destination-meta">
                  <div>
                    <Plane size={19} />
                    <strong>{formatPrice(destination.flightPrice)}</strong>
                  </div>

                  <div>
                    <Building2 size={19} />
                    <strong>{formatPrice(destination.stayPrice)}/night</strong>
                  </div>
                </div>

                <div className="destination-footer">
                  <span>{destination.tags.slice(0, 2).join(" | ")}</span>
                  <button
                    type="button"
                    onClick={(event) => {
                      event.stopPropagation();
                      bookDestination(destination);
                    }}
                  >
                    Book flight
                  </button>
                </div>
              </article>
            ))}
          </div>

          {filteredDestinations.length === 0 && (
            <div className="planner-empty-state">
              <Map size={38} />
              <h3>No trip ideas match these filters</h3>
              <p>Clear a filter or increase your budget to see more places.</p>
            </div>
          )}

          {selectedDestination && (
            <aside className="destination-detail-panel">
              <button
                type="button"
                onClick={() => setSelectedDestination(null)}
              >
                Close
              </button>
              <span>{selectedDestination.category} plan</span>
              <h3>{selectedDestination.name}</h3>
              <p>{selectedDestination.region}</p>
              <div>
                <strong>{formatPrice(selectedDestination.flightPrice)}</strong>
                <small>estimated flight fare</small>
              </div>
              <div>
                <strong>{selectedDestination.travelTime}hr</strong>
                <small>approx travel time</small>
              </div>
              <button
                type="button"
                className="offer-search-button"
                onClick={() => bookDestination(selectedDestination)}
              >
                Book flight
              </button>
            </aside>
          )}
        </section>
      </main>
    </div>
  );
}

export default TripPlannerPage;
