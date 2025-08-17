import SymbolTable from "./components/SymbolTable";
import { Routes, Route } from "react-router-dom";
import PricePage from "./pages/PricePage";

function Home() {
  return (
    <div className="p-4">
      <h2 className="text-2xl font-bold mb-4">Symbols</h2>
      <SymbolTable />
    </div>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/symbol/:symbol" element={<PricePage />} />
    </Routes>
  );
}
