import { Outlet, createRootRoute } from "@tanstack/react-router";
import { GlassPage } from "../components/GlassPage";

export const Route = createRootRoute({
  component: Root,
});

function Root() {
  return (
    <GlassPage backgroundUrl="https://images.unsplash.com/photo-1720174595774-2b5b4cb19d47?q=80&w=1974&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D">
      <Outlet />
    </GlassPage>
  );
}
