import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  BadgeIndianRupee,
  CheckCircle2,
  CreditCard,
  Plane,
  ShieldCheck,
} from "lucide-react";
import { isAuthenticated } from "../utils/auth";

function CreditCardPage() {
  const navigate = useNavigate();

  const handleEligibility = () => {
    if (isAuthenticated()) {
      navigate("/user");
      return;
    }

    navigate("/login", {
      state: {
        redirectTo: "/travel-credit-card",
        message:
          "Log in to check your SkyLink credit card eligibility.",
      },
    });
  };

  return (
    <div className="my-bookings-page utility-page credit-card-page">
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

      <main className="credit-card-shell">
        <section className="credit-card-hero">
          <div>
            <span>SKYLINK CO-BRANDED CARD</span>
            <h2>SkyLink Travel Credit Card</h2>
            <p>
              A travel-first card concept for flight savings, faster refunds
              and smarter rewards inside your SkyLink account.
            </p>
          </div>

          <div className="credit-card-mock">
            <div className="credit-card-face">
              <span>SkyLink</span>
              <CreditCard size={42} />
              <strong>Fly More</strong>
              <small>Rewards | EMI | Refunds</small>
            </div>
            <div className="credit-card-back" />
          </div>
        </section>

        <section className="credit-benefit-grid">
          <article>
            <BadgeIndianRupee size={28} />
            <h3>Up to 25% flight benefits</h3>
            <p>Use curated card offers on selected SkyLink bookings.</p>
          </article>
          <article>
            <ShieldCheck size={28} />
            <h3>Priority refund support</h3>
            <p>Track eligible refunds directly from your SkyLink account.</p>
          </article>
          <article>
            <CheckCircle2 size={28} />
            <h3>Eligibility in minutes</h3>
            <p>Start with login and continue from your profile dashboard.</p>
          </article>
        </section>

        <button
          type="button"
          className="credit-eligibility-button"
          onClick={handleEligibility}
        >
          Check My Eligibility
        </button>
      </main>
    </div>
  );
}

export default CreditCardPage;
