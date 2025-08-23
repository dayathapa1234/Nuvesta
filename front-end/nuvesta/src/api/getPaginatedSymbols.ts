export interface SymbolInfo {
  symbol: string;
  name: string;
  exchange: string;
  assetType: string;
  ipoDate: string;
  delistingDate: string;
  status: string;
  latestPrice: number | null;
}

export interface PaginatedSymbolsResponse {
  data: SymbolInfo[];
  totalPages: number;
}

export default async function getPaginatedSymbols(
  params: URLSearchParams
): Promise<PaginatedSymbolsResponse> {
  const res = await fetch(`/api/paginatedSymbols?${params.toString()}`);
  const data = (await res.json()) as SymbolInfo[];
  const totalPages = Number(res.headers.get("X-Total-Pages") ?? "0");
  return { data, totalPages };
}
