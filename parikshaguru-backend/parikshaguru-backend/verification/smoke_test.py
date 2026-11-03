#!/usr/bin/env python3
"""Live end-to-end smoke test (stdlib only). Start the backend + MySQL first, then:
       python3 verification/smoke_test.py [http://localhost:8080/api]
Uses the seeded demo accounts and the demo exam EX-CS602-DEMO. Run on a fresh database (one attempt per student)."""
import json, sys, urllib.request, urllib.error
BASE = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080/api"
ok = bad = 0

def call(method, path, body=None, token=None):
    req = urllib.request.Request(BASE + path, method=method, data=None if body is None else json.dumps(body).encode(),
                                 headers={"Content-Type": "application/json", **({"Authorization": "Bearer " + token} if token else {})})
    try:
        with urllib.request.urlopen(req) as r:
            t = r.read().decode(); return r.status, (json.loads(t) if t else None)
    except urllib.error.HTTPError as e:
        t = e.read().decode(); return e.code, (json.loads(t) if t else None)

def check(name, cond, extra=""):
    global ok, bad
    if cond: ok += 1; print("  PASS", name)
    else: bad += 1; print("  FAIL", name, extra)

def login(email, pw):
    s, j = call("POST", "/auth/login", {"email": email, "password": pw}); assert s == 200, (email, s, j); return j["token"]

print("1-3 Login (student, faculty, admin)")
S = login("rohan.verma@university.ac.in", "Student@123"); S2 = login("aarav.sharma@university.ac.in", "Student@123")
F = login("nair@university.ac.in", "Faculty@123"); A = login("admin@university.ac.in", "Admin@12345")
check("bad password rejected", call("POST", "/auth/login", {"email": "rohan.verma@university.ac.in", "password": "x"})[0] == 401)
check("no token -> 401", call("GET", "/students/dashboard")[0] == 401)
check("student cannot use faculty API", call("GET", "/exams", token=S)[0] == 403)
check("admin dashboard", call("GET", "/admin/dashboard", token=A)[0] == 200)

print("4-7 Subject / exam / question / publish (faculty)")
st, subj = call("POST", "/subjects", {"code": "SMK101", "name": "Smoke Subject", "department": "CSE", "semester": 6, "creditPoints": 3}, F)
check("subject created", st == 201, subj); sid = subj["id"]
now = "2026-01-01T00:00:00Z"; end = "2035-01-01T00:00:00Z"
st, ex = call("POST", "/exams", {"title": "Smoke", "code": "EX-SMOKE-1", "subjectId": sid, "totalMarks": 1, "durationMinutes": 10,
                                 "startTime": now, "endTime": end, "negativeMarking": False}, F)
check("exam created as DRAFT", st == 201 and ex["status"] == "DRAFT", ex); eid = ex["id"]
check("publish blocked without questions", call("POST", f"/exams/{eid}/publish", token=F)[0] == 400)
call("POST", f"/exams/{eid}/questions", {"questionText": "1+1?", "type": "MCQ", "options": [{"text": "1"}, {"text": "2"}], "correctAnswer": "B", "marks": 1}, F)
st, pub = call("POST", f"/exams/{eid}/publish", token=F); check("exam published", st == 200 and pub["status"] == "PUBLISHED", pub)
check("published exam not editable", call("PUT", f"/exams/{eid}", {"title": "x", "code": "EX-SMOKE-1", "subjectId": sid, "totalMarks": 1, "durationMinutes": 10, "startTime": now, "endTime": end}, F)[0] == 409)

print("8-10 Student attends demo exam, autosave, submit")
st, exams = call("GET", "/students/exams", token=S); demo = next(e for e in exams if e["examCode"] == "EX-CS602-DEMO")
check("demo exam can start", demo["canStart"])
st, sess = call("POST", f"/exams/{demo['examId']}/start", token=S); check("session started", st == 200, sess)
sessid = sess["sessionId"]; qs = sess["questions"]
check("correct answers NOT exposed", "correctAnswer" not in json.dumps(sess))
check("start again resumes same session", call("POST", f"/exams/{demo['examId']}/start", token=S)[1]["sessionId"] == sessid)
check("other student cannot read session", call("GET", f"/sessions/{sessid}", token=S2)[0] == 403)
ans = {1: "D", 2: "A,B,C", 3: "FALSE", 4: "third normal form", 5: "WAL logs changes before data pages are written."}
for q in qs: check(f"autosave Q{q['orderNo']}", call("POST", f"/sessions/{sessid}/answers", {"questionId": q["id"], "answer": ans[q["orderNo"]]}, S)[0] == 200)
check("invalid option rejected", call("POST", f"/sessions/{sessid}/answers", {"questionId": qs[0]["id"], "answer": "Z"}, S)[0] == 400)
st, pr = call("POST", f"/sessions/{sessid}/mark-review", {"questionId": qs[0]["id"], "marked": True}, S); check("mark for review counted", pr["markedForReview"] == 1, pr)
check("answers persisted", len(call("GET", f"/sessions/{sessid}/answers", token=S)[1]) == 5)
check("proctoring event stored", call("POST", f"/sessions/{sessid}/proctoring-events", {"eventType": "TAB_HIDDEN"}, S)[0] == 202)
check("submit ok", call("POST", f"/sessions/{sessid}/submit", token=S)[0] == 200)
check("duplicate submit -> 409", call("POST", f"/sessions/{sessid}/submit", token=S)[0] == 409)
check("answer after submit -> 409", call("POST", f"/sessions/{sessid}/answers", {"questionId": qs[0]["id"], "answer": "A"}, S)[0] == 409)
check("restart after submit -> 409", call("POST", f"/exams/{demo['examId']}/start", token=S)[0] == 409)
st, res = call("GET", "/students/results", token=S); r0 = next(r for r in res["results"] if r["examCode"] == "EX-CS602-DEMO")
check("result hidden until published", r0["marksObtained"] is None and r0["status"] == "PENDING_APPROVAL", r0)

print("11-12 Auto + manual evaluation")
st, subs = call("GET", f"/submissions?examId={demo['examId']}&status=PENDING", token=F); sub = subs["content"][0]
check("objective auto-graded = 8", abs(sub["objectiveMarks"] - 8.0) < 1e-9, sub)
check("approve blocked before grading", call("POST", f"/results/{sub['resultId']}/approve", token=F)[0] == 409)
st, det = call("GET", f"/submissions/{sub['id']}", token=F); desc = next(a for a in det["answers"] if a["type"] == "DESCRIPTIVE")
check("descriptive awaits manual grading", desc["needsManualGrading"])
st, ev = call("POST", f"/submissions/{sub['id']}/evaluate", {"items": [{"questionId": desc["questionId"], "marks": 2, "feedback": "Good"}], "feedback": "Well done"}, F)
check("manual evaluation -> GRADED 10", st == 200 and ev["summary"]["gradingStatus"] == "GRADED" and abs(ev["summary"]["totalMarks"] - 10) < 1e-9, ev)
check("marks above maximum rejected", call("POST", f"/submissions/{sub['id']}/evaluate", {"items": [{"questionId": desc["questionId"], "marks": 99}]}, F)[0] == 400)

print("13-15 Approve, publish, student views result")
check("publish before approve -> 409", call("POST", f"/results/{sub['resultId']}/publish", token=F)[0] == 409)
check("approve ok", call("POST", f"/results/{sub['resultId']}/approve", token=F)[0] == 200)
st, p = call("POST", f"/results/{sub['resultId']}/publish", token=F); check("publish ok", st == 200 and p["status"] == "PUBLISHED", p)
check("edit after publication blocked", call("POST", f"/submissions/{sub['id']}/evaluate", {"items": [{"questionId": desc["questionId"], "marks": 1}]}, F)[0] == 409)
st, res = call("GET", "/students/results", token=S); r0 = next(r for r in res["results"] if r["examCode"] == "EX-CS602-DEMO")
check("student sees 10/10 A+", r0["marksObtained"] == 10 and r0["grade"] == "A+" and r0["published"], r0)
check("history recorded", len(call("GET", f"/results/{sub['resultId']}/history", token=F)[1]) >= 2)
check("logout invalidates token", call("POST", "/auth/logout", token=S)[0] == 200 and call("GET", "/auth/me", token=S)[0] == 401)

print(f"\n{ok} passed, {bad} failed"); sys.exit(1 if bad else 0)
