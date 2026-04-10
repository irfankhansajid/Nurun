import { useState } from "react";
import api from "../api/axios";

import { useNavigate, Link } from "react-router-dom";

const LoginPage = ({ theme, toggleTheme }) => {
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  const navigate = useNavigate();

  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setFieldErrors({});

    try {
      const response = await api.post("/api/auth/login", form);
      localStorage.setItem("token", response.data.token);
      navigate("/chat");
    } catch (error) {
      const data = error?.response?.data;
      const firstFieldError =
        data?.fieldErrors && Object.values(data.fieldErrors).length > 0
          ? Object.values(data.fieldErrors)[0]
          : null;

      setFieldErrors(data?.fieldErrors || {});
      setError(data?.message || firstFieldError || "Login failed.");
    } finally {
      setLoading(false);
    }
  };

  const inputClassName =
    "w-full rounded-xl border px-3 py-2.5 text-sm outline-none transition focus:ring-2 focus:ring-offset-0";

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4 py-10 dark:bg-slate-900">
      <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-800 sm:p-8">
        <div className="mb-8 flex items-start justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Nurun</h1>
            <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">Sign in to continue</p>
          </div>
          <button
            type="button"
            onClick={toggleTheme}
            className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 dark:border-slate-600 dark:text-slate-200 dark:hover:bg-slate-700"
          >
            {theme === "dark" ? "Light" : "Dark"}
          </button>
        </div>

        {error && (
          <p className="mb-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700 dark:border-red-900/50 dark:bg-red-950/40 dark:text-red-300">
            {error}
          </p>
        )}

        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-slate-700 dark:text-slate-200">Email</label>
            <input
              className={`${inputClassName} border-slate-300 text-slate-900 focus:border-slate-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:focus:border-slate-400`}
              type="email"
              name="email"
              value={form.email}
              onChange={(e) =>
                setForm({ ...form, [e.target.name]: e.target.value })
              }
              placeholder="your@email.com"
              required
            />
            {fieldErrors?.email && (
              <p className="text-xs text-red-600 dark:text-red-300">{fieldErrors.email}</p>
            )}
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-slate-700 dark:text-slate-200">Password</label>
            <input
              className={`${inputClassName} border-slate-300 text-slate-900 focus:border-slate-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:focus:border-slate-400`}
              type="password"
              name="password"
              value={form.password}
              onChange={(e) =>
                setForm({ ...form, [e.target.name]: e.target.value })
              }
              placeholder="••••••••"
              required
            />
            {fieldErrors?.password && (
              <p className="text-xs text-red-600 dark:text-red-300">{fieldErrors.password}</p>
            )}
          </div>

          <button
            type="submit"
            disabled={loading}
            className="mt-1 rounded-xl bg-slate-900 py-2.5 text-sm font-semibold text-white disabled:opacity-50 dark:bg-slate-100 dark:text-slate-900"
          >
            {loading ? "Signing in..." : "Sign In"}
          </button>
        </form>

        <p className="mt-5 text-center text-sm text-slate-500 dark:text-slate-400">
          No account?{" "}
          <Link to="/register" className="font-semibold text-slate-900 dark:text-slate-100">
            Register
          </Link>
        </p>
      </div>
    </div>
  );
};
export default LoginPage;
