import PriceChart from "@/components/PriceChart";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useParams } from "react-router-dom";

export default function PricePage() {
  const { symbol } = useParams<{ symbol: string }>();
  if (!symbol) return null;
  return (
    <div className="p-4 flex justify-center">
      <Card className="w-full max-w-5xl">
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
