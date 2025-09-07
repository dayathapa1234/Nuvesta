import { CartesianGrid, Line, LineChart, XAxis } from "recharts";
import { useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { useParams } from "@tanstack/react-router";
import LoadingScreen from "./LoadingScreen";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from "./ui/chart";
import getPrices, { type PricePoint } from "../api/getPrices";

interface ChartPoint extends PricePoint {
  dateLabel: string;
}

function downsampleToMax<T>(data: T[], maxPoints: number): T[] {
  if (data.length <= maxPoints) return data;
  const step = Math.ceil(data.length / maxPoints);
  return data.filter((_, i) => i % step === 0);
}

const chartConfig = {
  price: {
    label: "Price",
    color: "hsl(var(--chart-1))",
  },
} satisfies ChartConfig;

export default function PriceChart() {
  const { symbol } = useParams({ from: "/symbol/$symbol" });
  const { data: points = [], isLoading } = useQuery({
    queryKey: ["prices", symbol],
    queryFn: () => getPrices(symbol),
    staleTime: 5 * 60 * 1000,
    select: (points: PricePoint[]): ChartPoint[] =>
      points.map((p) => ({
        ...p,
        dateLabel: new Date(p.time).toLocaleDateString(),
      })),
  });

  const data = useMemo(() => downsampleToMax(points, 500), [points]);

  return (
      <Card className="mx-auto w-full max-w-5xl">
        <CardHeader>
          <CardTitle className="text-2xl font-bold">{symbol}</CardTitle>
        </CardHeader>
        <CardContent>
          <LoadingScreen show={isLoading} />
          <ChartContainer config={chartConfig} className="h-64 w-full">
            <LineChart
              accessibilityLayer
              data={data}
              margin={{ left: 12, right: 12 }}
            >
              <CartesianGrid vertical={false} />
              <XAxis
                dataKey="dateLabel"
                tickLine={false}
                axisLine={false}
                tickMargin={8}
              />
              <ChartTooltip
                cursor={false}
                content={
                  <ChartTooltipContent
                    labelFormatter={(_, payload) =>
                      payload?.[0]?.payload?.dateLabel ?? ""
                    }
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
        </CardContent>
      </Card>
  );
}
