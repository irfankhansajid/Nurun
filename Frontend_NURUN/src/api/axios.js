import axios from "axios";

const baseUrl = import.meta.env.VITE_API_URL || "";

const api = axios.create({
  baseURL: baseUrl,
  headers: {
    common: {
      "Content-Type": "application/json",
    },
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  const requestUrl = config.url || "";

  const isAuthEndPoint = requestUrl.startsWith("/api/auth/");

  if (token && !isAuthEndPoint) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});


api.interceptors.response.use(
    (response) => response,

    (error) => {
      const status = error?.response?.status;
      const requestUrl = error?.config?.url || "";
      const isAuthEndpoint = requestUrl.startsWith("/api/auth/");


      if (status === 401) {

        if (isAuthEndpoint) {
          return Promise.reject(error);
        }
        console.warn("Unauthorized! Token expired or invalid. Logging out...");

        localStorage.removeItem("token");

        if (window.location.pathname !== "/login") {
          window.location.href = "/login";
        }
      }
      return Promise.reject(error);
    }
)

export default api;
