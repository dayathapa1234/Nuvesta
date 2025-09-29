export interface SymbolFilters {
  exchanges: string[];
  assetTypes: string[];
}

const API_BASE = import.meta.env.VITE_API_BASE ?? "";

export default async function getSymbolFilters(): Promise<SymbolFilters> {
  const res = await fetch(`${API_BASE}/api/symbolFilters`);
  return (await res.json()) as SymbolFilters;
}
