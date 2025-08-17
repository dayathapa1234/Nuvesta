import { useEffect, useState } from "react";
import { CartesianGrid, Line, LineChart, XAxis } from "recharts";
import LoadingScreen from "./LoadingScreen";
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from "./ui/chart";

interface PricePoint {
  time: number;
  price: number;
}

const chartConfig = {
  price: {
    label: "Price",
    color: "hsl(var(--chart-1))",
  },
} satisfies ChartConfig;

export default function PriceChart({ symbol }: { symbol: string }) {
  console.log("PriceChart", symbol);
  const [data, setData] = useState<PricePoint[]>([]);
  const [loading, setLoading] = useState(false);

  const formatDate = (value: number) => new Date(value).toLocaleDateString();

  useEffect(() => {
    setLoading(true);
    fetch(`/api/prices?symbol=${symbol}`)
      .then((res) => res.json())
      .then((d: PricePoint[]) => setData(d))
      .catch(() => setData([]))
      .finally(() => setLoading(false));
  }, [symbol]);

  return (
    <>
      <LoadingScreen show={loading} />
      <ChartContainer config={chartConfig} className="h-64 w-full">
        <LineChart
          accessibilityLayer
          data={data}
          margin={{ left: 12, right: 12 }}
        >
          <CartesianGrid vertical={false} />
          <XAxis
            dataKey="time"
            tickLine={false}
            axisLine={false}
            tickMargin={8}
            tickFormatter={formatDate}
          />
          <ChartTooltip
            cursor={false}
            content={
              <ChartTooltipContent
                labelFormatter={(_, payload) => {
                  const date = (payload?.[0]?.payload as PricePoint)?.time;
                  return date ? formatDate(date) : "";
                }}
              />
            }
          />
          <Line
            dataKey="price"
            type="natural"
            stroke="var(--color-price)"
            strokeWidth={2}
            dot={false}
          />
        </LineChart>
      </ChartContainer>
    </>
  );
}
