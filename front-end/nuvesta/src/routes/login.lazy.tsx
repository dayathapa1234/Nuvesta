import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Link, createLazyFileRoute, useNavigate } from "@tanstack/react-router";

import { login, type LoginPayload } from "@/api/auth";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { storeAuth } from "@/lib/auth";

export const Route = createLazyFileRoute("/login")({
  component: LoginPage,
});

function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState<LoginPayload>({ email: "", password: "" });
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (session) => {
      storeAuth(session);
      navigate({ to: "/" });
    },
    onError: (err: unknown) => {
      const message = err instanceof Error ? err.message : "Unable to login";
      setError(message);
    },
  });

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    mutation.mutate(form);
  };

  return (
    <div className="flex min-h-[80vh] items-center justify-center">
      <Card className="w-full max-w-2xl p-5 border-white/20 bg-white/5 shadow-2xl backdrop-blur-2xl">
        <CardHeader>
          <CardTitle className="text-3xl font-semibold text-white">
            Welcome back
          </CardTitle>
          <CardDescription className="text-sm text-white/80">
            Sign in to continue building your Nuvesta portfolio.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={handleSubmit}>
            <div className="space-y-2">
              <label
                htmlFor="email"
                className="text-lg font-medium text-white space-y-6"
              >
                Email
              </label>
              <Input
                id="email"
                name="email"
                type="email"
                value={form.email}
                onChange={handleChange}
                placeholder="you@example.com"
                required
                className="bg-white/5 text-white placeholder:text-white/50"
                autoComplete="email"
              />
            </div>
            <div className="space-y-2">
              <label
                className="text-lg font-medium text-white"
                htmlFor="password"
              >
                Password
              </label>
              <Input
                id="password"
                name="password"
                type="password"
                value={form.password}
                onChange={handleChange}
                placeholder="Password"
                required
                className="bg-white/5 text-white placeholder:text-white/50"
                autoComplete="current-password"
              />
            </div>
            {error ? <p className="text-sm text-red-300">{error}</p> : null}
            <Button
              type="submit"
              className="w-full bg-white/80 text-slate-900 hover:bg-white"
              disabled={mutation.isPending}
            >
              {mutation.isPending ? "Signing in..." : "Sign in"}
            </Button>
          </form>
        </CardContent>
        <CardFooter className="flex-col gap-2 text-sm text-white/80">
          <span className="text-center">
            New to Nuvesta?{" "}
            <Link className="font-semibold text-white" to="/register">
              Create an account
            </Link>
          </span>
        </CardFooter>
      </Card>
    </div>
  );
}
