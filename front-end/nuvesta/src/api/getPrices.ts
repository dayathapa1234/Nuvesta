import { clearAuth, getAuthHeader } from "@/lib/auth";

export interface PricePoint {
  time: number;
  price: number;
}

const API_BASE = import.meta.env.VITE_API_BASE ?? "";

export default async function getPrices(symbol: string): Promise<PricePoint[]> {
  const res = await fetch(`${API_BASE}/api/prices?symbol=${encodeURIComponent(symbol)}`, {
    headers: getAuthHeader(),
  });
  if (!res.ok) {
    if (res.status === 401) clearAuth();
    throw new Error("Failed to fetch prices");
  }

  const payload = (await res.json()) as unknown;
  if (!Array.isArray(payload)) {
    throw new Error("Unexpected price response");
  }

  return payload as PricePoint[];
}
