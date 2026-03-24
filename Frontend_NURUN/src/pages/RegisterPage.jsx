import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../api/axios";

const RegisterPage = () => {
  const [form, setForm] = useState({
    email: "",
    password: "",
  });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const response = await api.post("/api/auth/register", form);

      navigate("/login");
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-sm w-full max-w-sm">
        <h1 className="text-2xl font-bold mb-1">Create Account</h1>
        <p className="text-sm text-gray-500 mb-6">Join Nurun</p>

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
            {loading ? "Creating account..." : "Create Account"}
          </button>
        </form>

        <p className="text-sm text-center text-gray-500 mt-5">
          Already have an account?{" "}
          <Link to="/login" className="font-semibold text-gray-900">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
};

export default RegisterPage;
