export interface PricePoint {
  time: number;
  price: number;
}

export default async function getPrices(symbol: string): Promise<PricePoint[]> {
  const res = await fetch(`/api/prices?symbol=${symbol}`);
  return (await res.json()) as PricePoint[];
}
