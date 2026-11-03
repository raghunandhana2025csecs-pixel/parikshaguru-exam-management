// src/context/AuthContext.tsx
import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { api, tokenStore } from "@/lib/api";

type Role = "STUDENT" | "FACULTY" | "ADMIN";
interface User { id: number; fullName: string; email: string; identifier: string; role: Role; department?: string; course?: string; semester?: number; }
interface Ctx { user: User | null; loading: boolean; login(email: string, pw: string, persist?: boolean): Promise<User>; logout(): Promise<void>; }
const AuthCtx = createContext<Ctx>(null as any);
export const useAuth = () => useContext(AuthCtx);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const onUnauth = () => setUser(null);
    window.addEventListener("pg:unauthorized", onUnauth);
    if (!tokenStore.get()) setLoading(false);
    else api.auth.me().then(setUser).catch(() => tokenStore.clear()).finally(() => setLoading(false));
    return () => window.removeEventListener("pg:unauthorized", onUnauth);
  }, []);

  const login = async (email: string, pw: string, persist = true) => {
    const r = await api.auth.login(email, pw);
    tokenStore.set(r.token, persist);
    setUser(r.user);
    return r.user as User;
  };
  const logout = async () => { try { await api.auth.logout(); } catch {} tokenStore.clear(); setUser(null); };
  return <AuthCtx.Provider value={{ user, loading, login, logout }}>{children}</AuthCtx.Provider>;
}

/** Usage: <Route element={<ProtectedRoute roles={["STUDENT"]}/>}> ... </Route> */
export function ProtectedRoute({ roles, children }: { roles?: Role[]; children: ReactNode }) {
  const { user, loading } = useAuth();
  const loc = useLocation();
  if (loading) return <div className="p-8 text-center">Loading…</div>;
  if (!user) return <Navigate to="/login" state={{ from: loc }} replace />;
  if (roles && !roles.includes(user.role)) return <Navigate to={user.role === "STUDENT" ? "/dashboard" : "/overview"} replace />;
  return <>{children}</>;
}
