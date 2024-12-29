import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { useEffect, useState } from "react";
import { AuthForm } from "./components/AuthForm";
import { Dashboard } from "./components/Dashboard";
import { supabase } from "./lib/supabase";
import { data } from "browserslist";
import { AuthProvider, useAuth } from "./contexts/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";

function App() {
  // const [session, setSession] = useState(null);

  // async function handleAuthRedirect() {
  //   const hash = window.location.hash;

  //   if (hash) {
  //     const params = new URLSearchParams(hash.substring(1));
  //     const access_token = params.get("access_token");

  //     localStorage.setItem("access_token", access_token);
  //     if (access_token) {
  //       try {
  //         // Get the session
  //         const { data: session, error } = await supabase.auth.setSession({
  //           access_token,
  //           refresh_token: params.get("refresh_token"),
  //         });
  //         if (error) {
  //           console.error("Error getting session:", error);
  //           return;
  //         }
  //         if (session) {
  //           // Save the session
  //           setSession(session);
  //           window.history.replaceState(
  //             {},
  //             document.title,
  //             window.location.pathname
  //           );
  //         }
  //       } catch (error) {
  //         console.error("Error getting session:", error);
  //       }
  //     }
  //     // // Get the session
  //     // const {
  //     //   data: { session },
  //     //   error,
  //     // } = await supabase.auth.getSession();

  //     // if (error) {
  //     //   console.error("Error getting session:", error);
  //     //   return;
  //     // }

  //     // if (session) {
  //     //   // Save the session
  //     //   setSession(session);
  //     //   // Clean up the URL by removing the hash
  //     //   window.history.replaceState(
  //     //     { data: session },
  //     //     document.title,
  //     //     window.location.pathname + "/dashboard"
  //     //   );
  //     // }
  //   }
  // }

  // useEffect(() => {
  //   supabase.auth.getSession().then(({ data: { session } }) => {
  //     setSession(session);
  //   });
  //   handleAuthRedirect();

  //   const {
  //     data: { subscription },
  //   } = supabase.auth.onAuthStateChange((_event, session) => {
  //     setSession(session);
  //   });

  //   return () => subscription.unsubscribe();
  // }, []);

  const { user } = useAuth();
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route
            path="/login"
            element={user ? <Navigate to="/dashboard" /> : <AuthForm />}
          />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
            // element={session ? <Dashboard /> : <Navigate to="/login" />}
          />
          <Route path="/" element={<Navigate to="/dashboard" />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
