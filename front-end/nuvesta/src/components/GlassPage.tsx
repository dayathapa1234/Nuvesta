import React from "react";
import ModeToggle from "./ModeToggle";

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
      <div className="absolute top-4 right-4 z-20">
        <ModeToggle />
      </div>
      <div className="relative z-10 h-full p-6 md:p-12">{children}</div>
    </div>
  );
}
