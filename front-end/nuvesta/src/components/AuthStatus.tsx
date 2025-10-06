import { useEffect, useState } from "react";
import { Link } from "@tanstack/react-router";

import { Button } from "@/components/ui/button";
import {
  clearAuth,
  loadAuth,
  onAuthChange,
  type AuthSession,
} from "@/lib/auth";

export function AuthStatus() {
  const [session, setSession] = useState<AuthSession | null>(() => {
    if (typeof window === "undefined") {
      return null;
    }
    return loadAuth();
  });

  useEffect(() => {
    if (typeof window === "undefined") {
      return;
    }
    setSession(loadAuth());
    return onAuthChange((next) => {
      setSession(next);
    });
  }, []);

  if (!session) {
    return (
      <div className="flex items-center gap-2">
        <Button
          asChild
          size="sm"
          variant="outline"
          className="border-white/40 bg-white/10 text-white hover:bg-white/20"
        >
          <Link to="/login">Sign in</Link>
        </Button>
        <Button
          asChild
          size="sm"
          className="bg-white/80 text-slate-900 hover:bg-white"
        >
          <Link to="/register">Get started</Link>
        </Button>
      </div>
    );
  }

  const primaryName =
    session.user.fullName.trim() !== ""
      ? session.user.fullName
      : session.user.email;
  const firstName = primaryName.split(" ")[0] ?? primaryName;

  return (
    <div className="flex items-center gap-3 rounded-full border border-white/30 bg-white/10 px-4 py-2 text-white shadow-lg backdrop-blur">
      <span className="text-sm font-medium">Hi, {firstName}</span>
      <Button
        size="sm"
        variant="ghost"
        className="text-white hover:bg-white/20 hover:text-white"
        onClick={() => clearAuth()}
      >
        Sign out
      </Button>
    </div>
  );
}
