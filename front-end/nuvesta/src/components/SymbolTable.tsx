import { useEffect, useState } from "react";
import { Input } from "./ui/input";
import { Button } from "./ui/button";
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
} from "./ui/table";

interface SymbolInfo {
  symbol: string;
  name: string;
  exchange: string;
  assetType: string;
  ipoDate: string;
  delistingDate: string;
  status: string;
}

export default function SymbolTable() {
  const [symbols, setSymbols] = useState<SymbolInfo[]>([]);
  const [keyword, setKeyword] = useState("");
  const [exchangeFilter, setExchangeFilter] = useState<string[]>([]);
  const [assetTypeFilter, setAssetTypeFilter] = useState<string[]>([]);
  const [ipoDateFilter, setIpoDateFilter] = useState<string[]>([]);
  const [allExchanges, setAllExchanges] = useState<string[]>([]);
  const [allAssetTypes, setAllAssetTypes] = useState<string[]>([]);
  const [allIpoDates, setAllIpoDates] = useState<string[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [openFilter, setOpenFilter] = useState<string | null>(null);
  const [sortField, setSortField] = useState<keyof SymbolInfo | null>(null);
  const [sortAsc, setSortAsc] = useState(true);

  useEffect(() => {
    const handler = () => setOpenFilter(null);
    document.addEventListener("click", handler);
    return () => document.removeEventListener("click", handler);
  }, []);

  useEffect(() => {
    fetch("/api/symbol-filters")
      .then((res) => res.json())
      .then((data) => {
        setAllExchanges(data.exchanges || []);
        setAllAssetTypes(data.assetTypes || []);
        setAllIpoDates(data.ipoDates || []);
      })
      .catch(() => {
        setAllExchanges([]);
        setAllAssetTypes([]);
        setAllIpoDates([]);
      });
  }, []);

  useEffect(() => {
    const params = new URLSearchParams();
    if (keyword) params.append("keyword", keyword);
    exchangeFilter.forEach((e) => params.append("exchange", e));
    assetTypeFilter.forEach((a) => params.append("asset_type", a));
    ipoDateFilter.forEach((d) => params.append("ipo_date", d));
    if (sortField) {
      params.append("sortBy", sortField);
      params.append("order", sortAsc ? "asc" : "desc");
    }
    params.append("page", page.toString());
    params.append("size", "30");

    fetch(`/api/paginatedSymbols?${params.toString()}`)
      .then(async (res) => {
        const data: SymbolInfo[] = await res.json();
        const tp = Number(res.headers.get("X-Total-Pages") ?? "0");
        setTotalPages(tp);
        setSymbols(data);
      })
      .catch(() => {
        setSymbols([]);
        setTotalPages(0);
      });
  }, [
    keyword,
    exchangeFilter,
    assetTypeFilter,
    ipoDateFilter,
    page,
    sortField,
    sortAsc,
  ]);

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

  const sortIndicator = (field: keyof SymbolInfo) => {
    if (sortField !== field) return null;
    return sortAsc ? " ↑" : " ↓";
  };

  return (
    <div className="space-y-4">
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
            <TableHead
              className="cursor-pointer"
              onClick={() => {
                if (sortField === "symbol") {
                  setSortAsc(!sortAsc);
                } else {
                  setSortField("symbol");
                  setSortAsc(true);
                }
                setPage(0);
              }}
            >
              {`Symbol${sortIndicator("symbol") ?? ""}`}
            </TableHead>
            <TableHead
              className="cursor-pointer"
              onClick={() => {
                if (sortField === "name") {
                  setSortAsc(!sortAsc);
                } else {
                  setSortField("name");
                  setSortAsc(true);
                }
                setPage(0);
              }}
            >
              {`Name${sortIndicator("name") ?? ""}`}
            </TableHead>
            <TableHead className="relative">
              <div className="flex items-center">
                <span
                  className="cursor-pointer select-none"
                  onClick={() => {
                    if (sortField === "exchange") {
                      setSortAsc(!sortAsc);
                    } else {
                      setSortField("exchange");
                      setSortAsc(true);
                    }
                    setPage(0);
                  }}
                >
                  {`Exchange${sortIndicator("exchange") ?? ""}`}
                </span>
                <span
                  className="ml-1 cursor-pointer select-none"
                  onClick={(e) => {
                    e.stopPropagation();
                    setOpenFilter(
                      openFilter === "exchange" ? null : "exchange"
                    );
                  }}
                >
                  ▼
                </span>
              </div>
              {openFilter === "exchange" && (
                <div
                  className="absolute left-0 mt-2 w-40 max-h-60 overflow-auto rounded border bg-white p-2 shadow z-10"
                  onClick={(e) => e.stopPropagation()}
                >
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
                </div>
              )}
            </TableHead>
            <TableHead className="relative">
              <div className="flex items-center">
                <span
                  className="cursor-pointer select-none"
                  onClick={() => {
                    if (sortField === "assetType") {
                      setSortAsc(!sortAsc);
                    } else {
                      setSortField("assetType");
                      setSortAsc(true);
                    }
                    setPage(0);
                  }}
                >
                  {`Asset Type${sortIndicator("assetType") ?? ""}`}
                </span>
                <span
                  className="ml-1 cursor-pointer select-none"
                  onClick={(e) => {
                    e.stopPropagation();
                    setOpenFilter(openFilter === "asset" ? null : "asset");
                  }}
                >
                  ▼
                </span>
              </div>
              {openFilter === "asset" && (
                <div
                  className="absolute left-0 mt-2 w-40 max-h-60 overflow-auto rounded border bg-white p-2 shadow z-10"
                  onClick={(e) => e.stopPropagation()}
                >
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
                </div>
              )}
            </TableHead>
            <TableHead className="relative">
              <div className="flex items-center">
                <span
                  className="cursor-pointer select-none"
                  onClick={() => {
                    if (sortField === "ipoDate") {
                      setSortAsc(!sortAsc);
                    } else {
                      setSortField("ipoDate");
                      setSortAsc(true);
                    }
                    setPage(0);
                  }}
                >
                  {`IPO Date${sortIndicator("ipoDate") ?? ""}`}
                </span>
                <span
                  className="ml-1 cursor-pointer select-none"
                  onClick={(e) => {
                    e.stopPropagation();
                    setOpenFilter(openFilter === "ipo" ? null : "ipo");
                  }}
                ></span>
              </div>
              {openFilter === "ipo" && (
                <div
                  className="absolute left-0 mt-2 w-40 max-h-60 overflow-auto rounded border bg-white p-2 shadow z-10"
                  onClick={(e) => e.stopPropagation()}
                >
                  {allIpoDates.map((d) => (
                    <label key={d} className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        checked={ipoDateFilter.includes(d)}
                        onChange={() =>
                          toggleValue(d, ipoDateFilter, setIpoDateFilter)
                        }
                      />
                      <span>{d}</span>
                    </label>
                  ))}
                </div>
              )}
            </TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {symbols.map((s) => (
            <TableRow key={s.symbol}>
              <TableCell>{s.symbol}</TableCell>
              <TableCell>{s.name}</TableCell>
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
  );
}
