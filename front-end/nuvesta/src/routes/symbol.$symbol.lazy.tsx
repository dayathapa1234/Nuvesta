import { createLazyFileRoute } from "@tanstack/react-router";
import PriceChart from "../components/PriceChart";
import BackdatedPurchaseForm from "../components/BackdatedPurchaseForm";

export const Route = createLazyFileRoute("/symbol/$symbol")({
  component: SymbolRoute,
});

function SymbolRoute() {
  return (
    <div className="flex flex-col items-center gap-6">
      <PriceChart />
      <BackdatedPurchaseForm />
    </div>
  );
}
