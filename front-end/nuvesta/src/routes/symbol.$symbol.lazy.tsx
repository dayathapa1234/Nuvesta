import { createLazyFileRoute } from "@tanstack/react-router";
import PriceChart from "../components/PriceChart";

export const Route = createLazyFileRoute("/symbol/$symbol")({
  component: SymbolRoute,
});

function SymbolRoute() {
  return (
    <div className="flex justify-center">
      <PriceChart />
    </div>
  );
}
