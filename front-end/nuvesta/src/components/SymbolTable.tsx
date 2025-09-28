import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { useNavigate } from "@tanstack/react-router";
import { createPortal } from "react-dom";
import { useQuery, keepPreviousData } from "@tanstack/react-query";
import { Input } from "./ui/input";
import { ChevronDown, ChevronUp } from "lucide-react";
import { Button } from "./ui/button";
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
} from "./ui/table";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import LoadingScreen from "./LoadingScreen";
import getSymbolFilters, { type SymbolFilters } from "../api/getSymbolFilters";
import getPaginatedSymbols, {
  type SymbolInfo,
  type PaginatedSymbolsResponse,
} from "../api/getPaginatedSymbols";

function useAnchorRect(open: boolean) {
  const ref = useRef<HTMLButtonElement | null>(null);
  const [rect, setRect] = useState<DOMRect | null>(null);

  useLayoutEffect(() => {
    if (!open) return;
    let rafId: number | null = null;
    const update = () => {
      if (rafId) cancelAnimationFrame(rafId);
      rafId = requestAnimationFrame(() => {
        if (ref.current) {
          setRect(ref.current.getBoundingClientRect());
        }
      });
    };
    update();
    window.addEventListener("scroll", update, true);
    window.addEventListener("resize", update);
    return () => {
      if (rafId) cancelAnimationFrame(rafId);
      window.removeEventListener("scroll", update, true);
      window.removeEventListener("resize", update);
    };
  }, [open]);
  return { ref, rect };
}

function PortalDropdown({
  anchorRect,
  width = 160,
  children,
}: {
  anchorRect: DOMRect | null;
  width?: number;
  children: React.ReactNode;
}) {
  if (!anchorRect) return null;

  const top = anchorRect.bottom + window.scrollY;
  const left = anchorRect.left + window.scrollX;

  return createPortal(
    <div
      className="absolute z-50 max-h-60 overflow-auto rounded border border-[var(--glass-popover-border)] bg-[var(--glass-popover-bg)] p-2 text-foreground shadow backdrop-blur-md"
      style={{ top, left, width }}
      onClick={(e) => e.stopPropagation()}
    >
      {children}
    </div>,
    document.body
  );
}

export default function SymbolTable() {
  const navigate = useNavigate();
  const [keyword, setKeyword] = useState("");
  const [exchangeFilter, setExchangeFilter] = useState<string[]>([]);
  const [assetTypeFilter, setAssetTypeFilter] = useState<string[]>([]);
  const [page, setPage] = useState(0);
  const [openFilter, setOpenFilter] = useState<"exchange" | "asset" | null>(
    null
  );
  const [sortField, setSortField] = useState<keyof SymbolInfo | null>(null);
  const [sortAsc, setSortAsc] = useState(true);

  useEffect(() => {
    const handler = () => setOpenFilter(null);
    document.addEventListener("click", handler);
    return () => document.removeEventListener("click", handler);
  }, []);

  const { data: filterData } = useQuery<SymbolFilters>({
    queryKey: ["symbol-filters"],
    queryFn: getSymbolFilters,
    staleTime: 5 * 60 * 1000,
  });

  const allExchanges = filterData?.exchanges || [];
  const allAssetTypes = filterData?.assetTypes || [];

  const symbolsQuery = useQuery<PaginatedSymbolsResponse>({
    queryKey: [
      "symbols",
      keyword,
      exchangeFilter,
      assetTypeFilter,
      page,
      sortField,
      sortAsc,
    ],
    queryFn: () => {
      const params = new URLSearchParams();
      if (keyword) params.append("keyword", keyword);
      exchangeFilter.forEach((e) => params.append("exchange", e));
      assetTypeFilter.forEach((a) => params.append("asset_type", a));
      if (sortField) {
        params.append("sortBy", sortField);
        params.append("order", sortAsc ? "asc" : "desc");
      }
      params.append("page", page.toString());
      params.append("size", "30");
      return getPaginatedSymbols(params);
    },
    placeholderData: keepPreviousData,
    staleTime: 5 * 60 * 1000,
  });

  const symbols = symbolsQuery.data?.data || [];
  const totalPages = symbolsQuery.data?.totalPages || 0;
  const loading = symbolsQuery.isFetching;

  const toggleValue = (
    value: string,
    state: string[],
    setter: (v: string[]) => void
  ) => {
    if (state.includes(value)) {
      setter(state.filter((v) => v !== value));
    } else {
      setter([...state, value]);
    }
    setPage(0);
  };

  const sortIndicator = (field: keyof SymbolInfo): JSX.Element | null => {
    if (sortField !== field) return null;
    return sortAsc ? (
      <ChevronUp className="h-3.5 w-3.5" aria-label="sorted ascending" />
    ) : (
      <ChevronDown className="h-3.5 w-3.5" aria-label="sorted descending" />
    );
  };

  // anchor refs for dropdowns
  const exOpen = openFilter === "exchange";
  const asOpen = openFilter === "asset";
  const { ref: exRef, rect: exRect } = useAnchorRect(exOpen);
  const { ref: asRef, rect: asRect } = useAnchorRect(asOpen);

  return (
    <>
      <Card className="mx-auto w-full max-w-6xl">
        <CardHeader>
          <CardTitle className="text-2xl font-bold mb-4">Symbols</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <LoadingScreen show={loading} />
            <Input
              placeholder="Search by symbol or name"
              value={keyword}
              onChange={(e) => {
                setKeyword(e.target.value);
                setPage(0);
              }}
            />
            <Table>
              <TableHeader>
                <TableRow>
                  {/* Symbol */}
                  <TableHead
                    className="cursor-pointer"
                    onClick={() => {
                      if (sortField === "symbol") setSortAsc(!sortAsc);
                      else {
                        setSortField("symbol");
                        setSortAsc(true);
                      }
                      setPage(0);
                    }}
                  >
                    <span className="inline-flex items-center gap-1">
                      Symbol
                      {sortIndicator("symbol")}
                    </span>
                  </TableHead>

                  {/* Name */}
                  <TableHead
                    className="cursor-pointer"
                    onClick={() => {
                      if (sortField === "name") setSortAsc(!sortAsc);
                      else {
                        setSortField("name");
                        setSortAsc(true);
                      }
                      setPage(0);
                    }}
                  >
                    <span className="inline-flex items-center gap-1">
                      Name
                      {sortIndicator("name")}
                    </span>
                  </TableHead>

                  {/* Latest Price */}
                  <TableHead>Latest Price</TableHead>

                  {/* Exchange */}
                  <TableHead>
                    <div className="flex items-center">
                      <span
                        className="inline-flex items-center gap-1 cursor-pointer select-none"
                        onClick={() => {
                          if (sortField === "exchange") setSortAsc(!sortAsc);
                          else {
                            setSortField("exchange");
                            setSortAsc(true);
                          }
                          setPage(0);
                        }}
                      >
                        Exchange
                        {sortIndicator("exchange")}
                      </span>
                      <button
                        ref={exRef}
                        type="button"
                        className="ml-1 flex h-5 w-5 items-center justify-center rounded border border-transparent bg-transparent text-foreground/70 transition hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
                        onClick={(e) => {
                          e.stopPropagation();
                          setOpenFilter(exOpen ? null : "exchange");
                        }}
                        aria-label="Toggle exchange filter"
                      >
                        <ChevronDown className="h-4 w-4" aria-hidden="true" />
                      </button>
                    </div>
                  </TableHead>

                  {/* Asset Type */}
                  <TableHead>
                    <div className="flex items-center">
                      <span
                        className="inline-flex items-center gap-1 cursor-pointer select-none"
                        onClick={() => {
                          if (sortField === "assetType") setSortAsc(!sortAsc);
                          else {
                            setSortField("assetType");
                            setSortAsc(true);
                          }
                          setPage(0);
                        }}
                      >
                        Asset Type
                        {sortIndicator("assetType")}
                      </span>
                      <button
                        ref={asRef}
                        type="button"
                        className="ml-1 flex h-5 w-5 items-center justify-center rounded border border-transparent bg-transparent text-foreground/70 transition hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
                        onClick={(e) => {
                          e.stopPropagation();
                          setOpenFilter(asOpen ? null : "asset");
                        }}
                        aria-label="Toggle asset type filter"
                      >
                        <ChevronDown className="h-4 w-4" aria-hidden="true" />
                      </button>
                    </div>
                  </TableHead>
                  {/* IPO Date - Sort only */}
                  <TableHead
                    className="cursor-pointer"
                    onClick={() => {
                      if (sortField === "ipoDate") setSortAsc(!sortAsc);
                      else {
                        setSortField("ipoDate");
                        setSortAsc(true);
                      }
                      setPage(0);
                    }}
                  >
                    <span className="inline-flex items-center gap-1">
                      IPO Date
                      {sortIndicator("ipoDate")}
                    </span>
                  </TableHead>
                </TableRow>
              </TableHeader>

              <TableBody>
                {symbols.map((s) => (
                  <TableRow
                    key={s.symbol}
                    className="cursor-pointer"
                    onClick={() =>
                      navigate({
                        to: "/symbol/$symbol",
                        params: { symbol: s.symbol },
                      })
                    }
                  >
                    <TableCell>{s.symbol}</TableCell>
                    <TableCell>{s.name}</TableCell>
                    <TableCell>{s.latestPrice ?? "-"}</TableCell>
                    <TableCell>{s.exchange}</TableCell>
                    <TableCell>{s.assetType}</TableCell>
                    <TableCell>{s.ipoDate}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>

            <div className="flex items-center justify-end space-x-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.max(p - 1, 0))}
                disabled={page === 0}
              >
                Previous
              </Button>
              <span className="text-sm">
                Page {page + 1} of {totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.min(p + 1, totalPages - 1))}
                disabled={page + 1 >= totalPages}
              >
                Next
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Exchange filter dropdown */}
      {exOpen && (
        <PortalDropdown anchorRect={exRect}>
          {allExchanges.map((e) => (
            <label key={e} className="flex items-center space-x-2">
              <input
                type="checkbox"
                checked={exchangeFilter.includes(e)}
                onChange={() =>
                  toggleValue(e, exchangeFilter, setExchangeFilter)
                }
              />
              <span>{e}</span>
            </label>
          ))}
        </PortalDropdown>
      )}

      {/* Asset Type filter dropdown */}
      {asOpen && (
        <PortalDropdown anchorRect={asRect}>
          {allAssetTypes.map((a) => (
            <label key={a} className="flex items-center space-x-2">
              <input
                type="checkbox"
                checked={assetTypeFilter.includes(a)}
                onChange={() =>
                  toggleValue(a, assetTypeFilter, setAssetTypeFilter)
                }
              />
              <span>{a}</span>
            </label>
          ))}
        </PortalDropdown>
      )}
    </>
  );
}











