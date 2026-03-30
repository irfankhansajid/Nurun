import axios from "axios";

const baseUrl = "http://localhost:8080";

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
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});


api.interceptors.response.use(
    (response) => {
      return response;
    },
    (error) => {
      if (error.response && error.response.status === 401) {
        console.warn("Unauthorized! Token expired or invalid. Logging out...");

        localStorage.removeItem("token");
        window.location.href = "/login";
      }
      return Promise.reject(error);
    }
)

export default api;
