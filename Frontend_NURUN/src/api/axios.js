import axios from "axios";

const baseUrl = "https://nurun.tech";

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

export default api;
