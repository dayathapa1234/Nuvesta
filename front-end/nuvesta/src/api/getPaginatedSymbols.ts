import { clearAuth, getAuthHeader } from "@/lib/auth";

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

const API_BASE = import.meta.env.VITE_API_BASE ?? "";

export default async function getPaginatedSymbols(
  params: URLSearchParams
): Promise<PaginatedSymbolsResponse> {
  const res = await fetch(`${API_BASE}/api/paginatedSymbols?${params.toString()}`, {
    headers: getAuthHeader(),
  });
  if (!res.ok) {
    if (res.status === 401) clearAuth();
    throw new Error("Failed to fetch symbols");
  }

  const payload = (await res.json()) as unknown;
  if (!Array.isArray(payload)) {
    throw new Error("Unexpected symbol list response");
  }

  const data = payload as SymbolInfo[];
  const totalPages = Number(res.headers.get("X-Total-Pages") ?? "0");
  return { data, totalPages };
}
