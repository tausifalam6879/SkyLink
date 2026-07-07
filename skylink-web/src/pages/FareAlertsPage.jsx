import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  Bell,
  CalendarDays,
  Mail,
  MapPin,
  Plane,
  Search,
  Trash2,
} from "lucide-react";

const FARE_ALERTS_KEY = "skylink_fare_alerts";

const months = [
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
];

const getStoredAlerts = () => {
  try {
    return JSON.parse(localStorage.getItem(FARE_ALERTS_KEY) || "[]");
  } catch {
    return [];
  }
};

const formatPrice = (price) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(price);

function FareAlertsPage() {
  const navigate = useNavigate();

  const [alerts, setAlerts] = useState(getStoredAlerts);
  const [formData, setFormData] = useState({
    source: "Delhi",
    destination: "Mumbai",
    month: "July",
    targetFare: "6500",
    email: "",
  });

  const bestAlert = useMemo(() => alerts[0], [alerts]);

  const updateField = (field, value) => {
    setFormData((currentData) => ({
      ...currentData,
      [field]: value,
    }));
  };

  const persistAlerts = (nextAlerts) => {
    setAlerts(nextAlerts);
    localStorage.setItem(FARE_ALERTS_KEY, JSON.stringify(nextAlerts));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const targetFare = Number(formData.targetFare);
    const simulatedCurrentFare = targetFare + 1200 + alerts.length * 350;
    const nextAlert = {
      id: Date.now(),
      ...formData,
      targetFare,
      currentFare: simulatedCurrentFare,
      status:
        simulatedCurrentFare <= targetFare
          ? "Target reached"
          : "Watching fare",
      createdAt: new Date().toISOString(),
    };

    persistAlerts([nextAlert, ...alerts].slice(0, 8));
  };

  const deleteAlert = (alertId) => {
    persistAlerts(alerts.filter((alert) => alert.id !== alertId));
  };

  return (
    <div className="my-bookings-page utility-page fare-alert-page">
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
        <section className="utility-hero fare-alert-hero">
          <div>
            <span>FARE ALERTS</span>
            <h2>Set a target price and let SkyLink watch the route.</h2>
            <p>
              Create route alerts, track the current fare and jump back to
              search when a deal looks right.
            </p>
          </div>

          <Bell size={58} />
        </section>

        <section className="fare-alert-shell">
          <form className="fare-alert-card" onSubmit={handleSubmit}>
            <h3>Create alert</h3>

            <div className="utility-input-grid">
              <label>
                <MapPin size={18} />
                <span>From</span>
                <input
                  value={formData.source}
                  onChange={(event) => updateField("source", event.target.value)}
                  required
                />
              </label>

              <label>
                <MapPin size={18} />
                <span>To</span>
                <input
                  value={formData.destination}
                  onChange={(event) =>
                    updateField("destination", event.target.value)
                  }
                  required
                />
              </label>
            </div>

            <div className="utility-input-grid">
              <label>
                <CalendarDays size={18} />
                <span>Travel month</span>
                <select
                  value={formData.month}
                  onChange={(event) => updateField("month", event.target.value)}
                >
                  {months.map((month) => (
                    <option key={month}>{month}</option>
                  ))}
                </select>
              </label>

              <label>
                <Bell size={18} />
                <span>Target fare</span>
                <input
                  type="number"
                  min="1000"
                  step="100"
                  value={formData.targetFare}
                  onChange={(event) =>
                    updateField("targetFare", event.target.value)
                  }
                  required
                />
              </label>
            </div>

            <div className="utility-input-grid single">
              <label>
                <Mail size={18} />
                <span>Email for alert</span>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(event) => updateField("email", event.target.value)}
                  placeholder="Optional"
                />
              </label>
            </div>

            <button type="submit" className="utility-primary-button">
              <Bell size={18} />
              Create fare alert
            </button>
          </form>

          <aside className="fare-alert-card">
            <div className="fare-alert-list-head">
              <h3>Watching now</h3>
              <span>{alerts.length} active</span>
            </div>

            {bestAlert ? (
              <div className="fare-alert-highlight">
                <span>Best watched route</span>
                <strong>
                  {bestAlert.source} to {bestAlert.destination}
                </strong>
                <p>
                  Current {formatPrice(bestAlert.currentFare)} against target{" "}
                  {formatPrice(bestAlert.targetFare)}
                </p>
              </div>
            ) : (
              <div className="fare-alert-empty">
                <Bell size={28} />
                <strong>No alerts yet</strong>
                <span>Create one from the form to start watching fares.</span>
              </div>
            )}

            <div className="fare-alert-list">
              {alerts.map((alert) => (
                <article key={alert.id} className="fare-alert-item">
                  <div>
                    <span>{alert.month}</span>
                    <strong>
                      {alert.source} to {alert.destination}
                    </strong>
                    <small>
                      {alert.status} at {formatPrice(alert.targetFare)}
                    </small>
                  </div>

                  <div className="fare-alert-actions">
                    <button type="button" onClick={() => navigate("/")}>
                      <Search size={16} />
                    </button>
                    <button type="button" onClick={() => deleteAlert(alert.id)}>
                      <Trash2 size={16} />
                    </button>
                  </div>
                </article>
              ))}
            </div>
          </aside>
        </section>
      </main>
    </div>
  );
}

export default FareAlertsPage;
