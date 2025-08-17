import SymbolTable from "./components/SymbolTable";
import { Routes, Route } from "react-router-dom";
import PricePage from "./pages/PricePage";
import ModeToggle from "./components/ModeToggle";
import { Card, CardContent, CardHeader, CardTitle } from "./components/ui/card";

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

export default function App() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-background to-muted/20">
      <div className="flex justify-end p-4">
        <ModeToggle />
      </div>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/symbol/:symbol" element={<PricePage />} />
      </Routes>
    </div>
  );
}
