import { createLazyFileRoute } from "@tanstack/react-router";
import SymbolTable from "../components/SymbolTable";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "../components/ui/card";

export const Route = createLazyFileRoute("/")({
  component: Home,
});

function Home() {
  return (
    <div className="p-4">
      <Card className="backdrop-blur-md bg-background/60">
        <CardHeader>
          <CardTitle className="text-2xl font-bold mb-4">Symbols</CardTitle>
        </CardHeader>
        <CardContent>
          <SymbolTable />
        </CardContent>
      </Card>
    </div>
  );
}
