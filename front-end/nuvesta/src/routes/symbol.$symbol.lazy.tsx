import { createLazyFileRoute } from "@tanstack/react-router";
import PriceChart from "../components/PriceChart";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "../components/ui/card";

export const Route = createLazyFileRoute("/symbol/$symbol")({
  component: SymbolRoute,
});

function SymbolRoute() {
  const { symbol } = Route.useParams();
  return (
    <div className="p-4 flex justify-center">
      <Card className="w-full max-w-5xl backdrop-blur-md bg-background/60">
        <CardHeader>
          <CardTitle className="text-2xl font-bold">{symbol}</CardTitle>
        </CardHeader>
        <CardContent>
          <PriceChart symbol={symbol} />
        </CardContent>
      </Card>
    </div>
  );
}
