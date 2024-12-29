import { createContext, useContext, useEffect, useState } from "react";
// import PropTypes from "prop-types";
import { supabase } from "../lib/supabase";

const AuthContext = createContext({});

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check active sessions and sets the user
    supabase.auth.getSession().then(({ data: { session } }) => {
      setUser(session?.user ?? null);
      setLoading(false);
    });

    // Listen for changes on auth state (login, logout, etc.)
    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange((_event, session) => {
      setUser(session?.user ?? null);
    });

    return () => subscription.unsubscribe();
  }, []);

  // Sign up with email/password
  const signUp = (email, password) => {
    return supabase.auth.signUp({
      email,
      password,
    });
  };

  // Sign in with email/password
  const signIn = (email, password) => {
    return supabase.auth.signInWithPassword({
      email,
      password,
    });
  };

  // Sign in with Google
  const signInWithGoogle = async () => {
    return supabase.auth.signInWithOAuth({
      provider: "google",
      options: {
        queryParams: {
          prompt: "consent",
          access_type: "offline",
        },
      },
    });
  };

  // Sign out
  const signOut = () => {
    return supabase.auth.signOut();
  };

  const value = {
    user,
    signUp,
    signIn,
    signInWithGoogle,
    signOut,
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
}

// AuthProvider.propTypes = {
//   children: PropTypes.node.isRequired,
// };

export const useAuth = () => {
  return useContext(AuthContext);
};
