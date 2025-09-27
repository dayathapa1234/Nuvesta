import type { FormEvent } from "react";
import { useEffect, useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useParams } from "@tanstack/react-router";

import getPrices, { type PricePoint } from "../api/getPrices";
import LoadingScreen from "./LoadingScreen";
import { Button } from "./ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "./ui/card";
import { Input } from "./ui/input";

function toDateInputValue(time: number): string {
  return new Date(time).toISOString().slice(0, 10);
}

interface MatchedPrice {
  point: PricePoint;
  exactMatch: boolean;
}

export default function BackdatedPurchaseForm() {
  const { symbol } = useParams({ from: "/symbol/$symbol" });
  const { data: rawPoints = [], isLoading } = useQuery<PricePoint[]>({
    queryKey: ["prices", symbol],
    queryFn: () => getPrices(symbol),
    staleTime: 5 * 60 * 1000,
  });

  const points = useMemo(
    () => [...rawPoints].sort((a, b) => a.time - b.time),
    [rawPoints]
  );

  const [date, setDate] = useState<string>("");
  const [shares, setShares] = useState<string>("1");
  const [confirmation, setConfirmation] = useState<string | null>(null);

  const earliestDate = points.length ? toDateInputValue(points[0].time) : "";
  const latestDate = points.length
    ? toDateInputValue(points[points.length - 1].time)
    : "";

  useEffect(() => {
    if (!date && latestDate) {
      setDate(latestDate);
    }
  }, [date, latestDate]);

  const match: MatchedPrice | null = useMemo(() => {
    if (!date || !points.length) return null;
    const targetTime = new Date(`${date}T00:00:00Z`).getTime();
    if (Number.isNaN(targetTime)) return null;

    for (let i = points.length - 1; i >= 0; i -= 1) {
      const candidate = points[i];
      if (candidate.time > targetTime) {
        continue;
      }
      const exactMatch = toDateInputValue(candidate.time) === date;
      return { point: candidate, exactMatch };
    }
    return null;
  }, [date, points]);

  const shareCount = parseFloat(shares);
  const canQuote =
    Boolean(match) && Number.isFinite(shareCount) && shareCount > 0;
  const closingPrice = match?.point.price ?? null;
  const totalCost =
    canQuote && closingPrice !== null ? shareCount * closingPrice : null;
  const settlementDate = match ? toDateInputValue(match.point.time) : null;

  const priceFormatter = useMemo(
    () =>
      new Intl.NumberFormat(undefined, {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      }),
    []
  );

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (
      !canQuote ||
      !settlementDate ||
      closingPrice === null ||
      totalCost === null
    ) {
      return;
    }
    const contextDate =
      settlementDate === date
        ? settlementDate
        : `${settlementDate} (last trading day before ${date})`;
    setConfirmation(
      `Mock purchase saved: ${shareCount.toFixed(4)} shares on ${contextDate} for ${priceFormatter.format(totalCost)} total.`
    );
  };

  return (
    <Card className="mx-auto w-full max-w-5xl">
      <CardHeader>
        <CardTitle className="text-xl font-semibold">
          Backdated Purchase
        </CardTitle>
        <CardDescription>
          Pick a past trading day and simulate how many shares you could have
          bought.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <LoadingScreen show={isLoading} />
        <form className="space-y-6" onSubmit={handleSubmit}>
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <label htmlFor="purchase-date" className="text-sm font-medium">
                Purchase date
              </label>
              <Input
                id="purchase-date"
                type="date"
                required
                value={date}
                min={earliestDate}
                max={latestDate || undefined}
                onChange={(event) => {
                  setDate(event.target.value);
                  setConfirmation(null);
                }}
              />
            </div>
            <div className="space-y-2">
              <label htmlFor="purchase-shares" className="text-sm font-medium">
                Shares to buy (decimals allowed)
              </label>
              <Input
                id="purchase-shares"
                type="number"
                min="0"
                step="0.0001"
                inputMode="decimal"
                placeholder="1.5"
                value={shares}
                onChange={(event) => {
                  setShares(event.target.value);
                  setConfirmation(null);
                }}
              />
            </div>
          </div>

          <div className="space-y-2">
            {match ? (
              <div className="space-y-1 rounded-md border border-dashed border-muted-foreground/40 p-4">
                <p className="text-sm">
                  <span className="font-medium">Closing price:</span>{" "}
                  {priceFormatter.format(match.point.price)}
                </p>
                {totalCost !== null && (
                  <p className="text-sm">
                    <span className="font-medium">Total cost:</span>{" "}
                    {priceFormatter.format(totalCost)}
                  </p>
                )}
                {!match.exactMatch && settlementDate && (
                  <p className="text-xs text-muted-foreground">
                    No data for {date}; using {settlementDate}, the most recent
                    trading day.
                  </p>
                )}
              </div>
            ) : (
              <p className="rounded-md border border-dashed border-destructive/50 p-4 text-sm text-destructive">
                No historical price data is available for the selected date yet.
              </p>
            )}
          </div>

          <Button type="submit" disabled={!canQuote}>
            Simulate purchase
          </Button>
        </form>

        {confirmation && (
          <p className="mt-4 rounded-md border border-emerald-500/60 bg-emerald-500/10 p-4 text-sm text-emerald-900 dark:text-emerald-200">
            {confirmation}
          </p>
        )}
      </CardContent>
    </Card>
  );
}
