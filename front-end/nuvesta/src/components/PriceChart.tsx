import { CartesianGrid, Line, LineChart, XAxis } from "recharts";
import { useQuery } from "@tanstack/react-query";
import LoadingScreen from "./LoadingScreen";␊
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
  const formatDate = (value: number) => new Date(value).toLocaleDateString();

  const { data = [], isLoading } = useQuery({
    queryKey: ["prices", symbol],
    queryFn: async () => {
      const res = await fetch(`/api/prices?symbol=${symbol}`);
      return (await res.json()) as PricePoint[];
    },
    staleTime: 30000,
  });

  return (
    <>
      <LoadingScreen show={isLoading} />
      <ChartContainer config={chartConfig} className="h-64 w-full">
        <LineChart accessibilityLayer data={data} margin={{ left: 12, right: 12 }}>
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
