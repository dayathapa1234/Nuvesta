import { useEffect, useState } from "react";
import { CartesianGrid, Line, LineChart, XAxis } from "recharts";
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

  useEffect(() => {
    fetch(`/api/prices?symbol=${symbol}`)
      .then((res) => res.json())
      .then((d: PricePoint[]) => setData(d))
      .catch(() => setData([]));
  }, [symbol]);

  return (
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
        />
        <ChartTooltip
          cursor={false}
          content={<ChartTooltipContent hideLabel />}
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
  );
}
