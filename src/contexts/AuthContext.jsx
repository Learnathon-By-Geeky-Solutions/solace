import { createContext, useContext, useEffect, useState } from "react";
import { supabase } from "../lib/supabase";

const AuthContext = createContext({});

// eslint-disable-next-line react/prop-types
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check active sessions and sets the user
    supabase.auth
      .getSession()
      .then(({ data: { session } }) => {
        setUser(session?.user ?? null);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Session check error:", err);
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
  // const signOut = async () => {
  //   try {
  //     return await supabase.auth.signOut();
  //   } catch (err) {
  //     console.error("Sign out error:", err);
  //   }
  // };

  const signOut = async () => {
    try {
      const response = await supabase.auth.signOut();
      return { error: null };
    } catch (err) {
      console.error("Sign out error:", err);
      return {
        error: {
          message:
            err instanceof Error
              ? err.message
              : "An error occurred during sign out",
        },
      };
    }
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
      {loading ? (
        <div
          style={{ display: "flex", justifyContent: "center", padding: "20px" }}
        >
          Loading...
        </div>
      ) : (
        children
      )}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  return useContext(AuthContext);
};
