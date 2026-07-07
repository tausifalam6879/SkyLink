import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  CalendarCheck2,
  CheckCircle2,
  Mail,
  Phone,
  Plane,
  ShieldCheck,
  Ticket,
  UserRound,
} from "lucide-react";
import api from "../api/axiosConfig";
import { getUser, saveAuth, getToken } from "../utils/auth";

function UserDashboardPage() {
  const navigate = useNavigate();

  const [profile, setProfile] = useState(() => getUser());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    const loadProfile = async () => {
      setLoading(true);
      setError("");

      try {
        const response = await api.get("/users/me");

        if (!active) {
          return;
        }

        const freshProfile = response.data;

        setProfile(freshProfile);

        const token = getToken();

        if (token) {
          saveAuth(token, freshProfile);
        }
      } catch (requestError) {
        if (!active) {
          return;
        }

        setError(
          requestError.response?.data?.message ||
            "Unable to load your profile."
        );
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    loadProfile();

    return () => {
      active = false;
    };
  }, []);

  const displayName =
    profile?.fullName ||
    [profile?.firstName, profile?.lastName].filter(Boolean).join(" ") ||
    "SkyLink Passenger";

  return (
    <div className="my-bookings-page dashboard-page">
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
        <section className="dashboard-hero-panel">
          <div>
            <span>PASSENGER DASHBOARD</span>
            <h2>{displayName}</h2>
            <p>
              Manage your profile, bookings and SkyLink travel tools from one
              place.
            </p>
          </div>

          <div className="dashboard-avatar">
            <UserRound size={42} />
          </div>
        </section>

        {error && <div className="booking-error">{error}</div>}

        <section className="dashboard-grid">
          <article className="dashboard-card profile-card">
            <div className="dashboard-card-heading">
              <UserRound size={20} />
              <h3>Profile details</h3>
            </div>

            {loading ? (
              <p className="dashboard-muted">Loading profile...</p>
            ) : (
              <div className="profile-detail-list">
                <div>
                  <Mail size={18} />
                  <span>Email</span>
                  <strong>{profile?.email || "Not available"}</strong>
                </div>

                <div>
                  <Phone size={18} />
                  <span>Mobile</span>
                  <strong>{profile?.mobileNumber || "Not available"}</strong>
                </div>

                <div>
                  <Phone size={18} />
                  <span>WhatsApp</span>
                  <strong>{profile?.whatsappNumber || "Not available"}</strong>
                </div>

                <div>
                  <ShieldCheck size={18} />
                  <span>Role</span>
                  <strong>{profile?.role || "USER"}</strong>
                </div>
              </div>
            )}
          </article>

          <article className="dashboard-card">
            <div className="dashboard-card-heading">
              <CheckCircle2 size={20} />
              <h3>Verification</h3>
            </div>

            <div className="verification-list">
              <span className={profile?.emailVerified ? "verified" : ""}>
                Email verified
              </span>
              <span className={profile?.mobileVerified ? "verified" : ""}>
                Mobile verified
              </span>
              <span className={profile?.whatsappVerified ? "verified" : ""}>
                WhatsApp verified
              </span>
            </div>
          </article>

          <article className="dashboard-card action-card">
            <div className="dashboard-card-heading">
              <Ticket size={20} />
              <h3>Travel actions</h3>
            </div>

            <div className="dashboard-actions">
              <button type="button" onClick={() => navigate("/my-bookings")}>
                <CalendarCheck2 size={18} />
                My bookings
              </button>

              <button type="button" onClick={() => navigate("/flight-status")}>
                <Plane size={18} />
                Flight status
              </button>
            </div>
          </article>
        </section>
      </main>
    </div>
  );
}

export default UserDashboardPage;
