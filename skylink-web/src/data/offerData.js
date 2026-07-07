export const skyLinkOffers = [
  {
    slug: "hdfc-domestic-emi",
    bank: "HDFC Bank",
    category: "Domestic Flights",
    journeyType: "Domestic",
    title: "Up to INR 2,000 off",
    detailTitle:
      "Up to INR 2,000 off on domestic SkyLink flights with HDFC Bank EMI",
    text: "on domestic SkyLink flights",
    coupon: "SKYHDFC",
    expiresOn: "31 Jul 2026",
    minimumBooking: "INR 3,000",
    heroClass: "offer-hero-hdfc",
    about: [
      "Instant discounts from INR 300 to INR 2,000 based on booking value.",
      "Valid for domestic SkyLink flight bookings.",
      "Applicable on selected HDFC Bank credit card EMI payments.",
    ],
    rows: [
      ["INR 3,000 - INR 7,500", "3 months", "INR 300", "SKYHDFC3"],
      ["INR 7,501 - INR 14,999", "3 months", "INR 750", "SKYHDFC3"],
      ["INR 15,000+", "6 months and above", "INR 2,000", "SKYHDFC6"],
    ],
  },
  {
    slug: "icici-domestic-emi",
    bank: "ICICI Bank",
    category: "Domestic Flights",
    journeyType: "Domestic",
    title: "Up to INR 2,500 off",
    detailTitle:
      "Up to INR 2,500 off on domestic flights with ICICI Bank Credit Card EMI",
    text: "on domestic card EMI",
    coupon: "SKYICICI",
    expiresOn: "30 Sep 2026",
    minimumBooking: "INR 5,000",
    heroClass: "offer-hero-icici",
    about: [
      "Get up to INR 2,500 with ICICI Bank Credit Card EMI.",
      "The offer is valid only on domestic flight bookings.",
      "The coupon is valid once per card during the offer period.",
    ],
    rows: [
      ["INR 5,000 - INR 9,999", "3 months", "INR 750", "SKYICICI"],
      ["INR 10,000 - INR 19,999", "6 months", "INR 1,500", "SKYICICI"],
      ["INR 20,000+", "6 months and above", "INR 2,500", "SKYICICI"],
    ],
  },
  {
    slug: "icici-international-emi",
    bank: "ICICI Bank",
    category: "International Flights",
    journeyType: "International",
    title: "Up to INR 7,500 off",
    detailTitle:
      "Up to INR 7,500 off on international flights with ICICI Bank Credit Card EMI",
    text: "on international card EMI",
    coupon: "SKYICICINT",
    expiresOn: "30 Sep 2026",
    minimumBooking: "INR 15,000",
    heroClass: "offer-hero-icici-int",
    about: [
      "Save up to INR 7,500 on international SkyLink flight bookings.",
      "Valid on selected ICICI Bank Credit Card EMI payments.",
      "Discount is calculated on the booking amount excluding add-ons.",
    ],
    rows: [
      ["INR 15,000 - INR 29,999", "3 months", "INR 2,000", "SKYICICINT"],
      ["INR 30,000 - INR 59,999", "6 months", "INR 4,500", "SKYICICINT"],
      ["INR 60,000+", "6 months and above", "INR 7,500", "SKYICICINT"],
    ],
  },
  {
    slug: "sbi-visa-sale",
    bank: "SBI Visa",
    category: "Domestic Flights",
    journeyType: "Domestic",
    title: "Flat 12% off",
    detailTitle:
      "Flat 12% off on domestic flights with select SBI Visa debit cards",
    text: "on debit card bookings",
    coupon: "SKYSBI12",
    expiresOn: "30 Sep 2026",
    minimumBooking: "INR 5,000",
    heroClass: "offer-hero-sbi",
    about: [
      "Get flat 12% off on domestic SkyLink flights.",
      "Valid for select SBI Visa Platinum, Business Platinum and Signature debit cards.",
      "Applicable only on Pay Now bookings.",
    ],
    rows: [
      ["INR 5,000+", "Debit card", "12% off", "SKYSBI12"],
      ["INR 10,000+", "Debit card", "Up to INR 1,500", "SKYSBI12"],
    ],
  },
  {
    slug: "axis-priority-refund",
    bank: "Axis Bank",
    category: "Flexible Fares",
    journeyType: "Domestic",
    title: "Priority refund benefit",
    detailTitle:
      "Priority refund benefit on SkyLink flexible fares with Axis Bank",
    text: "on flexible fares",
    coupon: "SKYAXIS",
    expiresOn: "31 Aug 2026",
    minimumBooking: "INR 4,000",
    heroClass: "offer-hero-axis",
    about: [
      "Book flexible fares and get priority refund support.",
      "Valid on domestic SkyLink bookings paid through Axis Bank cards.",
      "Refund timeline depends on airline and bank settlement windows.",
    ],
    rows: [
      ["INR 4,000+", "Flexible fare", "Priority refund", "SKYAXIS"],
      ["INR 8,000+", "Flexible fare", "Support add-on", "SKYAXIS"],
    ],
  },
];

export const offerSteps = [
  "Search and choose your preferred SkyLink flight.",
  "Apply the coupon code at the time of booking.",
  "Proceed to payment and complete the booking with an eligible card.",
];

export const offerTerms = [
  "The customer should be logged in to a SkyLink account to avail the offer.",
  "The offer is valid once per card per product during the offer period.",
  "Discounts are calculated on the booking amount excluding convenience fee and add-ons.",
  "The offer cannot be combined with any other SkyLink coupon.",
  "In case of cancellation, the promotional discount may be deducted from the refund.",
  "SkyLink reserves the right to modify or withdraw the offer in case of misuse.",
];

export const getOfferBySlug = (slug) =>
  skyLinkOffers.find((offer) => offer.slug === slug) || skyLinkOffers[0];
