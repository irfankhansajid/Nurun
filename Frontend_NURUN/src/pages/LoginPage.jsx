import { useState } from "react";
import api from "../api/axios";

import { useNavigate, Link } from "react-router-dom";

const LoginPage = () => {
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      const response = await api.post("/api/auth/login", form);
      // console.log(response);
      localStorage.setItem("token", response.data.token);
      navigate("/chat");
    } catch (error) {
      // console.log(error);
      setError(error.response.data.message || "Login failed.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-sm w-full max-w-sm">
        <h1 className="text-2xl font-bold mb-1">Nurun</h1>
        <p className="text-sm text-gray-500 mb-6">Sign in to continue</p>

        {error && (
          <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded px-3 py-2 mb-4">
            {error}
          </p>
        )}

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-700">Email</label>
            <input
              className="px-3 py-2 border border-gray-300 rounded text-sm outline-none"
              type="email"
              name="email"
              value={form.email}
              onChange={(e) =>
                setForm({ ...form, [e.target.name]: e.target.value })
              }
              placeholder="your@email.com"
              required
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-700">
              Password
            </label>
            <input
              className="px-3 py-2 border border-gray-300 rounded text-sm outline-none"
              type="password"
              name="password"
              value={form.password}
              onChange={(e) =>
                setForm({ ...form, [e.target.name]: e.target.value })
              }
              placeholder="••••••••"
              required
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="mt-1 py-2 bg-gray-900 text-white text-sm font-semibold rounded disabled:opacity-50"
          >
            {loading ? "Signing in..." : "Sign In"}
          </button>
        </form>

        <p className="text-sm text-center text-gray-500 mt-5">
          No account?{" "}
          <Link to="/register" className="font-semibold text-gray-900">
            Register
          </Link>
        </p>
      </div>
    </div>
  );
};
export default LoginPage;
