import { Loader2 } from "lucide-react";

export default function LoadingScreen({ show }: { show: boolean }) {
  if (!show) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/70 backdrop-blur-sm">
      <Loader2 className="h-10 w-10 animate-spin" />
    </div>
  );
}
