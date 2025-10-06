import { register, type RegisterPayload } from "@/api/auth";
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
import { useMutation } from "@tanstack/react-query";
import { createLazyFileRoute, Link, useNavigate } from "@tanstack/react-router";
import React, { useState } from "react";

export const Route = createLazyFileRoute("/register")({
  component: RegisterPage,
});

function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState<RegisterPayload>({
    email: "form",
    fullName: "",
    password: "",
  });

  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: register,
    onSuccess: (session) => {
      storeAuth(session);
      navigate({ to: "/" });
    },
    onError: (err: unknown) => {
      const message =
        err instanceof Error ? err.message : "Unable to creatre your account";
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
      <Card className="w-full max-w-2xl border-white/20 bg-white/5 shadow-2xl backdrop-blur-2xl">
        <CardHeader>
          <CardTitle className="text-3xl font-semibold text-white">
            Join Nuvesta
          </CardTitle>
          <CardDescription className="text-sm text-white/80">
            Create an account to start tracking your investments.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={handleSubmit}>
            <div className="space-y-2">
              <label
                htmlFor="fullName"
                className="text-lg font-medium text-white space-y-6"
              >
                Full name
              </label>
              <Input
                id="fullName"
                name="fullName"
                type="text"
                value={form.fullName}
                onChange={handleChange}
                placeholder="John Doe"
                required
                className="bg-white/5 text-white placeholder:text-white/50"
                autoComplete="name"
              />
            </div>
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
                minLength={8}
                className="bg-white/5 text-white placeholder:text-white/50"
                autoComplete="new-password"
              />
            </div>
            {error ? <p className="text-sm text-red-300">{error}</p> : null}
            <Button
              type="submit"
              className="w-full bg-white/80 text-slate-900 hover:bg-white"
              disabled={mutation.isPending}
            >
              {mutation.isPending ? "Creating account..." : "Sign up"}
            </Button>
          </form>
        </CardContent>
        <CardFooter className="flex-col gap-2 text-sm text-white/80">
          <span className="text-center">
            Already have an account?{" "}
            <Link className="font-semibold text-white" to="/login">
              Sign in
            </Link>
          </span>
        </CardFooter>
      </Card>
    </div>
  );
}
