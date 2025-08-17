import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createPortal } from "react-dom";
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
  latestPrice: number | null;
}

function useAnchorRect(open: boolean) {
  const ref = useRef<HTMLSpanElement | null>(null);
  const [rect, setRect] = useState<DOMRect | null>(null);

  useLayoutEffect(() => {
    if (!open) return;
    const update = () => {
      if (ref.current) setRect(ref.current.getBoundingClientRect());
    };
    update();
    window.addEventListener("scroll", update, true);
    window.addEventListener("resize", update);
    return () => {
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
      className="absolute z-50 max-h-60 overflow-auto rounded border bg-white p-2 shadow"
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
  const [symbols, setSymbols] = useState<SymbolInfo[]>([]);
  const [keyword, setKeyword] = useState("");
  const [exchangeFilter, setExchangeFilter] = useState<string[]>([]);
  const [assetTypeFilter, setAssetTypeFilter] = useState<string[]>([]);
  const [allExchanges, setAllExchanges] = useState<string[]>([]);
  const [allAssetTypes, setAllAssetTypes] = useState<string[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
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

  // preload filter lists
  useEffect(() => {
    fetch("/api/symbol-filters")
      .then((res) => res.json())
      .then((data) => {
        setAllExchanges(data.exchanges || []);
        setAllAssetTypes(data.assetTypes || []);
      })
      .catch(() => {
        setAllExchanges([]);
        setAllAssetTypes([]);
      });
  }, []);

  // data fetch
  useEffect(() => {
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
  }, [keyword, exchangeFilter, assetTypeFilter, page, sortField, sortAsc]);

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

  const sortIndicator = (field: keyof SymbolInfo) =>
    sortField === field ? (sortAsc ? " ↑" : " ↓") : "";

  // anchor refs for dropdowns
  const exOpen = openFilter === "exchange";
  const asOpen = openFilter === "asset";
  const { ref: exRef, rect: exRect } = useAnchorRect(exOpen);
  const { ref: asRef, rect: asRect } = useAnchorRect(asOpen);

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
              {`Symbol${sortIndicator("symbol")}`}
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
              {`Name${sortIndicator("name")}`}
            </TableHead>

            {/* Latest Price */}
            <TableHead>Latest Price</TableHead>

            {/* Exchange */}
            <TableHead>
              <div className="flex items-center">
                <span
                  className="cursor-pointer select-none"
                  onClick={() => {
                    if (sortField === "exchange") setSortAsc(!sortAsc);
                    else {
                      setSortField("exchange");
                      setSortAsc(true);
                    }
                    setPage(0);
                  }}
                >
                  {`Exchange${sortIndicator("exchange")}`}
                </span>
                <span
                  ref={exRef}
                  className="ml-1 cursor-pointer select-none"
                  onClick={(e) => {
                    e.stopPropagation();
                    setOpenFilter(exOpen ? null : "exchange");
                  }}
                >
                  ▼
                </span>
              </div>
            </TableHead>

            {/* Asset Type */}
            <TableHead>
              <div className="flex items-center">
                <span
                  className="cursor-pointer select-none"
                  onClick={() => {
                    if (sortField === "assetType") setSortAsc(!sortAsc);
                    else {
                      setSortField("assetType");
                      setSortAsc(true);
                    }
                    setPage(0);
                  }}
                >
                  {`Asset Type${sortIndicator("assetType")}`}
                </span>
                <span
                  ref={asRef}
                  className="ml-1 cursor-pointer select-none"
                  onClick={(e) => {
                    e.stopPropagation();
                    setOpenFilter(asOpen ? null : "asset");
                  }}
                >
                  ▼
                </span>
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
              {`IPO Date${sortIndicator("ipoDate")}`}
            </TableHead>
          </TableRow>
        </TableHeader>

        <TableBody>
          {symbols.map((s) => (
            <TableRow
              key={s.symbol}
              className="cursor-pointer"
              onClick={() => navigate(`/symbol/${s.symbol}`)}
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
    </div>
  );
}
