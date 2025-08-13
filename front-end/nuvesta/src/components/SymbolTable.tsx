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
  const [symbol, setSymbol] = useState("");
  const [name, setName] = useState("");
  const [exchange, setExchange] = useState("");
  const [assetType, setAssetType] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    const params = new URLSearchParams();
    if (symbol) params.append("symbol", symbol);
    if (name) params.append("name", name);
    if (exchange) params.append("exchange", exchange);
    if (assetType) params.append("asset_type", assetType);
    if (status) params.append("status", status);
    params.append("page", page.toString());
    params.append("size", "10");

    fetch(`http://localhost:8080/api/paginatedSymbols?${params.toString()}`)
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
  }, [symbol, name, exchange, assetType, status, page]);

  return (
    <div className="space-y-4">
      <div className="grid gap-2 md:grid-cols-5">
        <Input
          placeholder="Symbol"
          value={symbol}
          onChange={(e) => {
            setSymbol(e.target.value);
            setPage(0);
          }}
        />
        <Input
          placeholder="Name"
          value={name}
          onChange={(e) => {
            setName(e.target.value);
            setPage(0);
          }}
        />
        <Input
          placeholder="Exchange"
          value={exchange}
          onChange={(e) => {
            setExchange(e.target.value);
            setPage(0);
          }}
        />
        <Input
          placeholder="Asset Type"
          value={assetType}
          onChange={(e) => {
            setAssetType(e.target.value);
            setPage(0);
          }}
        />
        <Input
          placeholder="Status"
          value={status}
          onChange={(e) => {
            setStatus(e.target.value);
            setPage(0);
          }}
        />
      </div>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Symbol</TableHead>
            <TableHead>Name</TableHead>
            <TableHead>Exchange</TableHead>
            <TableHead>Asset Type</TableHead>
            <TableHead>IPO Date</TableHead>
            <TableHead>Delisting Date</TableHead>
            <TableHead>Status</TableHead>
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
              <TableCell>{s.delistingDate}</TableCell>
              <TableCell>{s.status}</TableCell>
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
