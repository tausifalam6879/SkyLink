import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  CalendarDays,
  CheckCircle2,
  Copy,
  Mail,
  MapPin,
  Phone,
  Plane,
  RefreshCw,
  Search,
  Send,
  UsersRound,
} from "lucide-react";

const GROUP_REQUESTS_KEY = "skylink_group_booking_requests";

const getToday = () => new Date().toISOString().split("T")[0];

const createGroupReference = () =>
  `SLG-${Date.now().toString().slice(-6)}`;

function GroupBookingPage() {
  const navigate = useNavigate();

  const [submitted, setSubmitted] = useState(false);
  const [requestReference, setRequestReference] = useState("");
  const [copyStatus, setCopyStatus] = useState("");
  const [formData, setFormData] = useState({
    tripType: "ONE_WAY",
    source: "",
    destination: "",
    departureDate: "",
    returnDate: "",
    passengers: "10",
    cabinClass: "Economy",
    contactPreference: "Phone",
    email: "",
    phone: "",
    notes: "",
  });

  const updateField = (field, value) => {
    setFormData((currentData) => ({
      ...currentData,
      [field]: value,
      ...(field === "tripType" && value === "ONE_WAY"
        ? { returnDate: "" }
        : {}),
    }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const reference = createGroupReference();
    const request = {
      ...formData,
      reference,
      createdAt: new Date().toISOString(),
    };

    try {
      const storedRequests = JSON.parse(
        localStorage.getItem(GROUP_REQUESTS_KEY) || "[]"
      );
      localStorage.setItem(
        GROUP_REQUESTS_KEY,
        JSON.stringify([request, ...storedRequests].slice(0, 8))
      );
    } catch {
      localStorage.setItem(GROUP_REQUESTS_KEY, JSON.stringify([request]));
    }

    setRequestReference(reference);
    setCopyStatus("");
    setSubmitted(true);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const resetRequest = () => {
    setSubmitted(false);
    setRequestReference("");
    setCopyStatus("");
  };

  const copyRequestSummary = async () => {
    const summary = [
      `SkyLink group request: ${requestReference}`,
      `${formData.passengers} passengers, ${formData.cabinClass}`,
      `${formData.source} to ${formData.destination}`,
      `Depart: ${formData.departureDate}`,
      formData.returnDate ? `Return: ${formData.returnDate}` : null,
      `Contact: ${formData.contactPreference} | ${formData.phone || formData.email}`,
    ]
      .filter(Boolean)
      .join("\n");

    try {
      await navigator.clipboard.writeText(summary);
      setCopyStatus("Copied");
    } catch {
      setCopyStatus("Copy failed");
    }
  };

  return (
    <div className="my-bookings-page utility-page group-booking-page">
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

      <main className="utility-content group-layout">
        <section className="group-copy">
          <span>GROUP TRAVEL</span>
          <h2>
            Flying with more than 9 passengers? SkyLink can coordinate it.
          </h2>
          <p>
            Share your route, passenger count and contact details. The request
            summary is prepared instantly so your operations team can follow up.
          </p>

          <div className="group-benefits">
            <div>
              <UsersRound size={20} />
              Dedicated group fare handling
            </div>
            <div>
              <Plane size={20} />
              Domestic and international routing
            </div>
            <div>
              <CheckCircle2 size={20} />
              Flexible passenger finalization
            </div>
          </div>
        </section>

        <section className="group-form-card">
          {submitted ? (
            <div className="group-success">
              <CheckCircle2 size={48} />
              <span>REQUEST READY</span>
              <h3>Group booking request captured</h3>
              <p>
                {formData.passengers} passengers from {formData.source} to{" "}
                {formData.destination} on {formData.departureDate}.
              </p>

              <div className="group-meta-grid">
                <div>
                  <span>Reference</span>
                  <strong>{requestReference}</strong>
                </div>
                <div>
                  <span>Class</span>
                  <strong>{formData.cabinClass}</strong>
                </div>
                <div>
                  <span>Contact by</span>
                  <strong>{formData.contactPreference}</strong>
                </div>
                <div>
                  <span>Trip type</span>
                  <strong>
                    {formData.tripType === "ROUND_TRIP"
                      ? "Round trip"
                      : "One way"}
                  </strong>
                </div>
              </div>

              <div className="group-success-actions">
                <button type="button" onClick={copyRequestSummary}>
                  <Copy size={17} />
                  {copyStatus || "Copy summary"}
                </button>

                <button type="button" onClick={resetRequest}>
                  <RefreshCw size={17} />
                  Edit request
                </button>

                <button type="button" onClick={() => navigate("/")}>
                  <Search size={17} />
                  Search flights
                </button>
              </div>
            </div>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className="status-tabs">
                <button
                  type="button"
                  className={formData.tripType === "ONE_WAY" ? "active" : ""}
                  onClick={() => updateField("tripType", "ONE_WAY")}
                >
                  One Way
                </button>

                <button
                  type="button"
                  className={formData.tripType === "ROUND_TRIP" ? "active" : ""}
                  onClick={() => updateField("tripType", "ROUND_TRIP")}
                >
                  Round Trip
                </button>
              </div>

              <div className="utility-input-grid">
                <label>
                  <MapPin size={18} />
                  <span>Origin</span>
                  <input
                    value={formData.source}
                    onChange={(event) => updateField("source", event.target.value)}
                    placeholder="Delhi"
                    required
                  />
                </label>

                <label>
                  <MapPin size={18} />
                  <span>Destination</span>
                  <input
                    value={formData.destination}
                    onChange={(event) =>
                      updateField("destination", event.target.value)
                    }
                    placeholder="Dubai"
                    required
                  />
                </label>
              </div>

              <div className="utility-input-grid">
                <label>
                  <CalendarDays size={18} />
                  <span>Departure</span>
                  <input
                    type="date"
                    min={getToday()}
                    value={formData.departureDate}
                    onChange={(event) =>
                      updateField("departureDate", event.target.value)
                    }
                    required
                  />
                </label>

                {formData.tripType === "ROUND_TRIP" ? (
                  <label>
                    <CalendarDays size={18} />
                    <span>Return</span>
                    <input
                      type="date"
                      min={formData.departureDate || getToday()}
                      value={formData.returnDate}
                      onChange={(event) =>
                        updateField("returnDate", event.target.value)
                      }
                      required
                    />
                  </label>
                ) : (
                  <label>
                    <UsersRound size={18} />
                    <span>Passengers</span>
                    <input
                      type="number"
                      min="10"
                      value={formData.passengers}
                      onChange={(event) =>
                        updateField("passengers", event.target.value)
                      }
                      required
                    />
                  </label>
                )}
              </div>

              {formData.tripType === "ROUND_TRIP" && (
                <div className="utility-input-grid">
                  <label>
                    <UsersRound size={18} />
                    <span>Passengers</span>
                    <input
                      type="number"
                      min="10"
                      value={formData.passengers}
                      onChange={(event) =>
                        updateField("passengers", event.target.value)
                      }
                      required
                    />
                  </label>

                  <label>
                    <Plane size={18} />
                    <span>Cabin class</span>
                    <select
                      value={formData.cabinClass}
                      onChange={(event) =>
                        updateField("cabinClass", event.target.value)
                      }
                    >
                      <option>Economy</option>
                      <option>Premium Economy</option>
                      <option>Business</option>
                    </select>
                  </label>
                </div>
              )}

              {formData.tripType === "ONE_WAY" && (
                <div className="utility-input-grid single">
                  <label>
                    <Plane size={18} />
                    <span>Cabin class</span>
                    <select
                      value={formData.cabinClass}
                      onChange={(event) =>
                        updateField("cabinClass", event.target.value)
                      }
                    >
                      <option>Economy</option>
                      <option>Premium Economy</option>
                      <option>Business</option>
                    </select>
                  </label>
                </div>
              )}

              <div className="utility-input-grid">
                <label>
                  <Mail size={18} />
                  <span>Email</span>
                  <input
                    type="email"
                    value={formData.email}
                    onChange={(event) =>
                      updateField("email", event.target.value)
                    }
                    placeholder="coordinator@example.com"
                    required
                  />
                </label>

                <label>
                  <Phone size={18} />
                  <span>Contact preference</span>
                  <select
                    value={formData.contactPreference}
                    onChange={(event) =>
                      updateField("contactPreference", event.target.value)
                    }
                  >
                    <option>Phone</option>
                    <option>Email</option>
                    <option>WhatsApp</option>
                  </select>
                </label>
              </div>

              <div className="utility-input-grid single">
                <label>
                  <Phone size={18} />
                  <span>Phone</span>
                  <input
                    value={formData.phone}
                    onChange={(event) => updateField("phone", event.target.value)}
                    placeholder="+91 9876543210"
                    required
                  />
                </label>
              </div>

              <label className="utility-textarea-field">
                <span>Special requirements</span>
                <textarea
                  value={formData.notes}
                  onChange={(event) => updateField("notes", event.target.value)}
                  placeholder="Meal preference, baggage needs, flexible dates..."
                />
              </label>

              <div className="group-request-preview">
                <span>Live request preview</span>
                <strong>
                  {formData.source || "Origin"} to{" "}
                  {formData.destination || "Destination"}
                </strong>
                <p>
                  {formData.passengers || "10"} travellers,{" "}
                  {formData.cabinClass},{" "}
                  {formData.departureDate || "departure date pending"}
                  {formData.returnDate ? ` to ${formData.returnDate}` : ""}
                </p>
              </div>

              <button type="submit" className="utility-primary-button">
                <Send size={18} />
                Submit request
              </button>
            </form>
          )}
        </section>
      </main>
    </div>
  );
}

export default GroupBookingPage;
