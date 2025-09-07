import { createLazyFileRoute } from "@tanstack/react-router";
import SymbolTable from "../components/SymbolTable";

export const Route = createLazyFileRoute("/")({
  component: Home,
});

function Home() {
  return (
    <div>
      <SymbolTable />
    </div>
  );
}
