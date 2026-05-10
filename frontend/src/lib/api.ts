const BASE = '/api';

function getToken(): string | null {
  if (typeof localStorage === 'undefined') return null;
  return localStorage.getItem('token');
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const res = await fetch(`${BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });
  if (res.status === 204) return null as T;
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new ApiError(res.status, body.error ?? 'Unknown error');
  }
  return res.json();
}

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

// ── Types ─────────────────────────────────────────────────────

export type TaskStatus = 'OPEN' | 'DONE';
export type ProjectStatus = 'ACTIVE' | 'DONE';

export interface Recurrence {
  type: 'DAILY' | 'WEEKLY' | 'BIWEEKLY' | 'MONTHLY' | 'ON_WEEKDAY';
  weekday?: string;
}

export interface Task {
  id: string;
  title: string;
  date: string | null;
  assignedTo: string | null;
  status: TaskStatus;
  recurrence: Recurrence | null;
  projectId: string | null;
}

export interface Project {
  id: string;
  title: string;
  goal: string;
  status: ProjectStatus;
  totalSteps: number;
  completedSteps: number;
}

export interface ProjectDetail {
  id: string;
  title: string;
  goal: string;
  status: ProjectStatus;
  tasks: Task[];
}

export interface FixedEvent {
  id: string;
  title: string;
  date: string;
  recurrence: Recurrence | null;
}

export interface MealNote {
  date: string;
  note: string;
}

export interface Member {
  id: string;
  email: string;
}

// ── Auth ──────────────────────────────────────────────────────

export const auth = {
  register: (email: string, password: string) =>
    request<{ token: string; memberId: string; inviteCode: string }>('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
  join: (email: string, password: string, inviteCode: string) =>
    request<{ token: string; memberId: string }>('/auth/join', {
      method: 'POST',
      body: JSON.stringify({ email, password, inviteCode }),
    }),
  login: (email: string, password: string) =>
    request<{ token: string; memberId: string }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
};

// ── Tasks ─────────────────────────────────────────────────────

export const tasks = {
  today: () => request<Task[]>('/tasks/today'),
  week: (startDate: string) => request<Task[]>(`/tasks/week?startDate=${startDate}`),
  unplanned: () => request<Task[]>('/tasks/unplanned'),
  create: (data: { title: string; date?: string; assignedTo?: string; recurrence?: Recurrence; projectId?: string }) =>
    request<Task>('/tasks', { method: 'POST', body: JSON.stringify(data) }),
  complete: (id: string) => request<Task>(`/tasks/${id}/complete`, { method: 'POST' }),
  reopen:   (id: string) => request<Task>(`/tasks/${id}/reopen`,   { method: 'POST' }),
  update: (id: string, data: { date?: string | null; assignedTo?: string | null }) =>
    request<Task>(`/tasks/${id}`, { method: 'PATCH', body: JSON.stringify(data) }),
};

// ── Fixed Events ──────────────────────────────────────────────

export const fixedEvents = {
  today: () => request<FixedEvent[]>('/fixed-events/today'),
  week: (startDate: string) => request<FixedEvent[]>(`/fixed-events/week?startDate=${startDate}`),
  create: (data: { title: string; date: string; recurrence?: Recurrence }) =>
    request<FixedEvent>('/fixed-events', { method: 'POST', body: JSON.stringify(data) }),
};

// ── Projects ──────────────────────────────────────────────────

export const projects = {
  list: () => request<Project[]>('/projects'),
  get: (id: string) => request<ProjectDetail>(`/projects/${id}`),
  create: (data: { title: string; goal: string }) =>
    request<Project>('/projects', { method: 'POST', body: JSON.stringify(data) }),
  complete: (id: string) => request<Project>(`/projects/${id}/complete`, { method: 'POST' }),
};

// ── Members ───────────────────────────────────────────────────

export const members = {
  list: () => request<Member[]>('/members'),
};

// ── Meal Notes ────────────────────────────────────────────────

export const mealNotes = {
  today: () => request<MealNote | null>('/meal-notes/today'),
  week: (startDate: string) => request<MealNote[]>(`/meal-notes/week?startDate=${startDate}`),
  set: (date: string, note: string) =>
    request<MealNote>(`/meal-notes/${date}`, { method: 'PUT', body: JSON.stringify({ note }) }),
};
