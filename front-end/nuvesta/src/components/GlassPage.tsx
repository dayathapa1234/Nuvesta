import React from "react";
import ModeToggle from "./ModeToggle";
import { AuthStatus } from "./AuthStatus";

interface GlassPageProps {
  backgroundUrl: string;
  children: React.ReactNode;
}

export function GlassPage({ backgroundUrl, children }: GlassPageProps) {
  return (
    <div className="relative min-h-screen w-full overflow-hidden">
      <img
        src={backgroundUrl}
        alt="Background"
        className="absolute inset-0 h-full w-full object-cover"
      />
      <div className="absolute inset-0 bg-black/30" />
      <div className="absolute top-4 right-4 z-20 flex flex-col items-end gap-3 sm:flex-row sm:items-center">
        <AuthStatus />
        <ModeToggle />
      </div>
      <div className="relative z-10 h-full p-6 pt-24 sm:pt-28 md:p-12 md:pt-32">
        {children}
      </div>
    </div>
  );
}
