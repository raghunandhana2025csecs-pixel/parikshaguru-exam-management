// src/lib/api.ts  - single API layer for the ParikshaGuru Spring Boot backend.
const BASE = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api";
const TOKEN_KEY = "pg_token"; // only the JWT is kept client-side; all exam data lives in MySQL

export class ApiError extends Error {
  constructor(public status: number, message: string, public fieldErrors?: Record<string, string>) { super(message); }
}

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY) ?? sessionStorage.getItem(TOKEN_KEY),
  set: (t: string, persist = true) => { (persist ? localStorage : sessionStorage).setItem(TOKEN_KEY, t); },
  clear: () => { localStorage.removeItem(TOKEN_KEY); sessionStorage.removeItem(TOKEN_KEY); },
};

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  const t = tokenStore.get();
  if (t) headers.Authorization = `Bearer ${t}`;
  let res: Response;
  try {
    res = await fetch(`${BASE}${path}`, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  } catch {
    throw new ApiError(0, "Cannot reach the server. Check your connection.");
  }
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  const data = text ? JSON.parse(text) : undefined;
  if (!res.ok) {
    if (res.status === 401 && !path.startsWith("/auth/login")) { tokenStore.clear(); window.dispatchEvent(new Event("pg:unauthorized")); }
    throw new ApiError(res.status, data?.message ?? res.statusText, data?.fieldErrors);
  }
  return data as T;
}
const get = <T,>(p: string) => request<T>("GET", p);
const post = <T,>(p: string, b?: unknown) => request<T>("POST", p, b ?? {});
const put = <T,>(p: string, b?: unknown) => request<T>("PUT", p, b);
const del = <T,>(p: string) => request<T>("DELETE", p);
const qs = (o: Record<string, unknown>) => {
  const s = new URLSearchParams(Object.entries(o).filter(([, v]) => v !== undefined && v !== "" && v !== null).map(([k, v]) => [k, String(v)]));
  return s.toString() ? `?${s}` : "";
};

export const api = {
  auth: {
    login: (email: string, password: string) => post<any>("/auth/login", { email, password }),
    register: (b: any) => post<any>("/auth/register", b),
    logout: () => post("/auth/logout"),
    me: () => get<any>("/auth/me"),
    forgot: (email: string) => post<any>("/auth/forgot-password", { email }),
    reset: (b: any) => post("/auth/reset-password", b),
    changePassword: (b: any) => put<any>("/auth/change-password", b),
  },
  departments: () => get<any[]>("/departments"),
  student: {
    dashboard: () => get<any>("/students/dashboard"),
    profile: () => get<any>("/students/profile"),
    updateProfile: (b: any) => put<any>("/students/profile", b),
    exams: (state?: string) => get<any[]>(`/students/exams${qs({ state })}`),
    results: () => get<any>("/students/results"),
  },
  admin: {
    dashboard: () => get<any>("/admin/dashboard"),
    users: (p: any) => get<any>(`/admin/users${qs(p)}`),
    setUserStatus: (id: number, status: string) => request<any>("PATCH", `/admin/users/${id}/status`, { status }),
  },
  subjects: {
    list: (p: any = {}) => get<any>(`/subjects${qs(p)}`),
    create: (b: any) => post<any>("/subjects", b),
    update: (id: number, b: any) => put<any>(`/subjects/${id}`, b),
    remove: (id: number) => del(`/subjects/${id}`),
  },
  exams: {
    list: (p: any = {}) => get<any>(`/exams${qs(p)}`),
    get: (id: number) => get<any>(`/exams/${id}`),
    create: (b: any) => post<any>("/exams", b),
    update: (id: number, b: any) => put<any>(`/exams/${id}`, b),
    remove: (id: number) => del(`/exams/${id}`),
    publish: (id: number) => post<any>(`/exams/${id}/publish`),
    assign: (id: number, studentIds?: number[]) => post<any>(`/exams/${id}/assign`, { studentIds }),
    questions: (id: number) => get<any[]>(`/exams/${id}/questions`),
    addQuestion: (id: number, b: any) => post<any>(`/exams/${id}/questions`, b),
  },
  questions: {
    update: (id: number, b: any) => put<any>(`/questions/${id}`, b),
    remove: (id: number) => del(`/questions/${id}`),
  },
  session: {
    start: (examId: number) => post<any>(`/exams/${examId}/start`),
    get: (id: number) => get<any>(`/sessions/${id}`),
    saveAnswer: (id: number, questionId: number, answer?: string, markedForReview?: boolean) =>
      post<any>(`/sessions/${id}/answers`, { questionId, answer, markedForReview }),
    markReview: (id: number, questionId: number, marked: boolean) => post<any>(`/sessions/${id}/mark-review`, { questionId, marked }),
    answers: (id: number) => get<any[]>(`/sessions/${id}/answers`),
    submit: (id: number) => post<any>(`/sessions/${id}/submit`),
    event: (id: number, eventType: string, details?: string) => post(`/sessions/${id}/proctoring-events`, { eventType, details }),
  },
  submissions: {
    list: (p: any = {}) => get<any>(`/submissions${qs(p)}`),
    get: (id: number) => get<any>(`/submissions/${id}`),
    evaluate: (id: number, b: any) => post<any>(`/submissions/${id}/evaluate`, b),
  },
  results: {
    list: (p: any = {}) => get<any>(`/results${qs(p)}`),
    approve: (id: number) => post<any>(`/results/${id}/approve`),
    publish: (id: number) => post<any>(`/results/${id}/publish`),
    bulkApprove: (ids: number[]) => post<any>("/results/bulk-approve", { ids }),
    bulkPublish: (ids: number[]) => post<any>("/results/bulk-publish", { ids }),
    history: (id: number) => get<any[]>(`/results/${id}/history`),
  },
  notifications: {
    list: (p: any = {}) => get<any>(`/notifications${qs(p)}`),
    unread: () => get<{ unread: number }>("/notifications/unread-count"),
    markRead: (id: number) => put(`/notifications/${id}/read`),
    markAllRead: () => put("/notifications/read-all"),
    send: (b: any) => post<any>("/notifications", b),
  },
};
