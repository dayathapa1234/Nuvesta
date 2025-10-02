import type { AuthSession } from "@/lib/auth";

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  email: string;
  fullName: string;
  password: string;
}

const AUTH_BASE =
  import.meta.env.VITE_AUTH_BASE ?? import.meta.env.VITE_API_BASE ?? "";

async function postAuth(path: string, body: unknown): Promise<AuthSession> {
  const response = await fetch(`${AUTH_BASE}${path}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  const contentType = response.headers.get("content-type") ?? "";
  const isJson = contentType.includes("application/json");

  if (!response.ok) {
    let message = "Unable to process request";
    if (isJson) {
      try {
        const errorBody = (await response.json()) as { message?: string };
        if (errorBody?.message) {
          message = errorBody.message;
        }
      } catch (error) {
        console.error("Failed to parse error response", error);
      }
    }
    throw new Error(message);
  }

  if (!isJson) {
    throw new Error("Received unexpected response from server");
  }

  return (await response.json()) as AuthSession;
}

export async function login(payload: LoginPayLoad) {
  return postAuth("/api/auth/login", payload);
}

export async function register(payload: RegisterPayload) {
  return postAuth("/api/auth/register", payload);
}
