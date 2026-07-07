import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  Eye,
  EyeOff,
  LockKeyhole,
  Mail,
  Plane,
  ShieldCheck,
} from "lucide-react";
import api from "../api/axiosConfig";
import { saveAuth } from "../utils/auth";

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

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const [loginMethod, setLoginMethod] = useState("password");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [otpCode, setOtpCode] = useState("");
  const [otpSent, setOtpSent] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const notice = location.state?.notice || "";
  const redirectTo = location.state?.redirectTo || "/";
  const normalizedEmail = email.trim().toLowerCase();
  const isOtpMode = loginMethod === "otp";

  const saveLoginAndNavigate = async (data) => {
    const token = data.token || data.accessToken || data.jwtToken;

    if (!token) {
      throw new Error("JWT token was not found in login response.");
    }

    const user = {
      id: data.userId || data.id || null,
      email: data.email || normalizedEmail,
      fullName: data.fullName || "",
      firstName: data.firstName || "",
      lastName: data.lastName || "",
      role: data.role || "USER",
    };

    saveAuth(token, user);

    try {
      const profileResponse = await api.get("/users/me");
      saveAuth(token, profileResponse.data);
    } catch (profileError) {
      console.error(profileError);
    }

    navigate(redirectTo, {
      replace: true,
    });
  };

  const sendLoginOtp = async () => {
    if (!normalizedEmail) {
      setError("OTP bhejne se pehle email address enter karein.");
      return;
    }

    setLoading(true);
    setError("");
    setMessage("");

    try {
      await api.post("/auth/login/otp/send", {
        email: normalizedEmail,
      });

      setOtpSent(true);
      setMessage("Login OTP email par bhej diya gaya hai.");
    } catch (requestError) {
      setError(
        getApiErrorMessage(
          requestError,
          "Unable to send login OTP."
        )
      );
    } finally {
      setLoading(false);
    }
  };

  const resetOtpState = () => {
    setOtpSent(false);
    setOtpCode("");
  };

  const verifyLoginOtp = async () => {
    if (!normalizedEmail || !otpCode.trim()) {
      setError("Email aur OTP code enter karein.");
      return;
    }

    setLoading(true);
    setError("");
    setMessage("");

    try {
      const response = await api.post("/auth/login/otp/verify", {
        identifier: normalizedEmail,
        otpCode: otpCode.trim(),
        otpType: "EMAIL",
        otpPurpose: "LOGIN",
      });

      await saveLoginAndNavigate(response.data);
    } catch (requestError) {
      console.error(requestError);

      setError(
        getApiErrorMessage(
          requestError,
          "Unable to login with OTP."
        )
      );
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async (event) => {
    event.preventDefault();

    if (isOtpMode) {
      if (otpSent) {
        await verifyLoginOtp();
      } else {
        await sendLoginOtp();
      }

      return;
    }

    setLoading(true);
    setError("");
    setMessage("");

    try {
      const response = await api.post("/auth/login", {
        email: normalizedEmail,
        password,
      });

      await saveLoginAndNavigate(response.data);
    } catch (requestError) {
      console.error(requestError);

      setError(
        getApiErrorMessage(
          requestError,
          "Unable to login. Please check your credentials."
        )
      );
    } finally {
      setLoading(false);
    }
  };

  const switchLoginMethod = (nextMethod) => {
    setLoginMethod(nextMethod);
    resetOtpState();
    setError("");
    setMessage("");
  };

  return (
    <div className="login-page">
      <section className="login-brand-panel">
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
            <span>WELCOME ABOARD</span>

            <h2>
              Your next journey
              <strong> starts here.</strong>
            </h2>

            <p>
              Login to book flights, manage passengers and access
              all your SkyLink bookings in one place.
            </p>
          </div>
        </div>
      </section>

      <section className="login-form-panel">
        <div className="login-form-container">
          <div className="login-form-heading">
            <span>PASSENGER LOGIN</span>
            <h2>Welcome back</h2>
            <p>Password ya email OTP se login karein.</p>
          </div>

          <div className="auth-method-toggle">
            <button
              type="button"
              className={!isOtpMode ? "active" : ""}
              onClick={() => switchLoginMethod("password")}
            >
              Password
            </button>

            <button
              type="button"
              className={isOtpMode ? "active" : ""}
              onClick={() => switchLoginMethod("otp")}
            >
              Email OTP
            </button>
          </div>

          <form className="login-form" onSubmit={handleLogin}>
            <div className="login-input-group">
              <label htmlFor="email">Email address</label>

              <div className="login-input">
                <Mail size={19} />

                <input
                  id="email"
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

            {!isOtpMode && (
              <div className="login-input-group">
                <label htmlFor="password">Password</label>

                <div className="login-input">
                  <LockKeyhole size={19} />

                  <input
                    id="password"
                    type={showPassword ? "text" : "password"}
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                    placeholder="Enter your password"
                    autoComplete="current-password"
                    required={!isOtpMode}
                  />

                  <button
                    type="button"
                    className="password-toggle"
                    onClick={() =>
                      setShowPassword((currentValue) => !currentValue)
                    }
                    aria-label="Toggle password visibility"
                  >
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
              </div>
            )}

            {isOtpMode && (
              <div className="login-input-group">
                <label htmlFor="loginOtp">Email OTP</label>

                <div className="login-input">
                  <ShieldCheck size={19} />

                  <input
                    id="loginOtp"
                    value={otpCode}
                    onChange={(event) => setOtpCode(event.target.value)}
                    placeholder={otpSent ? "6 digit code" : "Send OTP first"}
                    inputMode="numeric"
                    maxLength={6}
                    disabled={!otpSent}
                  />
                </div>

                {otpSent && (
                  <button
                    type="button"
                    className="inline-link-button resend-otp-button"
                    onClick={sendLoginOtp}
                    disabled={loading}
                  >
                    Resend OTP
                  </button>
                )}
              </div>
            )}

            {(notice || message) && (
              <div className="login-success">
                {message || notice}
              </div>
            )}

            {error && <div className="login-error">{error}</div>}

            <button
              type="submit"
              className="login-submit-button"
              disabled={loading}
            >
              {loading
                ? isOtpMode && !otpSent
                  ? "Sending OTP..."
                  : "Signing in..."
                : isOtpMode
                  ? otpSent
                    ? "Login with OTP"
                    : "Send Login OTP"
                  : "Login to SkyLink"}
            </button>
          </form>

          <p className="login-footer-text">
            New to SkyLink?{" "}
            <button
              type="button"
              className="inline-link-button"
              onClick={() => navigate("/register")}
            >
              Create account
            </button>
          </p>
        </div>
      </section>
    </div>
  );
}

export default LoginPage;
