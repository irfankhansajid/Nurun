import { Navigate } from "react-router-dom";


const isTokenExpired = (token) => {
  try {
    const payloadBase64 = token.split(".")[1];
    if (!payloadBase64) return true;

    const payloadJson = atob(payloadBase64.replace(/-/g, "+").replace(/_/g, "/"));
    const payload = JSON.parse(payloadJson);

    if (!payload.exp) return true;
    const nowInSecond = Math.floor(Date.now() / 1000);

    return payload.exp <= nowInSecond;
  } catch {
    return true;
  }
}

const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem("token");
  if (!token || isTokenExpired(token)) {
    localStorage.removeItem("token");
    return <Navigate to="/login" />;
  }

  return children;
};

export default ProtectedRoute;
