export interface SymbolFilters {
  exchanges: string[];
  assetTypes: string[];
}

export default async function getSymbolFilters(): Promise<SymbolFilters> {
  const res = await fetch("/api/symbol-filters");
  return res.json() as Promise<SymbolFilters>;
}
