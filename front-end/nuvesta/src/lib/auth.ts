export interface AuthUser {
  id: string;
  email: string;
  fullName: string;
}

export interface AuthSession {
  token: string;
  user: AuthUser;
}

const STORAGE_KEY = "nuvesta:auth";
const AUTH_CHANGED_EVENT = "nuvesta:auth-changed";

function dispatchAuthChange(session: AuthSession | null) {
  if (typeof window === "undefined") {
    return;
  }
  window.dispatchEvent(
    new CustomEvent<AuthSession | null>(AUTH_CHANGED_EVENT, {
      detail: session,
    })
  );
}

export function onAuthChange(listener: (session: AuthSession | null) => void) {
  if (typeof window === "undefined") {
    return () => {};
  }
  const handler = (event: Event) => {
    const customEvent = event as CustomEvent<AuthSession | null>;
    listener(customEvent.detail ?? null);
  };
  window.addEventListener(AUTH_CHANGED_EVENT, handler);
  return () => {
    window.removeEventListener(AUTH_CHANGED_EVENT, handler);
  };
}

export function loadAuth(): AuthSession | null {
  if (typeof window === "undefined") {
    return null;
  }
  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as AuthSession;
  } catch (error) {
    console.error("Failed to parse auth session", error);
    window.localStorage.removeItem(STORAGE_KEY);
    dispatchAuthChange(null);
    return null;
  }
}

export function storeAuth(session: AuthSession) {
  if (typeof window === "undefined") {
    return;
  }
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  dispatchAuthChange(session);
}

export function clearAuth() {
  if (typeof window === "undefined") {
    return;
  }
  window.localStorage.removeItem(STORAGE_KEY);
  dispatchAuthChange(null);
}

export function getAuthToken(): string | null {
  const session = loadAuth();
  return session?.token ?? null;
}

export function getAuthHeader(): HeadersInit {
  const token = getAuthToken();
  if (!token) {
    return {};
  }
  return { Authorization: `Bearer ${token}` };
}
