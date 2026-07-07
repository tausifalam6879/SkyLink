import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  ArrowLeft,
  CheckCircle2,
  Copy,
  Plane,
  Search,
} from "lucide-react";
import {
  getOfferBySlug,
  offerSteps,
  offerTerms,
} from "../data/offerData";

function OfferDetailPage() {
  const navigate = useNavigate();
  const { offerSlug } = useParams();
  const offer = getOfferBySlug(offerSlug);
  const [copied, setCopied] = useState(false);

  const copyCode = async () => {
    try {
      await navigator.clipboard.writeText(offer.coupon);
    } catch {
      // Some browsers block clipboard access outside secure gestures.
    }

    setCopied(true);
    window.setTimeout(() => setCopied(false), 1400);
  };

  return (
    <div className="my-bookings-page utility-page offer-detail-page">
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
          onClick={() => navigate("/offers")}
        >
          <ArrowLeft size={18} />
          All offers
        </button>
      </nav>

      <main className="offer-detail-shell">
        <aside className="offer-summary-card">
          <div className={`offer-summary-visual ${offer.heroClass}`}>
            <Plane size={34} />
            <strong>{offer.bank}</strong>
          </div>

          <div className="offer-summary-body">
            <span>{offer.category}</span>
            <h2>{offer.detailTitle}</h2>

            <dl>
              <div>
                <dt>Expires on</dt>
                <dd>{offer.expiresOn}</dd>
              </div>
              <div>
                <dt>Min. booking</dt>
                <dd>{offer.minimumBooking}</dd>
              </div>
            </dl>

            <div className="offer-code-row">
              <strong>{offer.coupon}</strong>
              <button type="button" onClick={copyCode}>
                <Copy size={15} />
                {copied ? "Copied" : "Copy"}
              </button>
            </div>

            <button
              type="button"
              className="offer-search-button"
              onClick={() => navigate("/")}
            >
              <Search size={17} />
              Search flights
            </button>
          </div>
        </aside>

        <section className="offer-detail-content">
          <h2>About the offer</h2>

          <ul className="offer-bullet-list">
            {offer.about.map((item) => (
              <li key={item}>
                <CheckCircle2 size={18} />
                {item}
              </li>
            ))}
          </ul>

          <div className="offer-table">
            <div className="offer-table-row heading">
              <span>Booking amount</span>
              <span>Payment type</span>
              <span>Discount</span>
              <span>Coupon</span>
            </div>
            {offer.rows.map((row) => (
              <div className="offer-table-row" key={row.join("-")}>
                {row.map((cell) => (
                  <span key={cell}>{cell}</span>
                ))}
              </div>
            ))}
          </div>

          <h2>How to avail the offer?</h2>
          <ul>
            {offerSteps.map((step) => (
              <li key={step}>{step}</li>
            ))}
          </ul>

          <h2>Terms and conditions</h2>
          <ul>
            {offerTerms.map((term) => (
              <li key={term}>{term}</li>
            ))}
          </ul>
        </section>
      </main>
    </div>
  );
}

export default OfferDetailPage;
