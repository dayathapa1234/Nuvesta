import PriceChart from "@/components/PriceChart";
import { useParams } from "react-router-dom";

export default function PricePage() {
  const { symbol } = useParams<{ symbol: string }>();
  if (!symbol) return null;
  return (
    <div className="p-4 space-y-4">
      <h1 className="text-2xl font-bold">{symbol}</h1>
      <PriceChart symbol={symbol} />
    </div>
  );
}
