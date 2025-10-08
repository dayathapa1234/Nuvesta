import { clearAuth, getAuthHeader } from "@/lib/auth";

export interface SymbolFilters {
  exchanges: string[];
  assetTypes: string[];
}

const API_BASE = import.meta.env.VITE_API_BASE ?? "";

export default async function getSymbolFilters(): Promise<SymbolFilters> {
  const res = await fetch(`${API_BASE}/api/symbol-filters`, {
    method: "GET",
    headers: {
      Accept: "application/json",
      ...getAuthHeader(),
    },
  });
  if (!res.ok) {
    if (res.status === 401) clearAuth();
    throw new Error("Failed to fetch symbol filters");
  }

  const payload = (await res.json()) as unknown;
  if (typeof payload !== "object" || payload === null) {
    throw new Error("Unexpected symbol filter response");
  }

  const { exchanges, assetTypes } = payload as Record<string, unknown>;

  return {
    exchanges: Array.isArray(exchanges) ? exchanges.map(String) : [],
    assetTypes: Array.isArray(assetTypes) ? assetTypes.map(String) : [],
  };
}
