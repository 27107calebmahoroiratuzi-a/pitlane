export type Customer = {
  id: number;
  fullName: string;
  phone: string;
  email: string;
  address: string | null;
};

export type Vehicle = {
  id: number;
  plateNumber: string;
  make: string;
  model: string;
  year: number;
  color: string | null;
  status: string;
  customerId: number | null;
  customerName: string | null;
};

export type Mechanic = {
  id: number;
  fullName: string;
  phone: string;
  email: string;
  specialization: string | null;
  active: boolean;
};

export type SparePart = {
  id: number;
  sku: string;
  name: string;
  description: string | null;
  unitPrice: number;
  stockQuantity: number;
  reorderLevel: number;
  lowStock: boolean;
};

export type RepairJob = {
  id: number;
  complaint: string;
  diagnosis: string | null;
  repairDescription: string | null;
  dateReceived: string;
  expectedCompletionDate: string | null;
  actualCompletionDate: string | null;
  cost: number;
  status: string;
  vehicleId: number;
  plateNumber: string;
};

export type JobAssignment = {
  mechanicId: number;
  mechanicName: string;
  specialization: string | null;
  assignedAt: string;
};

export type JobPart = {
  id: number;
  sparePartId: number;
  sku: string;
  partName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
};

export type Payment = {
  id: number;
  amount: number;
  paymentMethod: string;
  reference: string | null;
  paidAt: string;
};

export type Invoice = {
  id: number;
  invoiceNumber: string;
  repairJobId: number;
  plateNumber: string;
  customerName: string | null;
  customerEmail: string | null;
  laborAmount: number;
  partsAmount: number;
  totalAmount: number;
  amountPaid: number;
  balanceDue: number;
  status: string;
  issuedAt: string;
  dueAt: string | null;
  payments: Payment[];
};

export type GarageData = {
  customers: Customer[];
  vehicles: Vehicle[];
  mechanics: Mechanic[];
  parts: SparePart[];
  jobs: RepairJob[];
  invoices: Invoice[];
};

export type AuthSession = {
  token: string;
  username: string;
  roles: string[];
  message: string;
  garageId: number | null;
  garageName: string | null;
};

export type GarageInfo = { id: number; name: string; createdAt: string };
/** acceptanceUrl is only returned when the backend runs with INVITATION_EXPOSE_LINK=true. */
export type InvitationInfo = { email: string; role: string; acceptanceUrl: string | null; expiresAt: string; message: string };
export type GarageCreation = { garage: GarageInfo; adminInvitation: InvitationInfo };
export type TeamMember = { id: number; username: string; fullName: string | null; email: string; role: string };
export type InvitationPreview = { email: string; role: string; garageName: string; invitedBy: string; expiresAt: string };
export type InvitationAcceptance = { token: string; fullName: string; phone: string; username: string; password: string };

const apiBase = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api").replace(/\/$/, "");
const authStorageKey = "pitlane.auth";
export const authExpiredEvent = "pitlane:auth-expired";

export function readAuthSession(): AuthSession | null {
  if (typeof window === "undefined") return null;
  try {
    const stored = window.sessionStorage.getItem(authStorageKey);
    if (!stored) return null;
    const session = JSON.parse(stored) as AuthSession;
    return session.token && session.username && Array.isArray(session.roles) ? session : null;
  } catch {
    window.sessionStorage.removeItem(authStorageKey);
    return null;
  }
}

export function clearAuthSession(): void {
  if (typeof window !== "undefined") window.sessionStorage.removeItem(authStorageKey);
}

export async function signIn(username: string, password: string): Promise<AuthSession> {
  const session = await apiRequest<AuthSession>("/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });
  saveAuthSession(session);
  return session;
}

export function previewInvitation(token: string): Promise<InvitationPreview> {
  return apiRequest<InvitationPreview>("/auth/invitations/preview", {
    method: "POST",
    body: JSON.stringify({ token }),
  });
}

export async function acceptInvitation(acceptance: InvitationAcceptance): Promise<AuthSession> {
  const session = await apiRequest<AuthSession>("/auth/invitations/accept", {
    method: "POST",
    body: JSON.stringify(acceptance),
  });
  saveAuthSession(session);
  return session;
}

function saveAuthSession(session: AuthSession): void {
  window.sessionStorage.setItem(authStorageKey, JSON.stringify(session));
}

export async function apiRequest<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers);
  if (init?.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");
  const token = readAuthSession()?.token;
  if (token) headers.set("Authorization", `Bearer ${token}`);

  const response = await fetch(`${apiBase}${path}`, {
    ...init,
    headers,
  });

  if (response.status === 401) {
    clearAuthSession();
    if (typeof window !== "undefined") window.dispatchEvent(new Event(authExpiredEvent));
  }

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null) as { message?: string; errors?: Record<string, string> } | null;
    const fieldErrors = errorBody?.errors ? Object.values(errorBody.errors).join(". ") : "";
    throw new Error(fieldErrors || errorBody?.message || `Request failed (${response.status})`);
  }

  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

export async function loadGarageData(): Promise<GarageData> {
  const [customers, vehicles, mechanics, parts, jobs, invoices] = await Promise.all([
    apiRequest<Customer[]>("/customers"),
    apiRequest<Vehicle[]>("/vehicles"),
    apiRequest<Mechanic[]>("/mechanics"),
    apiRequest<SparePart[]>("/spare-parts"),
    apiRequest<RepairJob[]>("/repair-jobs"),
    apiRequest<Invoice[]>("/invoices"),
  ]);

  return { customers, vehicles, mechanics, parts, jobs, invoices };
}