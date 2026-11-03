# Connecting the ParikshaGuru frontend

1. Copy `api.ts` -> `src/lib/api.ts`, `AuthContext.tsx` -> `src/context/AuthContext.tsx`,
   `useExamSession.ts` -> `src/hooks/useExamSession.ts`, and create `.env` from `.env.example`.
2. Wrap the app in `<AuthProvider>` and guard routes with `<ProtectedRoute roles={["STUDENT"]}>` /
   `roles={["FACULTY","ADMIN"]}`.
3. Replace each hard-coded value with the matching call:

| Screen | Call |
|---|---|
| Login / New Registration | `api.auth.login`, `api.auth.register`, `api.departments()` (dropdown) |
| Forgot Key? | `api.auth.forgot`, `api.auth.reset` |
| Student Dashboard | `api.student.dashboard()` (name, roll, counts, exams, notifications) |
| Available Exams / Start Exam / Resume | `api.student.exams()` then `api.session.start(examId)` -> navigate to `/exam/:sessionId` |
| Active Assessment page | `useExamSession(sessionId)` (timer, autosave, palette counts, mark-for-review, Finish & Submit) |
| My Results | `api.student.results()` (marks are `null` until PUBLISHED) |
| My Profile | `api.student.profile/updateProfile`, `api.auth.changePassword` |
| Faculty Overview | `api.admin.dashboard()`, `api.exams.list({q,status,page})` |
| Subject Management | `api.subjects.*` |
| Exam Builder | `api.exams.create/update/publish`, `api.exams.addQuestion`, `api.questions.update/remove` |
| Submissions & Grading | `api.submissions.list/get/evaluate` |
| Results Release | `api.results.list({status:"PENDING_APPROVAL"})`, `approve`, `publish`, `bulkApprove`, `bulkPublish` |
| Bell icon | `api.notifications.unread()` (poll every 30 s), `list`, `markAllRead` |

4. Remove the "Quick Demo Fill" buttons or point them at the seeded demo accounts (dev only).
5. Show `ApiError.message` in a toast; show `ApiError.fieldErrors[field]` under form inputs.
