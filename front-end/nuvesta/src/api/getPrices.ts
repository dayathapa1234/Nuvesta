export interface PricePoint {
  time: number;
  price: number;
}

const API_BASE = (import.meta as any).env?.VITE_API_BASE || "";

export default async function getPrices(symbol: string): Promise<PricePoint[]> {
  const res = await fetch(`${API_BASE}/api/prices?symbol=${encodeURIComponent(symbol)}`);
  return (await res.json()) as PricePoint[];
}
