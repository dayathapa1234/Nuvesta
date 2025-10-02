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

export function storeAuth(session: AuthSession) {
  if (typeof window === "undefined") {
    return;
  }
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  dispatchAuthChange(session);
}
