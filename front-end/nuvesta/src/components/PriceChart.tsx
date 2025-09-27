import { CartesianGrid, Line, LineChart, XAxis } from "recharts";
import { useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { useParams } from "@tanstack/react-router";
import LoadingScreen from "./LoadingScreen";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "./ui/card";
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
  const { data: rawPoints = [], isLoading } = useQuery<PricePoint[]>({
    queryKey: ["prices", symbol],
    queryFn: () => getPrices(symbol),
    staleTime: 5 * 60 * 1000,
  });

  const chartPoints = useMemo<ChartPoint[]>(
    () =>
      rawPoints.map((point) => ({
        ...point,
        dateLabel: new Date(point.time).toLocaleDateString(),
      })),
    [rawPoints]
  );
  const data = useMemo(() => downsampleToMax(chartPoints, 500), [chartPoints]);

  const priceFormatter = useMemo(
    () =>
      new Intl.NumberFormat(undefined, {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      }),
    []
  );

  const latestPoint = rawPoints.length
    ? rawPoints[rawPoints.length - 1]
    : undefined;
  const latestCloseText = latestPoint
    ? `Latest close ${priceFormatter.format(latestPoint.price)} on ${new Date(
        latestPoint.time
      ).toLocaleDateString()}`
    : null;

  return (
    <Card className="mx-auto w-full max-w-5xl">
      <CardHeader>
        <CardTitle className="text-2xl font-bold">{symbol}</CardTitle>
        {latestCloseText && (
          <CardDescription>{latestCloseText}</CardDescription>
        )}
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
