import axios from "axios";
import { demoAdapter } from "./demoApi";
import { getToken, logout } from "../utils/auth";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ||
  "http://localhost:8081/api";

const DEMO_API_ENABLED =
  import.meta.env.VITE_DEMO_MODE === "true" ||
  (
    typeof window !== "undefined" &&
    window.location.hostname.endsWith("github.io") &&
    !import.meta.env.VITE_API_BASE_URL
  );

const api = axios.create({
  baseURL: API_BASE_URL,
  adapter: DEMO_API_ENABLED ? demoAdapter : undefined,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use(
  (config) => {
    const token = getToken();

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      logout();

      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }

    return Promise.reject(error);
  }
);

export default api;
