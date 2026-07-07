import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  Copy,
  Plane,
  Search,
  Tag,
} from "lucide-react";
import { skyLinkOffers } from "../data/offerData";

function OffersPage() {
  const navigate = useNavigate();
  const [journeyType, setJourneyType] = useState("All");
  const [bank, setBank] = useState("All");
  const [copiedCode, setCopiedCode] = useState("");

  const banks = useMemo(
    () => ["All", ...new Set(skyLinkOffers.map((offer) => offer.bank))],
    []
  );

  const filteredOffers = skyLinkOffers.filter((offer) => {
    const matchesType =
      journeyType === "All" || offer.journeyType === journeyType;
    const matchesBank = bank === "All" || offer.bank === bank;

    return matchesType && matchesBank;
  });

  const copyCode = async (event, code) => {
    event.stopPropagation();

    try {
      await navigator.clipboard.writeText(code);
    } catch {
      // Clipboard can be blocked by the browser; the visible state still helps.
    }

    setCopiedCode(code);
    window.setTimeout(() => setCopiedCode(""), 1400);
  };

  return (
    <div className="my-bookings-page utility-page offers-page">
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

      <main className="offers-page-shell">
        <aside className="offer-filter-panel">
          <h2>Filters</h2>

          <div className="offer-filter-block">
            <span>Journey type</span>
            {["All", "Domestic", "International"].map((type) => (
              <button
                type="button"
                className={journeyType === type ? "active" : ""}
                key={type}
                onClick={() => setJourneyType(type)}
              >
                {type}
              </button>
            ))}
          </div>

          <div className="offer-filter-block">
            <span>Popular banks</span>
            {banks.map((bankName) => (
              <button
                type="button"
                className={bank === bankName ? "active" : ""}
                key={bankName}
                onClick={() => setBank(bankName)}
              >
                {bankName}
              </button>
            ))}
          </div>
        </aside>

        <section className="offers-catalog">
          <div className="offers-title-row">
            <div>
              <span>SKYLINK OFFERS</span>
              <h2>Flight deals and bank benefits</h2>
            </div>
            <p>{filteredOffers.length} offers available</p>
          </div>

          <div className="offer-list-grid">
            {filteredOffers.map((offer) => (
              <article
                className="offer-list-card"
                key={offer.slug}
                onClick={() => navigate(`/offers/${offer.slug}`)}
              >
                <div className={`offer-list-visual ${offer.heroClass}`}>
                  <Tag size={24} />
                  <strong>{offer.bank}</strong>
                </div>

                <div className="offer-list-body">
                  <span>{offer.category}</span>
                  <h3>{offer.detailTitle}</h3>
                  <p>Expires on {offer.expiresOn}</p>

                  <div className="offer-code-row">
                    <strong>{offer.coupon}</strong>
                    <button
                      type="button"
                      onClick={(event) => copyCode(event, offer.coupon)}
                    >
                      <Copy size={15} />
                      {copiedCode === offer.coupon ? "Copied" : "Copy"}
                    </button>
                  </div>

                  <div className="offer-card-actions">
                    <button
                      type="button"
                      onClick={(event) => {
                        event.stopPropagation();
                        navigate(`/offers/${offer.slug}`);
                      }}
                    >
                      Details
                    </button>

                    <button
                      type="button"
                      onClick={(event) => {
                        event.stopPropagation();
                        navigate("/");
                      }}
                    >
                      <Search size={15} />
                      Search flights
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </section>
      </main>
    </div>
  );
}

export default OffersPage;
