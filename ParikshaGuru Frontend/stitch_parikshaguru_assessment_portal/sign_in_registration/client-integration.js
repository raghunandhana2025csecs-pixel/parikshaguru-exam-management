// ParikshaGuru API Client & Complete Student Flow Integration
// Configurable backend URL for local/Render deployment
const API_BASE_URL = window.API_BASE_URL || "http://localhost:8080";
const BASE = `${API_BASE_URL}/api`;
const TOKEN_KEY = "pg_token";

class ApiError extends Error {
  constructor(status, message, fieldErrors) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
    this.name = "ApiError";
  }
}

const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY) ?? sessionStorage.getItem(TOKEN_KEY),
  set: (t, persist = true) => { (persist ? localStorage : sessionStorage).setItem(TOKEN_KEY, t); },
  clear: () => { localStorage.removeItem(TOKEN_KEY); sessionStorage.removeItem(TOKEN_KEY); },
};

async function apiRequest(method, path, body) {
  const headers = { "Content-Type": "application/json" };
  const t = tokenStore.get();
  if (t) headers.Authorization = `Bearer ${t}`;
  let res;
  try {
    res = await fetch(`${BASE}${path}`, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  } catch {
    throw new ApiError(0, "Cannot reach the server. Check your connection.");
  }
  if (res.status === 204) return undefined;
  const text = await res.text();
  let data;
  try { data = text ? JSON.parse(text) : undefined; } catch { data = { message: text }; }
  if (!res.ok) {
    if (res.status === 401 && !path.startsWith("/auth/login")) {
      tokenStore.clear();
      window.location.href = "/sign_in/code.html";
    }
    throw new ApiError(res.status, data?.message ?? res.statusText, data?.fieldErrors);
  }
  return data;
}

const api = {
  auth: {
    login: (email, password) => apiRequest("POST", "/auth/login", { email, password }),
    register: (b) => apiRequest("POST", "/auth/register", b),
    logout: () => apiRequest("POST", "/auth/logout"),
    me: () => apiRequest("GET", "/auth/me"),
  },
  departments: () => apiRequest("GET", "/departments"),
  student: {
    dashboard: () => apiRequest("GET", "/students/dashboard"),
    exams: (state) => apiRequest("GET", `/students/exams${state ? '?state=' + state : ''}`),
    results: () => apiRequest("GET", "/students/results"),
    profile: () => apiRequest("GET", "/students/profile"),
    updateProfile: (b) => apiRequest("PUT", "/students/profile", b),
  },
  session: {
    start: (examId) => apiRequest("POST", `/exams/${examId}/start`),
    get: (id) => apiRequest("GET", `/sessions/${id}`),
    saveAnswer: (id, questionId, answer, markedForReview) =>
      apiRequest("POST", `/sessions/${id}/answers`, { questionId, answer, markedForReview }),
    markReview: (id, questionId, marked) => apiRequest("POST", `/sessions/${id}/mark-review`, { questionId, marked }),
    submit: (id) => apiRequest("POST", `/sessions/${id}/submit`),
    event: (id, eventType, details) => apiRequest("POST", `/sessions/${id}/proctoring-events`, { eventType, details }),
  }
};

// Global UI helpers
function showAlert(title, message, isError = true) {
  const alertBox = document.getElementById('authAlert');
  if (!alertBox) {
    console.warn(`${title}: ${message}`);
    return;
  }
  const alertTitle = document.getElementById('alertTitle');
  const alertMsg = document.getElementById('alertMsg');
  if (alertTitle) alertTitle.textContent = title;
  if (alertMsg) alertMsg.textContent = message;

  if (isError) {
    alertBox.className = 'mb-space-md p-3 rounded-lg bg-error-container text-on-error-container flex items-start gap-2.5 shadow-sm transition-all duration-200';
  } else {
    alertBox.className = 'mb-space-md p-3 rounded-lg bg-surface-container-high text-primary font-medium flex items-start gap-2.5 shadow-sm transition-all duration-200';
  }
  alertBox.classList.remove('hidden');
}

function hideAlert() {
  const alertBox = document.getElementById('authAlert');
  if (alertBox) alertBox.classList.add('hidden');
}

// Full page logic initialization
document.addEventListener("DOMContentLoaded", async () => {
  const path = window.location.pathname;

  // Protect non-signin pages
  if (!path.includes("sign_in")) {
    if (!tokenStore.get()) {
      window.location.href = "/sign_in/code.html";
      return;
    }
    try {
      const me = await api.auth.me();
      window.currentUser = me;
      // Update header user name / role if elements exist
      const userBadge = document.querySelector("header .text-label-sm");
      if (userBadge && me.role) userBadge.textContent = `Role: ${me.role} (${me.identifier})`;
    } catch {
      tokenStore.clear();
      window.location.href = "/sign_in/code.html";
      return;
    }
  }

  // 1. SIGN IN / REGISTER FLOW
  const signInForm = document.querySelector("#viewSignIn form");
  if (signInForm) {
    signInForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const email = document.getElementById("signinEmail").value;
      const password = document.getElementById("signinPassword").value;
      const persist = document.getElementById("rememberCheck")?.checked ?? true;
      try {
        showAlert("Authenticating...", "Verifying credentials with Spring Boot backend...", false);
        const res = await api.auth.login(email, password);
        tokenStore.set(res.token, persist);
        showAlert("Success!", "Login successful. Redirecting...", false);
        setTimeout(() => {
          if (res.user.role === "STUDENT") {
            window.location.href = "/student/code.html";
          } else {
            window.location.href = "/faculty/code.html";
          }
        }, 800);
      } catch (err) {
        showAlert("Login Failed", err.message || "Invalid email or password.");
      }
    });
  }

  const registerForm = document.querySelector("#viewRegister form");
  if (registerForm) {
    registerForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const fullName = document.getElementById("regFullName").value;
      const email = document.getElementById("regEmail").value;
      const password = document.getElementById("regPassword").value;
      const isStudent = !document.getElementById("fieldsStudent")?.classList.contains("hidden");
      const role = isStudent ? "STUDENT" : "FACULTY";
      const identifier = isStudent
        ? (document.getElementById("regRollNo").value || "2024-ST-999")
        : (document.getElementById("regFacultyId").value || "FAC-999");
      const department = isStudent
        ? (document.getElementById("regBranch").value || "CSE")
        : (document.getElementById("regFacDept").value || "CSE");
      const course = isStudent ? "B.Tech Computer Science" : null;

      // Parse semester value dynamically from form selection (e.g. "Sem VI / Year 3" -> 6)
      let semester = 6;
      if (isStudent) {
        const semSelectVal = document.getElementById("regSemester")?.value || "";
        if (semSelectVal.includes("I") && !semSelectVal.includes("II") && !semSelectVal.includes("V")) semester = 1;
        else if (semSelectVal.includes("II") && !semSelectVal.includes("III") && !semSelectVal.includes("IV")) semester = 2;
        else if (semSelectVal.includes("III")) semester = 3;
        else if (semSelectVal.includes("IV")) semester = 4;
        else if (semSelectVal.includes("V")) semester = 5;
        else if (semSelectVal.includes("VI")) semester = 6;
        else if (semSelectVal.includes("VII")) semester = 7;
        else if (semSelectVal.includes("VIII")) semester = 8;
      }

      const inviteCode = isStudent ? null : document.getElementById("regInviteCode")?.value;

      try {
        showAlert("Registering...", "Creating institutional account...", false);
        await api.auth.register({
          fullName, email, identifier, department, course, semester,
          password, confirmPassword: password, role, inviteCode
        });
        showAlert("Registration Successful!", "Account created. You can now sign in.", false);
        setTimeout(() => switchAuthTab("signin"), 1500);
      } catch (err) {
        showAlert("Registration Failed", err.message || "Could not register account.");
      }
    });
  }

  // 2. STUDENT DASHBOARD & AVAILABLE EXAMS
  if (path.includes("student")) {
    try {
      const dashboard = await api.student.dashboard();
      const exams = await api.student.exams();

      // Update welcome banner
      const welcomeEl = document.querySelector("h1.font-headline-lg");
      if (welcomeEl && dashboard.fullName) welcomeEl.textContent = `Welcome back, ${dashboard.fullName}`;

      const rollEl = document.querySelector("span.font-semibold");
      if (rollEl && dashboard.rollNumber) rollEl.textContent = `Roll: ${dashboard.rollNumber}`;

      console.log("Dashboard loaded:", dashboard);
      console.log("Exams from API:", exams);

      // Render exam cards with real exam IDs
      const container = document.querySelector("#examContainer") || document.querySelector(".exam-list-container");
      if (container && exams && exams.length > 0) {
        container.innerHTML = "";
        exams.forEach((exam, idx) => {
          const card = document.createElement("div");
          card.className = "exam-card group bg-surface-container-lowest p-space-md rounded-xl transition hover:shadow-md flex flex-col justify-between";
          card.dataset.examId = exam.id;

          const statusColor = exam.state === "READY_TO_START" ? "emerald" : exam.state === "IN_PROGRESS" ? "blue" : "amber";
          const statusText = exam.state === "READY_TO_START" ? "Ready to Start" : exam.state === "IN_PROGRESS" ? "Attempt in Progress" : "Scheduled";

          card.innerHTML = `
            <div class="flex flex-col sm:flex-row sm:items-start justify-between gap-space-sm mb-space-sm">
              <div class="space-y-1">
                <div class="flex items-center gap-space-xs flex-wrap">
                  <span class="px-2 py-0.5 rounded font-label-sm text-label-sm bg-surface-container font-mono text-primary font-bold">${exam.code}</span>
                  <span class="px-2 py-0.5 rounded-full font-label-sm text-label-sm bg-${statusColor}-100 text-${statusColor}-800 font-semibold flex items-center gap-1">
                    ${statusText}
                  </span>
                </div>
                <h3 class="font-headline-sm text-headline-sm text-on-surface font-bold">${exam.title}</h3>
                <p class="font-body-sm text-body-sm text-on-surface-variant">${exam.subject || "Assessment"}</p>
              </div>
              <div class="sm:text-right shrink-0">
                <div class="font-headline-sm text-headline-sm text-primary font-bold">${exam.marks || "50"} Marks</div>
              </div>
            </div>
            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-space-sm pt-space-xs">
              <div class="flex items-center gap-2 text-label-sm font-label-sm text-on-surface-variant">
                <span class="material-symbols-outlined text-[16px]">timer</span>
                <span>${exam.duration || 60} mins</span>
              </div>
              <button class="px-space-md py-2.5 rounded-lg bg-primary text-on-primary font-label-lg text-label-lg font-bold hover:bg-primary-container transition shadow-sm flex items-center justify-center gap-2 start-exam-btn" type="button" data-exam-id="${exam.id}">
                <span>${exam.state === "IN_PROGRESS" ? "Resume" : "Start"} Exam</span>
                <span class="material-symbols-outlined text-[18px]">play_arrow</span>
              </button>
            </div>
          `;
          container.appendChild(card);
        });
      }

      // Attach global click handler for exam start/resume buttons
      document.addEventListener("click", async (ev) => {
        const btn = ev.target.closest("button.start-exam-btn");
        if (!btn) return;
        ev.preventDefault();
        const examId = btn.getAttribute("data-exam-id");
        if (!examId) {
          showAlert("Error", "Exam ID not found");
          return;
        }
        try {
          showAlert("Starting Exam...", "Initializing secure assessment session...", false);
          const session = await api.session.start(examId);
          console.log("Session created:", session);
          window.location.href = `/exam-hall/code.html?sessionId=${session.sessionId}`;
        } catch (err) {
          showAlert("Session Error", err.message || "Could not start exam session.");
        }
      });
    } catch (err) {
      console.error("Dashboard load failed:", err);
      showAlert("Dashboard Error", err.message);
    }
  }

  // 3. LIVE EXAM HALL SESSION & QUESTION TAKING
  if (path.includes("exam-hall")) {
    const params = new URLSearchParams(window.location.search);
    const sessionId = params.get("sessionId");
    if (!sessionId) {
      alert("No active session ID specified.");
      window.location.href = "/student/code.html";
      return;
    }

    try {
      const session = await api.session.get(sessionId);
      window.currentSession = session;
      window.activeQuestionIndex = 0;
      window.sessionId = sessionId;

      console.log("Session loaded:", session);

      // Update exam header details
      const titleEl = document.querySelector("h1.font-headline-md");
      if (titleEl) titleEl.textContent = `${session.examCode}: ${session.examTitle}`;

      const timerEl = document.getElementById("countdown-timer");
      let remainingSecs = session.remainingSeconds || 3600;

      const updateTimer = () => {
        if (remainingSecs <= 0) {
          if (timerEl) timerEl.textContent = "00:00:00";
          alert("Examination time is over. Submitting automatically.");
          api.session.submit(sessionId).then(r => {
            alert(r.message || "Exam submitted successfully.");
            window.location.href = "/student/code.html";
          }).catch(() => {});
          return;
        }
        const h = String(Math.floor(remainingSecs / 3600)).padStart(2, '0');
        const m = String(Math.floor((remainingSecs % 3600) / 60)).padStart(2, '0');
        const s = String(remainingSecs % 60).padStart(2, '0');
        if (timerEl) timerEl.textContent = `${h}:${m}:${s}`;
        remainingSecs--;
      };
      updateTimer();
      setInterval(updateTimer, 1000);

      // Render first question
      renderActiveQuestion(sessionId);

      // Navigation buttons
      const navButtons = document.querySelectorAll("button");
      let prevBtn = null;
      let nextBtn = null;
      navButtons.forEach(b => {
        const text = b.textContent || "";
        const icon = b.querySelector(".material-symbols-outlined")?.textContent || "";
        if (icon.includes("arrow_back") || text.includes("Previous")) prevBtn = b;
        if (icon.includes("arrow_forward") || text.includes("Next")) nextBtn = b;
      });

      if (prevBtn) {
        prevBtn.addEventListener("click", () => {
          if (window.activeQuestionIndex > 0) {
            window.activeQuestionIndex--;
            renderActiveQuestion(sessionId);
          }
        });
      }

      if (nextBtn) {
        nextBtn.addEventListener("click", () => {
          if (window.activeQuestionIndex < session.questions.length - 1) {
            window.activeQuestionIndex++;
            renderActiveQuestion(sessionId);
          }
        });
      }

      // Submit modal button
      const submitBtn = document.getElementById("open-submit-modal-btn");
      if (submitBtn) {
        submitBtn.addEventListener("click", async () => {
          if (confirm("Are you sure you want to finish and submit your examination? This action is final.")) {
            try {
              const res = await api.session.submit(sessionId);
              alert(res.message || "Exam successfully submitted.");
              window.location.href = "/student/code.html";
            } catch (err) {
              alert(err.message || "Submission failed.");
            }
          }
        });
      }

    } catch (err) {
      console.error("Failed to load exam session:", err);
      showAlert("Session Load Failed", err.message);
    }
  }
});

// Render question helper for Live Exam Hall
function renderActiveQuestion(sessionId) {
  const session = window.currentSession;
  if (!session || !session.questions || session.questions.length === 0) return;

  const q = session.questions[window.activeQuestionIndex];
  console.log("Rendering question:", q);

  // Update question number
  const qNumEl = document.querySelector(".bg-surface-container-low .font-headline-sm") || document.querySelector("span.px-space-sm");
  if (qNumEl) qNumEl.textContent = `Q${window.activeQuestionIndex + 1}`;

  // Update question text
  const qTextEl = document.querySelector(".font-body-lg");
  if (qTextEl) qTextEl.textContent = q.questionText || q.text || "Question text not available";

  // Render options
  const optionsGroup = document.getElementById("question-options-group");
  if (optionsGroup && q.options) {
    optionsGroup.innerHTML = "";
    q.options.forEach((opt, idx) => {
      const label = document.createElement("label");
      label.className = "group flex items-start gap-space-md p-space-md rounded-xl bg-surface-container-low hover:bg-surface-container transition-all cursor-pointer";

      const optionId = `opt_${idx}`;
      const isSelected = q.selectedAnswer === opt.id || q.selectedAnswer === idx;

      label.innerHTML = `
        <div class="pt-0.5 flex items-center">
          <input class="w-5 h-5 accent-primary text-primary focus:ring-0 cursor-pointer" type="radio" name="question_answer" value="${opt.id}" ${isSelected ? 'checked' : ''}/>
        </div>
        <div class="flex-1">
          <div class="flex items-center gap-space-xs mb-1">
            <span class="w-6 h-6 rounded-md bg-surface-container-highest text-on-surface flex items-center justify-center font-label-md text-label-md">${String.fromCharCode(65 + idx)}</span>
          </div>
          <p class="font-body-md text-body-md text-on-surface leading-normal">${opt.text || opt.option || opt}</p>
        </div>
      `;

      const radio = label.querySelector("input[type='radio']");
      radio.addEventListener("change", async () => {
        // Save answer
        try {
          await api.session.saveAnswer(sessionId, q.id, opt.id, false);
          console.log("Answer saved:", q.id, opt.id);
          // Update selected styling
          document.querySelectorAll("#question-options-group label").forEach(l => {
            l.classList.remove("bg-secondary-container");
            l.classList.add("bg-surface-container-low");
          });
          label.classList.remove("bg-surface-container-low");
          label.classList.add("bg-secondary-container");
        } catch (err) {
          console.error("Failed to save answer:", err);
        }
      });

      optionsGroup.appendChild(label);
    });
  }

  // Mark for review button
  const markBtn = document.getElementById("mark-review-btn");
  if (markBtn) {
    markBtn.onclick = async () => {
      try {
        await api.session.markReview(sessionId, q.id, !q.markedForReview);
        q.markedForReview = !q.markedForReview;
        markBtn.classList.toggle("bg-tertiary-container");
        markBtn.classList.toggle("text-on-tertiary");
        console.log("Mark for review toggled:", q.id);
      } catch (err) {
        console.error("Failed to mark for review:", err);
      }
    };
  }

  // Clear response button
  const clearBtn = document.getElementById("clear-response-btn");
  if (clearBtn) {
    clearBtn.onclick = () => {
      document.querySelectorAll("#question-options-group input[type='radio']").forEach(r => r.checked = false);
      q.selectedAnswer = null;
      document.querySelectorAll("#question-options-group label").forEach(l => {
        l.classList.remove("bg-secondary-container");
        l.classList.add("bg-surface-container-low");
      });
    };
  }
}
