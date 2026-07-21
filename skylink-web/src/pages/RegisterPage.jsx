import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  CheckCircle2,
  LockKeyhole,
  Mail,
  Phone,
  Plane,
  ShieldCheck,
  UserRound,
} from "lucide-react";
import api from "../api/axiosConfig";

function getApiErrorMessage(requestError, fallbackMessage) {
  const responseData = requestError.response?.data;

  if (responseData?.message) {
    return responseData.message;
  }

  if (typeof responseData === "string") {
    return responseData;
  }

  if (requestError.request && !requestError.response) {
    return "SkyLink API se connection nahi ban raha. Backend server 8081 aur CORS origin check karein.";
  }

  return requestError.message || fallbackMessage;
}

function RegisterPage() {
  const navigate = useNavigate();

  const [authMethod, setAuthMethod] = useState("password");
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [mobileNumber, setMobileNumber] = useState("");
  const [whatsappNumber, setWhatsappNumber] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [otpCode, setOtpCode] = useState("");
  const [demoOtp, setDemoOtp] = useState("");
  const [otpSent, setOtpSent] = useState(false);
  const [otpVerified, setOtpVerified] = useState(false);
  const [loadingAction, setLoadingAction] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const normalizedEmail = email.trim().toLowerCase();
  const isOtpMode = authMethod === "otp";
  const hasPasswordDetails =
    password.length >= 6 && confirmPassword.length >= 6;
  const canCreateAccount =
    fullName.trim() &&
    normalizedEmail &&
    (isOtpMode ? otpVerified : hasPasswordDetails);

  const switchAuthMethod = (nextMethod) => {
    setAuthMethod(nextMethod);
    setError("");
    setMessage("");
  };

  const resetOtpState = () => {
    setOtpSent(false);
    setOtpVerified(false);
    setOtpCode("");
    setDemoOtp("");
  };

  const sendOtp = async () => {
    if (!normalizedEmail) {
      setError("OTP bhejne se pehle email address enter karein.");
      return;
    }

    setLoadingAction("send-otp");
    setError("");
    setMessage("");

    try {
      const response = await api.post("/otp/send", {
        identifier: normalizedEmail,
        otpType: "EMAIL",
        otpPurpose: "REGISTRATION",
      });
      const generatedDemoOtp = response.data?.demoOtp || "";

      setOtpSent(true);
      setOtpVerified(false);
      setOtpCode("");
      setDemoOtp(generatedDemoOtp);
      setMessage(
        generatedDemoOtp
          ? "Demo OTP neeche generate ho gaya hai. Yeh 5 minutes tak valid hai."
          : "Registration OTP email par bhej diya gaya hai."
      );
    } catch (requestError) {
      setError(
        getApiErrorMessage(
          requestError,
          "Unable to send OTP."
        )
      );
    } finally {
      setLoadingAction("");
    }
  };

  const verifyOtp = async () => {
    if (!normalizedEmail || !otpCode.trim()) {
      setError("Email aur OTP code enter karein.");
      return;
    }

    setLoadingAction("verify-otp");
    setError("");
    setMessage("");

    try {
      await api.post("/otp/verify", {
        identifier: normalizedEmail,
        otpCode: otpCode.trim(),
        otpType: "EMAIL",
        otpPurpose: "REGISTRATION",
      });

      setOtpVerified(true);
      setDemoOtp("");
      setMessage("Email OTP verified. Account create kar sakte hain.");
    } catch (requestError) {
      setError(
        getApiErrorMessage(
          requestError,
          "Unable to verify OTP."
        )
      );
    } finally {
      setLoadingAction("");
    }
  };

  const createAccount = async (event) => {
    event.preventDefault();

    if (!fullName.trim()) {
      setError("Full name required hai.");
      return;
    }

    if (!normalizedEmail) {
      setError("Email address required hai.");
      return;
    }

    if (!isOtpMode) {
      if (password.length < 6) {
        setError("Password kam se kam 6 characters ka hona chahiye.");
        return;
      }

      if (password !== confirmPassword) {
        setError("Passwords match nahi kar rahe.");
        return;
      }
    }

    if (isOtpMode && !otpVerified) {
      setError("OTP mode me account create karne se pehle OTP verify karein.");
      return;
    }

    setLoadingAction("register");
    setError("");
    setMessage("");

    try {
      await api.post("/users/register", {
        fullName: fullName.trim(),
        email: normalizedEmail,
        mobileNumber: mobileNumber.trim() || null,
        whatsappNumber: whatsappNumber.trim() || null,
        password: isOtpMode ? "" : password,
      });

      navigate("/login", {
        replace: true,
        state: {
          notice: isOtpMode
            ? "Account created. Login page par Email OTP option use karein."
            : "Account created successfully. Password se login karein.",
        },
      });
    } catch (requestError) {
      setError(
        getApiErrorMessage(
          requestError,
          "Unable to create account."
        )
      );
    } finally {
      setLoadingAction("");
    }
  };

  return (
    <div className="login-page register-page">
      <section className="login-brand-panel register-brand-panel">
        <div className="login-brand-content">
          <button
            type="button"
            className="back-home-button"
            onClick={() => navigate("/")}
          >
            <ArrowLeft size={18} />
            Back to flight search
          </button>

          <div className="login-logo">
            <div className="brand-icon">
              <Plane size={25} />
            </div>

            <div>
              <h1>SkyLink</h1>
              <span>Fly Beyond Boundaries</span>
            </div>
          </div>

          <div className="login-message">
            <span>CREATE ACCOUNT</span>

            <h2>
              Build your travel
              <strong> command center.</strong>
            </h2>

            <p>
              Save passenger details, book faster, choose seats and keep
              every SkyLink journey in one secure account.
            </p>
          </div>
        </div>
      </section>

      <section className="login-form-panel">
        <div className="login-form-container register-form-container">
          <div className="login-form-heading">
            <span>PASSENGER REGISTRATION</span>
            <h2>Join SkyLink</h2>
            <p>Password ya email OTP me se ek method choose karein.</p>
          </div>

          <div className="auth-method-toggle">
            <button
              type="button"
              className={!isOtpMode ? "active" : ""}
              onClick={() => switchAuthMethod("password")}
            >
              Password
            </button>

            <button
              type="button"
              className={isOtpMode ? "active" : ""}
              onClick={() => switchAuthMethod("otp")}
            >
              Email OTP
            </button>
          </div>

          <form className="login-form" onSubmit={createAccount}>
            <div className="login-input-group">
              <label htmlFor="fullName">Full name</label>

              <div className="login-input">
                <UserRound size={19} />

                <input
                  id="fullName"
                  value={fullName}
                  onChange={(event) => setFullName(event.target.value)}
                  placeholder="Md Tausif Alam"
                  autoComplete="name"
                  required
                />
              </div>
            </div>

            <div className="login-input-group">
              <label htmlFor="registerEmail">Email address</label>

              <div className="login-input">
                <Mail size={19} />

                <input
                  id="registerEmail"
                  type="email"
                  value={email}
                  onChange={(event) => {
                    setEmail(event.target.value);
                    resetOtpState();
                  }}
                  placeholder="you@example.com"
                  autoComplete="email"
                  required
                />
              </div>
            </div>

            {isOtpMode && (
              <div className="login-input-group">
                <label htmlFor="otpCode">Email OTP</label>

                <div className="register-otp-row">
                  <div className="login-input">
                    <ShieldCheck size={19} />

                    <input
                      id="otpCode"
                      value={otpCode}
                      onChange={(event) => setOtpCode(event.target.value)}
                      placeholder={otpSent ? "6 digit code" : "Send OTP first"}
                      inputMode="numeric"
                      maxLength={6}
                      disabled={!otpSent || otpVerified}
                    />
                  </div>

                  <button
                    type="button"
                    className="secondary-action-button otp-action-button"
                    onClick={otpSent ? verifyOtp : sendOtp}
                    disabled={
                      loadingAction === "send-otp" ||
                      loadingAction === "verify-otp" ||
                      otpVerified
                    }
                  >
                    {otpVerified
                      ? "Verified"
                      : loadingAction === "send-otp"
                        ? "Sending..."
                        : loadingAction === "verify-otp"
                          ? "Checking..."
                          : otpSent
                            ? "Verify"
                            : "Send OTP"}
                  </button>
                </div>

                {otpSent && !otpVerified && (
                  <button
                    type="button"
                    className="inline-link-button resend-otp-button"
                    onClick={sendOtp}
                    disabled={loadingAction === "send-otp"}
                  >
                    Resend OTP
                  </button>
                )}

                {demoOtp && otpSent && !otpVerified && (
                  <div className="demo-otp-card" role="status">
                    <div>
                      <span>Demo OTP</span>
                      <strong>{demoOtp}</strong>
                      <small>5 minutes valid - isi browser session ke liye</small>
                    </div>

                    <button
                      type="button"
                      onClick={() => {
                        setOtpCode(demoOtp);
                        setError("");
                      }}
                    >
                      Use code
                    </button>
                  </div>
                )}
              </div>
            )}

            <div className="passenger-form-grid register-contact-grid">
              <div className="login-input-group">
                <label htmlFor="mobileNumber">Mobile number</label>

                <div className="login-input">
                  <Phone size={19} />

                  <input
                    id="mobileNumber"
                    value={mobileNumber}
                    onChange={(event) => setMobileNumber(event.target.value)}
                    placeholder="Optional"
                    autoComplete="tel"
                  />
                </div>
              </div>

              <div className="login-input-group">
                <label htmlFor="whatsappNumber">WhatsApp number</label>

                <div className="login-input">
                  <Phone size={19} />

                  <input
                    id="whatsappNumber"
                    value={whatsappNumber}
                    onChange={(event) => setWhatsappNumber(event.target.value)}
                    placeholder="Optional"
                    autoComplete="tel"
                  />
                </div>
              </div>
            </div>

            {!isOtpMode && (
              <div className="passenger-form-grid register-contact-grid">
                <div className="login-input-group">
                  <label htmlFor="registerPassword">Password</label>

                  <div className="login-input">
                    <LockKeyhole size={19} />

                    <input
                      id="registerPassword"
                      type="password"
                      value={password}
                      onChange={(event) => setPassword(event.target.value)}
                      placeholder="Create password"
                      autoComplete="new-password"
                      minLength={6}
                      required={!isOtpMode}
                    />
                  </div>
                </div>

                <div className="login-input-group">
                  <label htmlFor="confirmPassword">Confirm password</label>

                  <div className="login-input">
                    <LockKeyhole size={19} />

                    <input
                      id="confirmPassword"
                      type="password"
                      value={confirmPassword}
                      onChange={(event) =>
                        setConfirmPassword(event.target.value)
                      }
                      placeholder="Repeat password"
                      autoComplete="new-password"
                      minLength={6}
                      required={!isOtpMode}
                    />
                  </div>
                </div>
              </div>
            )}

            {message && (
              <div className="login-success">
                <CheckCircle2 size={18} />
                {message}
              </div>
            )}

            {error && <div className="login-error">{error}</div>}

            <button
              type="submit"
              className="login-submit-button"
              disabled={loadingAction === "register" || !canCreateAccount}
            >
              {loadingAction === "register"
                ? "Creating account..."
                : "Create SkyLink Account"}
            </button>
          </form>

          <p className="login-footer-text">
            Already registered?{" "}
            <button
              type="button"
              className="inline-link-button"
              onClick={() => navigate("/login")}
            >
              Login instead
            </button>
          </p>
        </div>
      </section>
    </div>
  );
}

export default RegisterPage;
