import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { AuthProvider, useAuth } from "./AuthContext";
import { supabase } from "../lib/supabase";

// Mock setup
vi.mock("../lib/supabase", () => ({
  supabase: {
    auth: {
      signOut: vi.fn(),
      getSession: vi.fn().mockResolvedValue({ data: { session: null } }),
      onAuthStateChange: vi.fn().mockImplementation((callback) => ({
        data: { subscription: { unsubscribe: vi.fn() } },
      })),
    },
  },
}));

describe("AuthContext - signOut", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should successfully sign out", async () => {
    supabase.auth.signOut.mockResolvedValueOnce({ error: null });

    let hook;

    await act(async () => {
      hook = renderHook(() => useAuth(), {
        wrapper: ({ children }) => <AuthProvider>{children}</AuthProvider>,
      });
    });

    const response = await act(async () => {
      return await hook.result.current.signOut();
    });

    expect(response.error).toBeNull();
    expect(supabase.auth.signOut).toHaveBeenCalled();
  });

  it("should handle sign out error", async () => {
    const mockError = new Error("Sign out failed");
    supabase.auth.signOut.mockRejectedValueOnce(mockError);

    let hook;

    await act(async () => {
      hook = renderHook(() => useAuth(), {
        wrapper: ({ children }) => <AuthProvider>{children}</AuthProvider>,
      });
    });

    const response = await act(async () => {
      return await hook.result.current.signOut();
    });

    expect(response.error).toBeTruthy();
    expect(response.error.message).toBe("Sign out failed");
  });
});
