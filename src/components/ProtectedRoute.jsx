import { Navigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

// eslint-disable-next-line react/prop-types
export default function ProtectedRoute({ children }) {
  const { user, loading } = useAuth(); // Add loading state from auth context

  if (loading) {
    // You can replace this with a proper loading component
    return <div>Loading...</div>;
  }

  if (!user) {
    return <Navigate to="/login" />;
  }

  return children;
}
