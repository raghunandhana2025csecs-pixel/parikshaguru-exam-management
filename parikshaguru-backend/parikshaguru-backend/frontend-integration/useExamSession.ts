// src/hooks/useExamSession.ts - exam page logic. The server deadline is authoritative; the local timer is display only.
import { useCallback, useEffect, useRef, useState } from "react";
import { api } from "@/lib/api";

export function useExamSession(sessionId: number, onClosed: (msg: string) => void) {
  const [session, setSession] = useState<any>(null);
  const [answers, setAnswers] = useState<Record<number, { answer?: string | null; marked: boolean }>>({});
  const [remaining, setRemaining] = useState(0);
  const [saving, setSaving] = useState<"idle" | "saving" | "error">("idle");
  const skew = useRef(0); // serverTime - clientTime

  const load = useCallback(async () => {
    const s = await api.session.get(sessionId);
    if (s.status !== "IN_PROGRESS") return onClosed(s.message ?? "Examination submitted");
    skew.current = new Date(s.serverTime).getTime() - Date.now();
    setSession(s);
    const map: any = {};
    s.questions.forEach((q: any) => (map[q.id] = { answer: q.selectedAnswer, marked: q.markedForReview }));
    setAnswers(map);
  }, [sessionId]);

  useEffect(() => { load().catch((e) => onClosed(e.message)); }, [load]);

  // countdown computed from the server deadline; when it hits 0 ask the server (it auto-submits)
  useEffect(() => {
    if (!session) return;
    const t = setInterval(() => {
      const left = Math.max(0, Math.floor((new Date(session.deadline).getTime() - (Date.now() + skew.current)) / 1000));
      setRemaining(left);
      if (left === 0) { clearInterval(t); api.session.get(sessionId).then((s) => onClosed(s.message ?? "Time over - submitted")).catch((e) => onClosed(e.message)); }
    }, 1000);
    return () => clearInterval(t);
  }, [session]);

  // basic proctoring events (informational only - not proof of identity or cheating prevention)
  useEffect(() => {
    const send = (t: string, d?: string) => api.session.event(sessionId, t, d).catch(() => {});
    const vis = () => send(document.hidden ? "TAB_HIDDEN" : "TAB_VISIBLE");
    const fs = () => { if (!document.fullscreenElement) send("FULLSCREEN_EXIT"); else send("FULLSCREEN_ENTER"); };
    const off = () => send("CONNECTION_LOST"); const on = () => send("CONNECTION_RESTORED");
    document.addEventListener("visibilitychange", vis); document.addEventListener("fullscreenchange", fs);
    window.addEventListener("offline", off); window.addEventListener("online", on);
    return () => { document.removeEventListener("visibilitychange", vis); document.removeEventListener("fullscreenchange", fs); window.removeEventListener("offline", off); window.removeEventListener("online", on); };
  }, [sessionId]);

  const answer = async (questionId: number, value: string) => {
    setAnswers((a) => ({ ...a, [questionId]: { ...a[questionId], answer: value || null } }));
    setSaving("saving");
    try { await api.session.saveAnswer(sessionId, questionId, value); setSaving("idle"); }
    catch (e: any) { setSaving("error"); if (e.status === 409) onClosed(e.message); }
  };
  const toggleReview = async (questionId: number) => {
    const marked = !answers[questionId]?.marked;
    setAnswers((a) => ({ ...a, [questionId]: { ...a[questionId], marked } }));
    await api.session.markReview(sessionId, questionId, marked).catch(() => {});
  };
  const submit = async () => { const r = await api.session.submit(sessionId); onClosed(r.message); };

  const answered = Object.values(answers).filter((a) => a.answer).length;
  const markedCount = Object.values(answers).filter((a) => a.marked).length;
  return { session, answers, remaining, saving, answer, toggleReview, submit, answered, markedCount,
           unanswered: (session?.totalQuestions ?? 0) - answered };
}
