import { useEffect, useState, type FormEvent, type ReactNode } from "react";
import {
  Activity, ArrowDownRight, ArrowRight, Boxes, Building2, CarFront, Check,
  CircleDollarSign, ClipboardList, Clock3, Gauge, LayoutDashboard, LoaderCircle,
  LogOut, Menu, Plus, Search, Settings2, ShieldCheck, Trash2, UserPlus, UsersRound, Wrench, X,
} from "lucide-react";
import {
  apiRequest, authExpiredEvent, clearAuthSession, loadGarageData, readAuthSession, signIn,
  type AuthSession, type Customer, type GarageCreation, type GarageData, type GarageInfo, type InvitationInfo, type Invoice, type TeamMember,
  type JobAssignment, type JobPart, type Mechanic, type RepairJob, type SparePart, type Vehicle,
} from "../api";
import type { Route } from "./+types/home";

type Section = "overview" | "jobs" | "customers" | "vehicles" | "mechanics" | "inventory" | "invoices" | "team";
type Editable = Customer | Vehicle | Mechanic | SparePart | RepairJob;
type Editor = { section: Exclude<Section, "overview" | "invoices" | "team">; item?: Editable };
type FieldOption = { value: string | number; label: string };
type Field = { name: string; label: string; type?: string; required?: boolean; options?: FieldOption[] };
type TableRow = { id: number; cells: ReactNode; record?: Editable };

const navigation: { id: Section; label: string; icon: typeof LayoutDashboard }[] = [
  { id: "overview", label: "Overview", icon: LayoutDashboard },
  { id: "jobs", label: "Repair jobs", icon: ClipboardList },
  { id: "customers", label: "Customers", icon: UsersRound },
  { id: "vehicles", label: "Vehicles", icon: CarFront },
  { id: "mechanics", label: "Mechanics", icon: Wrench },
  { id: "inventory", label: "Parts inventory", icon: Boxes },
  { id: "invoices", label: "Invoices", icon: CircleDollarSign },
  { id: "team", label: "Team access", icon: UserPlus },
];

const titles: Record<Section, string> = {
  overview: "Workshop overview", jobs: "Repair jobs", customers: "Customers", vehicles: "Vehicles",
  mechanics: "Mechanics", inventory: "Parts inventory", invoices: "Invoices & payments", team: "Team access",
};

const resourcePaths: Partial<Record<Section, string>> = {
  customers: "/customers", vehicles: "/vehicles", mechanics: "/mechanics",
  inventory: "/spare-parts", jobs: "/repair-jobs",
};

const money = (amount: number) => new Intl.NumberFormat("en-RW", {
  style: "currency", currency: "RWF", maximumFractionDigits: 0,
}).format(amount);

const dateLabel = (value?: string | null) => value
  ? new Intl.DateTimeFormat("en", { month: "short", day: "numeric", year: "numeric" }).format(new Date(value))
  : "Not scheduled";

function statusClass(status: string) {
  return `status status-${status.toLowerCase().replaceAll("_", "-")}`;
}

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Pitlane | Garage operations" },
    { name: "description", content: "Garage repair operations dashboard" },
  ];
}

export default function Home() {
  const [data, setData] = useState<GarageData>({ customers: [], vehicles: [], mechanics: [], parts: [], jobs: [], invoices: [] });
  const [activeSection, setActiveSection] = useState<Section>("overview");
  const [editor, setEditor] = useState<Editor | null>(null);
  const [selectedJob, setSelectedJob] = useState<RepairJob | null>(null);
  const [selectedInvoice, setSelectedInvoice] = useState<Invoice | null>(null);
  const [assignments, setAssignments] = useState<JobAssignment[]>([]);
  const [jobParts, setJobParts] = useState<JobPart[]>([]);
  const [session, setSession] = useState<AuthSession | null>(null);
  const [sessionReady, setSessionReady] = useState(false);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [working, setWorking] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  async function refreshData() {
    try {
      setLoading(true);
      setError("");
      setData(await loadGarageData());
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Unable to reach the garage API");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    setSession(readAuthSession());
    setSessionReady(true);
  }, []);

  useEffect(() => {
    if (!sessionReady) return;
    if (!session) {
      setLoading(false);
      return;
    }
    if (session.roles.includes("SYSTEM_ADMIN") || !session.garageId) {
      setLoading(false);
      return;
    }
    void refreshData();
  }, [sessionReady, session?.token]);

  useEffect(() => {
    const handleAuthExpired = () => {
      setSession(null);
      setData({ customers: [], vehicles: [], mechanics: [], parts: [], jobs: [], invoices: [] });
      setError("Your session expired. Please sign in again.");
    };
    window.addEventListener(authExpiredEvent, handleAuthExpired);
    return () => window.removeEventListener(authExpiredEvent, handleAuthExpired);
  }, []);

  useEffect(() => {
    if (!selectedJob) {
      setAssignments([]);
      setJobParts([]);
      return;
    }
    Promise.all([
      apiRequest<JobAssignment[]>(`/repair-jobs/${selectedJob.id}/mechanics`),
      apiRequest<JobPart[]>(`/repair-jobs/${selectedJob.id}/parts`),
    ]).then(([mechanicRows, partRows]) => {
      setAssignments(mechanicRows);
      setJobParts(partRows);
    }).catch((cause: unknown) => {
      setError(cause instanceof Error ? cause.message : "Unable to load repair job details");
    });
  }, [selectedJob?.id]);

  async function perform(action: () => Promise<unknown>, successMessage: string) {
    try {
      setWorking(true);
      setError("");
      await action();
      await refreshData();
      setNotice(successMessage);
      window.setTimeout(() => setNotice(""), 3200);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "The action could not be completed");
    } finally {
      setWorking(false);
    }
  }

  function openCreate(section = activeSection) {
    if (section !== "overview" && section !== "invoices" && section !== "team") setEditor({ section });
  }

  async function handleSignIn(username: string, password: string) {
    const authenticatedSession = await signIn(username, password);
    setSession(authenticatedSession);
    setError("");
  }

  function handleSignOut() {
    clearAuthSession();
    setSession(null);
    setData({ customers: [], vehicles: [], mechanics: [], parts: [], jobs: [], invoices: [] });
    setError("");
  }

  async function submitEditor(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!editor) return;
    const raw = Object.fromEntries(new FormData(event.currentTarget).entries());
    const id = editor.item?.id;
    const path = resourcePaths[editor.section] || "";
    let payload: Record<string, unknown>;

    if (editor.section === "customers") {
      payload = { fullName: raw.fullName, phone: raw.phone, email: raw.email, address: raw.address || null };
    } else if (editor.section === "vehicles") {
      payload = {
        plateNumber: raw.plateNumber, make: raw.make, model: raw.model, year: Number(raw.year),
        color: raw.color || null, customerId: raw.customerId ? Number(raw.customerId) : null,
      };
    } else if (editor.section === "mechanics") {
      payload = {
        fullName: raw.fullName, phone: raw.phone, email: raw.email,
        specialization: raw.specialization || null, active: raw.active === "on",
      };
    } else if (editor.section === "inventory") {
      payload = {
        sku: raw.sku, name: raw.name, description: raw.description || null,
        unitPrice: Number(raw.unitPrice), stockQuantity: Number(raw.stockQuantity), reorderLevel: Number(raw.reorderLevel || 0),
      };
    } else {
      const existing = editor.item as RepairJob | undefined;
      const expected = String(raw.expectedCompletionDate || "");
      payload = {
        complaint: raw.complaint, diagnosis: raw.diagnosis || null, repairDescription: raw.repairDescription || null,
        expectedCompletionDate: expected ? `${expected}:00` : null,
        actualCompletionDate: existing?.actualCompletionDate || null,
        cost: Number(raw.cost), status: raw.status || "PENDING", vehicleId: Number(raw.vehicleId),
      };
    }

    await perform(async () => {
      await apiRequest(`${path}${id ? `/${id}` : ""}`, { method: id ? "PUT" : "POST", body: JSON.stringify(payload) });
      setEditor(null);
    }, `${singularTitle(editor.section)} ${id ? "updated" : "created"}`);
  }

  async function deleteRecord(section: Section, id: number) {
    const path = resourcePaths[section];
    if (path && window.confirm("Delete this record? This action cannot be undone.")) {
      await perform(() => apiRequest(`${path}/${id}`, { method: "DELETE" }), "Record deleted");
    }
  }

  async function refreshJobDetails(jobId: number) {
    const [mechanicRows, partRows] = await Promise.all([
      apiRequest<JobAssignment[]>(`/repair-jobs/${jobId}/mechanics`),
      apiRequest<JobPart[]>(`/repair-jobs/${jobId}/parts`),
    ]);
    setAssignments(mechanicRows);
    setJobParts(partRows);
    await refreshData();
  }

  async function submitAssignment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedJob) return;
    const mechanicId = Number(new FormData(event.currentTarget).get("mechanicId"));
    await perform(async () => {
      await apiRequest(`/repair-jobs/${selectedJob.id}/mechanics/${mechanicId}`, { method: "POST" });
      await refreshJobDetails(selectedJob.id);
    }, "Mechanic assigned");
  }

  async function submitPartUsage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedJob) return;
    const form = new FormData(event.currentTarget);
    await perform(async () => {
      await apiRequest(`/repair-jobs/${selectedJob.id}/parts`, {
        method: "POST", body: JSON.stringify({ sparePartId: Number(form.get("sparePartId")), quantity: Number(form.get("quantity")) }),
      });
      await refreshJobDetails(selectedJob.id);
    }, "Part usage recorded and stock updated");
  }

  async function issueInvoice() {
    if (!selectedJob) return;
    const jobId = selectedJob.id;
    await perform(async () => {
      await apiRequest(`/repair-jobs/${jobId}/invoice`, { method: "POST" });
      setSelectedJob(null);
      setActiveSection("invoices");
    }, "Invoice issued");
  }

  async function submitPayment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedInvoice) return;
    const form = new FormData(event.currentTarget);
    const invoiceId = selectedInvoice.id;
    await perform(async () => {
      await apiRequest(`/invoices/${invoiceId}/payments`, {
        method: "POST",
        body: JSON.stringify({ amount: Number(form.get("amount")), paymentMethod: form.get("paymentMethod"), reference: form.get("reference") || null }),
      });
      setSelectedInvoice(null);
    }, "Payment recorded");
  }

  const query = search.trim().toLowerCase();
  const jobs = data.jobs.filter((job) => `${job.plateNumber} ${job.complaint} ${job.status}`.toLowerCase().includes(query));
  const customers = data.customers.filter((row) => `${row.fullName} ${row.email} ${row.phone}`.toLowerCase().includes(query));
  const vehicles = data.vehicles.filter((row) => `${row.plateNumber} ${row.make} ${row.model} ${row.customerName || ""}`.toLowerCase().includes(query));
  const mechanics = data.mechanics.filter((row) => `${row.fullName} ${row.specialization || ""} ${row.email}`.toLowerCase().includes(query));
  const parts = data.parts.filter((row) => `${row.sku} ${row.name}`.toLowerCase().includes(query));
  const invoices = data.invoices.filter((row) => `${row.invoiceNumber} ${row.plateNumber} ${row.customerName || ""} ${row.status}`.toLowerCase().includes(query));
  const activeCount = data.jobs.filter((job) => !["COMPLETED", "CANCELLED"].includes(job.status)).length;
  const lowStockCount = data.parts.filter((part) => part.lowStock).length;
  const totalBalance = data.invoices.reduce((sum, invoice) => sum + invoice.balanceDue, 0);
  const today = new Date();
  const isSystemAdmin = session?.roles.includes("SYSTEM_ADMIN") ?? false;
  const canManageTeam = Boolean(session?.roles.some((role) => ["GARAGE_ADMIN", "ADMIN", "MANAGER", "STAFF"].includes(role)));

  if (!sessionReady) {
    return <div className="auth-loading"><LoaderCircle size={20} className="spin" /><span>Loading session</span></div>;
  }
  if (!session) return <LoginScreen onSignIn={handleSignIn} notice={error} />;
  if (isSystemAdmin) return <PlatformAdminScreen session={session} onSignOut={handleSignOut} />;
  if (!session.garageId) return <LoginScreen onSignIn={handleSignIn} notice="Your account is not assigned to a garage. Ask a system administrator for an invitation." />;

  return <div className="app-frame">
    <aside className={`sidebar ${mobileNavOpen ? "sidebar-open" : ""}`}>
      <div className="brand-lockup"><div className="brand-mark"><Gauge size={21} strokeWidth={2.3} /></div><div><strong>pitlane</strong><span>GARAGE OPERATIONS</span></div><button className="icon-button nav-close" aria-label="Close navigation" onClick={() => setMobileNavOpen(false)}><X size={19} /></button></div>
      <div className="shop-switcher"><div className="shop-avatar">{(session.garageName || "G").slice(0, 1).toUpperCase()}</div><div className="shop-switcher-copy"><strong>{session.garageName}</strong><span>Garage workspace</span></div><Building2 size={15} /></div>
      <div className="nav-caption">WORKSPACE</div>
      <nav className="main-nav" aria-label="Main navigation">{navigation.filter(({ id }) => id !== "team" || canManageTeam).map(({ id, label, icon: Icon }) => <button key={id} className={`nav-item ${activeSection === id ? "nav-item-active" : ""}`} onClick={() => { setActiveSection(id); setMobileNavOpen(false); setSearch(""); }}><Icon size={18} strokeWidth={1.9} /><span>{label}</span>{id === "jobs" && activeCount > 0 && <span className="nav-count">{activeCount}</span>}{id === "inventory" && lowStockCount > 0 && <span className="nav-alert">{lowStockCount}</span>}</button>)}</nav>
      <div className="sidebar-bottom"><div className="online-indicator"><span /> API connection <b>{loading ? "CONNECTING" : error ? "OFFLINE" : "READY"}</b></div><button className="nav-item settings-item" onClick={() => setNotice("Workshop settings are managed by your administrator.")}><Settings2 size={18} /><span>Settings</span></button><div className="user-profile"><div className="user-avatar">{session.username.slice(0, 2).toUpperCase()}</div><div><strong>{session.username}</strong><span>{session.roles.join(" · ")}</span></div><button className="icon-button logout-button" title="Sign out" aria-label="Sign out" onClick={handleSignOut}><LogOut size={15} /></button></div></div>
    </aside>
    {mobileNavOpen && <button className="mobile-scrim" aria-label="Close navigation" onClick={() => setMobileNavOpen(false)} />}

    <main className="main-shell">
      <header className="topbar"><div className="topbar-left"><button className="icon-button mobile-menu" aria-label="Open navigation" onClick={() => setMobileNavOpen(true)}><Menu size={20} /></button><div className="breadcrumb"><span>{session.garageName}</span><ArrowRight size={13} /><strong>{titles[activeSection]}</strong></div></div><div className="topbar-right"><div className="today-label"><span>{new Intl.DateTimeFormat("en", { weekday: "long" }).format(today).toUpperCase()}</span><strong>{new Intl.DateTimeFormat("en", { day: "2-digit", month: "short", year: "numeric" }).format(today).toUpperCase()}</strong></div><div className="topbar-avatar">{session.username.slice(0, 2).toUpperCase()}</div></div></header>
      <section className="page-content">
        <div className="page-heading"><div><div className="eyebrow"><span className="eyebrow-line" /> SERVICE DESK <span className="eyebrow-dot">/</span> {activeSection.toUpperCase()}</div><h1>{titles[activeSection]}</h1><p>{sectionDescription(activeSection)}</p></div><div className="heading-actions">{activeSection !== "overview" && activeSection !== "invoices" && activeSection !== "team" && <button className="button button-primary" onClick={() => openCreate()}><Plus size={17} /> Add {singularTitle(activeSection)}</button>}{activeSection === "overview" && <button className="button button-primary" onClick={() => openCreate("jobs")}><Plus size={17} /> New repair job</button>}</div></div>
        {error && <div className="alert alert-error"><Activity size={17} /><span>{error}{error.toLowerCase().includes("fetch") ? " · Check that the Spring Boot API is running on port 8080." : ""}</span><button className="icon-button" onClick={() => setError("")} aria-label="Dismiss error"><X size={16} /></button></div>}
        {notice && <div className="alert alert-success"><Check size={17} /><span>{notice}</span><button className="icon-button" onClick={() => setNotice("")} aria-label="Dismiss notification"><X size={16} /></button></div>}
        {activeSection === "overview" ? <Overview data={data} loading={loading} onOpenJobs={() => setActiveSection("jobs")} onOpenInventory={() => setActiveSection("inventory")} onOpenInvoices={() => setActiveSection("invoices")} onManageJob={setSelectedJob} /> : activeSection === "team" ? <TeamPanel session={session} /> : <ResourceTable section={activeSection} data={data} search={search} setSearch={setSearch} loading={loading} rows={{ customers, vehicles, mechanics, parts, jobs, invoices }} onEdit={(section, item) => setEditor({ section, item })} onDelete={deleteRecord} onManageJob={setSelectedJob} onPay={setSelectedInvoice} />}
      </section>
      <footer className="page-footer"><span><ShieldCheck size={14} /> Workshop data stays in your garage system</span><span>PITLANE <i>·</i> OPERATIONS</span></footer>
    </main>

    {editor && <EditorDialog editor={editor} data={data} working={working} onClose={() => setEditor(null)} onSubmit={submitEditor} />}
    {selectedJob && <JobDialog job={selectedJob} mechanics={data.mechanics.filter((row) => row.active)} parts={data.parts} assignments={assignments} jobParts={jobParts} hasInvoice={data.invoices.some((invoice) => invoice.repairJobId === selectedJob.id)} working={working} onClose={() => setSelectedJob(null)} onAssign={submitAssignment} onAddPart={submitPartUsage} onIssueInvoice={() => void issueInvoice()} onRemovePart={(usageId) => void perform(async () => { await apiRequest(`/repair-jobs/${selectedJob.id}/parts/${usageId}`, { method: "DELETE" }); await refreshJobDetails(selectedJob.id); }, "Part returned to stock")} />}
    {selectedInvoice && <PaymentDialog invoice={selectedInvoice} working={working} onClose={() => setSelectedInvoice(null)} onSubmit={submitPayment} />}
  </div>;
}

function PlatformAdminScreen({ session, onSignOut }: { session: AuthSession; onSignOut: () => void }) {
  const [garages, setGarages] = useState<GarageInfo[]>([]);
  const [invitation, setInvitation] = useState<InvitationInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [working, setWorking] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    apiRequest<GarageInfo[]>("/system/garages")
      .then(setGarages)
      .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "Unable to load garages"))
      .finally(() => setLoading(false));
  }, []);

  async function createGarage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    setWorking(true);
    setError("");
    setInvitation(null);
    try {
      const created = await apiRequest<GarageCreation>("/system/garages", {
        method: "POST",
        body: JSON.stringify({ name: form.get("name"), adminEmail: form.get("adminEmail") }),
      });
      setGarages((current) => [created.garage, ...current]);
      setInvitation(created.adminInvitation);
      formElement.reset();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Garage could not be created");
    } finally {
      setWorking(false);
    }
  }

  return <div className="app-frame">
    <aside className="sidebar">
      <div className="brand-lockup"><div className="brand-mark"><Gauge size={21} strokeWidth={2.3} /></div><div><strong>pitlane</strong><span>PLATFORM CONTROL</span></div></div>
      <div className="shop-switcher"><div className="shop-avatar">P</div><div className="shop-switcher-copy"><strong>Platform administration</strong><span>All garages</span></div><Building2 size={15} /></div>
      <div className="nav-caption">ADMINISTRATION</div>
      <nav className="main-nav" aria-label="Platform administration"><div className="nav-item nav-item-active"><Building2 size={18} /><span>Garages</span></div></nav>
      <div className="sidebar-bottom"><div className="user-profile"><div className="user-avatar">{session.username.slice(0, 2).toUpperCase()}</div><div><strong>{session.username}</strong><span>SYSTEM ADMIN</span></div><button className="icon-button logout-button" title="Sign out" aria-label="Sign out" onClick={onSignOut}><LogOut size={15} /></button></div></div>
    </aside>
    <main className="main-shell">
      <header className="topbar"><div className="breadcrumb"><span>Platform</span><ArrowRight size={13} /><strong>Garages</strong></div><div className="topbar-avatar">{session.username.slice(0, 2).toUpperCase()}</div></header>
      <section className="page-content">
        <div className="page-heading"><div><div className="eyebrow"><span className="eyebrow-line" /> PLATFORM <span className="eyebrow-dot">/</span> TENANTS</div><h1>Garage network</h1><p>Create garages and invite their first administrator.</p></div></div>
        {error && <div className="alert alert-error" role="alert"><Activity size={16} /><span>{error}</span></div>}
        {invitation && <InviteResult invitation={invitation} kicker="GARAGE ADMIN INVITED" />}
        <div className="admin-layout">
          <section className="dashboard-panel admin-form-panel"><div className="panel-heading"><div><span className="section-kicker">NEW TENANT</span><h2>Create garage</h2></div></div><form className="admin-form" onSubmit={createGarage}><label className="form-field">Garage name<input name="name" maxLength={120} required /></label><label className="form-field">First admin email<input name="adminEmail" type="email" maxLength={150} required /></label><button className="button button-primary" type="submit" disabled={working}>{working && <LoaderCircle size={15} className="spin" />}{working ? "Creating garage" : "Create & invite admin"}<ArrowRight size={15} /></button></form></section>
          <section className="dashboard-panel tenant-list-panel"><div className="panel-heading"><div><span className="section-kicker">NETWORK</span><h2>Registered garages</h2></div><span className="tenant-count">{garages.length}</span></div>{loading ? <div className="loading-state"><LoaderCircle size={20} className="spin" /> Loading garages</div> : garages.length === 0 ? <div className="empty-compact"><Building2 size={19} /><span>No garages created yet</span></div> : <div className="tenant-list">{garages.map((garage) => <div className="tenant-row" key={garage.id}><div className="shop-avatar"><Building2 size={15} /></div><div><strong>{garage.name}</strong><span>Created {dateLabel(garage.createdAt)}</span></div><span className="status status-paid">ACTIVE</span></div>)}</div>}</section>
        </div>
      </section>
      <footer className="page-footer"><span><ShieldCheck size={14} /> Platform administration</span><span>PITLANE <i>·</i> GARAGE NETWORK</span></footer>
    </main>
  </div>;
}

function TeamPanel({ session }: { session: AuthSession }) {
  const [members, setMembers] = useState<TeamMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [working, setWorking] = useState(false);
  const [error, setError] = useState("");
  const [invitation, setInvitation] = useState<InvitationInfo | null>(null);
  const roles = session.roles.includes("GARAGE_ADMIN") || session.roles.includes("ADMIN")
    ? ["MANAGER", "STAFF", "USER"]
    : session.roles.includes("MANAGER") ? ["STAFF", "USER"]
      : session.roles.includes("STAFF") ? ["USER"] : [];

  useEffect(() => {
    apiRequest<TeamMember[]>("/garages/me/users")
      .then(setMembers)
      .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "Unable to load garage team"))
      .finally(() => setLoading(false));
  }, []);

  async function invite(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    setWorking(true);
    setError("");
    setInvitation(null);
    try {
      setInvitation(await apiRequest<InvitationInfo>("/garages/me/invitations", {
        method: "POST",
        body: JSON.stringify({ email: form.get("email"), role: form.get("role") }),
      }));
      formElement.reset();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Invitation could not be created");
    } finally {
      setWorking(false);
    }
  }

  return <div className="team-layout">
    <section className="dashboard-panel team-list-panel"><div className="panel-heading"><div><span className="section-kicker">{session.garageName}</span><h2>Garage team</h2></div><span className="tenant-count">{members.length}</span></div>
      {loading ? <div className="loading-state"><LoaderCircle size={20} className="spin" /> Loading team</div> : <div className="tenant-list">{members.map((member) => <div className="tenant-row" key={member.id}><div className="user-avatar">{(member.fullName || member.username).slice(0, 2).toUpperCase()}</div><div><strong>{member.fullName || member.username}</strong><span>{member.fullName ? `@${member.username} · ` : ""}{member.email}</span></div><span className="status status-in-progress">{member.role}</span></div>)}</div>}
    </section>
    {roles.length > 0 && <section className="dashboard-panel admin-form-panel"><div className="panel-heading"><div><span className="section-kicker">ROLE-BASED ACCESS</span><h2>Invite team member</h2></div></div><p className="team-guidance">You can invite users below your role for this garage only.</p><form className="admin-form" onSubmit={invite}><label className="form-field">Email address<input name="email" type="email" maxLength={150} required /></label><label className="form-field">Role<select name="role" required defaultValue=""><option value="" disabled>Select role</option>{roles.map((role) => <option key={role} value={role}>{role.replaceAll("_", " ")}</option>)}</select></label><button className="button button-primary" type="submit" disabled={working}>{working && <LoaderCircle size={15} className="spin" />}{working ? "Creating invitation" : "Create invitation"}<UserPlus size={15} /></button></form></section>}
    {error && <div className="alert alert-error team-alert" role="alert"><Activity size={16} /><span>{error}</span></div>}
    {invitation && <InviteResult invitation={invitation} kicker="INVITATION SENT" className="team-invite-result" />}
  </div>;
}

function InviteResult({ invitation, kicker, className = "" }: { invitation: InvitationInfo; kicker: string; className?: string }) {
  return <div className={`invite-result ${className}`} role="status">
    <div><span className="section-kicker">{kicker}</span><strong>{invitation.email} · {invitation.role.replaceAll("_", " ")}</strong><span>{invitation.message}. The link expires {dateLabel(invitation.expiresAt)}.</span></div>
    {invitation.acceptanceUrl && <a className="button button-secondary" href={invitation.acceptanceUrl} title="Shown because INVITATION_EXPOSE_LINK is enabled">Open invite <ArrowRight size={14} /></a>}
  </div>;
}

function LoginScreen({ onSignIn, notice }: { onSignIn: (username: string, password: string) => Promise<void>; notice: string }) {
  const [working, setWorking] = useState(false);
  const [error, setError] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setWorking(true);
    setError("");
    try {
      await onSignIn(String(form.get("username")), String(form.get("password")));
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Sign in failed");
    } finally {
      setWorking(false);
    }
  }

  return <main className="auth-screen">
    <div className="auth-panel">
      <aside className="auth-brand">
        <div className="auth-brand-lockup"><span className="auth-brand-mark"><Gauge size={22} /></span><strong>pitlane</strong></div>
        <div className="auth-brand-message">
          <span className="auth-brand-kicker">SERVICE DESK <i /></span>
          <h1>Every repair<br /><em>has a next move.</em></h1>
          <p>One platform for independent garages.</p>
        </div>
        <div className="auth-brand-bottom"><span>GARAGE OPERATIONS</span><div><i /> WORKSHOP SYSTEM</div></div>
      </aside>
      <section className="auth-form-panel">
        <div className="auth-form-heading"><span className="eyebrow-line" /><span>SECURE ACCESS</span><h1>Sign in</h1><p>Use your garage account to continue.</p></div>
        {(error || notice) && <div className="alert alert-error" role="alert"><Activity size={16} /><span>{error || notice}</span></div>}
        <form className="auth-form" onSubmit={submit}>
          <label className="form-field">Username<input name="username" autoComplete="username" required autoFocus /></label>
          <label className="form-field">Password<input name="password" type="password" autoComplete="current-password" required /></label>
          <button className="button button-primary auth-submit" type="submit" disabled={working}>{working && <LoaderCircle size={16} className="spin" />}{working ? "Signing in" : "Sign in"}<ArrowRight size={16} /></button>
        </form>
      </section>
    </div>
  </main>;
}

function Overview({ data, loading, onOpenJobs, onOpenInventory, onOpenInvoices, onManageJob }: {
  data: GarageData; loading: boolean; onOpenJobs: () => void; onOpenInventory: () => void; onOpenInvoices: () => void; onManageJob: (job: RepairJob) => void;
}) {
  const activeJobs = data.jobs.filter((job) => !["COMPLETED", "CANCELLED"].includes(job.status));
  const waitingJobs = data.jobs.filter((job) => ["WAITING_FOR_PARTS", "PENDING"].includes(job.status));
  const lowStock = data.parts.filter((part) => part.lowStock).slice(0, 4);
  const latestJobs = [...data.jobs].sort((a, b) => b.id - a.id).slice(0, 5);
  const balance = data.invoices.reduce((sum, invoice) => sum + invoice.balanceDue, 0);
  return <>
    <section className="metric-grid">
      <Metric icon={<ClipboardList size={18} />} label="Open repair jobs" value={activeJobs.length} note={`${waitingJobs.length} need attention`} tone="green" loading={loading} onClick={onOpenJobs} />
      <Metric icon={<CarFront size={18} />} label="Vehicles in workshop" value={data.vehicles.filter((vehicle) => vehicle.status !== "AVAILABLE").length} note={`${data.vehicles.length} registered vehicles`} tone="blue" loading={loading} onClick={onOpenJobs} />
      <Metric icon={<Boxes size={18} />} label="Parts to reorder" value={data.parts.filter((part) => part.lowStock).length} note="At or below minimum stock" tone="orange" loading={loading} onClick={onOpenInventory} />
      <Metric icon={<CircleDollarSign size={18} />} label="Outstanding balance" value={money(balance)} note={`${data.invoices.filter((invoice) => invoice.balanceDue > 0).length} unpaid invoices`} tone="rose" loading={loading} onClick={onOpenInvoices} />
    </section>
    <div className="overview-grid">
      <section className="dashboard-panel jobs-panel"><div className="panel-heading"><div><span className="section-kicker">SHOP FLOOR</span><h2>Latest repair jobs</h2></div><button className="subtle-link" onClick={onOpenJobs}>All jobs <ArrowRight size={14} /></button></div>
        {loading ? <div className="loading-state compact"><LoaderCircle className="spin" size={20} /> Loading jobs</div> : latestJobs.length === 0 ? <EmptyCompact label="No repair jobs yet" /> : <div className="recent-job-list">{latestJobs.map((job, index) => <button className="recent-job" key={job.id} onClick={() => onManageJob(job)}><div className={`job-icon job-icon-${index % 4}`}><Wrench size={17} /></div><div className="recent-job-main"><div className="recent-job-title"><strong>{job.plateNumber}</strong><span className={statusClass(job.status)}>{job.status.replaceAll("_", " ")}</span></div><span>{job.complaint}</span></div><div className="recent-job-side"><strong>{money(job.cost)}</strong><small>{dateLabel(job.dateReceived)}</small></div><ArrowRight className="job-arrow" size={16} /></button>)}</div>}
        <div className="panel-bottom-note"><span className="live-dot" /> Synced with service desk</div>
      </section>
      <section className="dashboard-panel stock-panel"><div className="panel-heading"><div><span className="section-kicker">INVENTORY WATCH</span><h2>Stock attention</h2></div><button className="round-arrow" aria-label="View inventory" onClick={onOpenInventory}><ArrowRight size={16} /></button></div>
        {loading ? <div className="loading-state compact"><LoaderCircle className="spin" size={20} /> Checking stock</div> : lowStock.length === 0 ? <div className="stock-clear"><div><Check size={18} /></div><strong>Stock levels look good</strong><span>Nothing needs reordering right now.</span></div> : <div className="stock-list">{lowStock.map((part) => <div className="stock-item" key={part.id}><div className="part-symbol"><Boxes size={16} /></div><div className="stock-item-main"><strong>{part.name}</strong><span>{part.sku}</span></div><div className="stock-meter"><div className="stock-meter-label"><b>{part.stockQuantity}</b><span> / {part.reorderLevel} min</span></div><div className="meter-track"><i style={{ width: `${Math.max(8, Math.min(100, (part.stockQuantity / Math.max(part.reorderLevel, 1)) * 100))}%` }} /></div></div></div>)}</div>}
        <button className="inventory-link" onClick={onOpenInventory}>Open parts inventory <ArrowRight size={14} /></button>
      </section>
    </div>
    <section className="floor-strip"><div className="floor-strip-mark"><Activity size={20} /></div><div className="floor-strip-copy"><strong>Workshop pulse</strong><span>{activeJobs.length} active jobs <i /> {data.mechanics.filter((mechanic) => mechanic.active).length} available mechanics <i /> {data.customers.length} customers on file</span></div><div className="pulse-bars" aria-label="Workshop activity">{Array.from({ length: 12 }, (_, index) => <i key={index} />)}</div><div className="pulse-live"><span /> LIVE</div></section>
  </>;
}

function Metric({ icon, label, value, note, tone, loading, onClick }: { icon: ReactNode; label: string; value: ReactNode; note: string; tone: string; loading: boolean; onClick: () => void }) {
  return <button className="metric-card" onClick={onClick}><div className={`metric-icon metric-${tone}`}>{icon}</div><span className="metric-label">{label}</span><strong className="metric-value">{loading ? <LoaderCircle className="spin" size={23} /> : value}</strong><span className="metric-note">{note}</span><ArrowRight className="metric-arrow" size={16} /></button>;
}

function EmptyCompact({ label }: { label: string }) {
  return <div className="empty-compact"><div className="empty-icon"><ClipboardList size={17} /></div><span>{label}</span></div>;
}

function ResourceTable({ section, data, search, setSearch, loading, rows, onEdit, onDelete, onManageJob, onPay }: {
  section: Section; data: GarageData; search: string; setSearch: (value: string) => void; loading: boolean;
  rows: { customers: Customer[]; vehicles: Vehicle[]; mechanics: Mechanic[]; parts: SparePart[]; jobs: RepairJob[]; invoices: Invoice[] };
  onEdit: (section: Editor["section"], item: Editable) => void; onDelete: (section: Section, id: number) => void;
  onManageJob: (job: RepairJob) => void; onPay: (invoice: Invoice) => void;
}) {
  const records: TableRow[] = section === "customers" ? rows.customers.map((item) => ({ id: item.id, record: item, cells: <><div className="primary-cell">{item.fullName}</div><div className="muted-cell">{item.email}</div></> }))
    : section === "vehicles" ? rows.vehicles.map((item) => ({ id: item.id, record: item, cells: <><div className="primary-cell">{item.plateNumber}</div><div className="muted-cell">{item.year} {item.make} {item.model}</div></> }))
      : section === "mechanics" ? rows.mechanics.map((item) => ({ id: item.id, record: item, cells: <><div className="primary-cell">{item.fullName}</div><div className="muted-cell">{item.specialization || "General service"}</div></> }))
        : section === "inventory" ? rows.parts.map((item) => ({ id: item.id, record: item, cells: <><div className="primary-cell">{item.name}</div><div className="muted-cell">{item.sku}</div></> }))
          : section === "jobs" ? rows.jobs.map((item) => ({ id: item.id, record: item, cells: <><div className="primary-cell">{item.plateNumber}</div><div className="muted-cell clamp-cell">{item.complaint}</div></> }))
            : rows.invoices.map((item) => ({ id: item.id, cells: <><div className="primary-cell">{item.invoiceNumber}</div><div className="muted-cell">{item.plateNumber} · {item.customerName || "Walk-in"}</div></> }));
  const count = section === "customers" ? rows.customers.length : section === "vehicles" ? rows.vehicles.length : section === "mechanics" ? rows.mechanics.length : section === "inventory" ? rows.parts.length : section === "jobs" ? rows.jobs.length : rows.invoices.length;

  return <section className="data-panel"><div className="panel-toolbar"><div className="record-count"><strong>{count}</strong> records <span>·</span> Live from garage API</div><label className="search-control"><Search size={16} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder={`Search ${section === "inventory" ? "parts" : section}...`} /></label></div>
    <div className="table-scroll">{loading ? <div className="loading-state"><LoaderCircle className="spin" size={23} /> Loading garage data</div> : records.length === 0 ? <div className="empty-state"><div className="empty-icon"><Search size={21} /></div><strong>{search ? "No matching records" : "Nothing here yet"}</strong><span>{search ? "Try another name, plate, or status." : "Add your first record to get this workspace moving."}</span></div> : <table><thead><tr><th>{section === "jobs" ? "VEHICLE / COMPLAINT" : section === "invoices" ? "INVOICE / CUSTOMER" : section === "inventory" ? "PART / SKU" : section === "vehicles" ? "VEHICLE" : section === "mechanics" ? "MECHANIC" : "CUSTOMER"}</th><th>{section === "jobs" ? "RECEIVED" : section === "invoices" ? "ISSUED" : section === "inventory" ? "UNIT PRICE" : section === "vehicles" ? "CUSTOMER" : section === "mechanics" ? "CONTACT" : "PHONE"}</th><th>{section === "jobs" ? "STATUS" : section === "invoices" ? "TOTAL" : section === "inventory" ? "ON HAND" : section === "vehicles" ? "STATUS" : section === "mechanics" ? "AVAILABILITY" : "ADDRESS"}</th><th>{section === "jobs" ? "TARGET" : section === "invoices" ? "BALANCE" : section === "inventory" ? "STOCK" : section === "vehicles" ? "PLATE" : section === "mechanics" ? "SPECIALTY" : "EMAIL"}</th><th className="action-heading">ACTIONS</th></tr></thead><tbody>{records.map((row) => {
      if (section === "jobs") { const job = row.record as RepairJob; return <tr key={row.id}><td>{row.cells}</td><td>{dateLabel(job.dateReceived)}</td><td><span className={statusClass(job.status)}>{job.status.replaceAll("_", " ")}</span></td><td>{dateLabel(job.expectedCompletionDate)}</td><td><div className="row-actions"><button className="text-action" onClick={() => onManageJob(job)}>Manage <ArrowRight size={14} /></button><button className="icon-button" title="Edit repair job" onClick={() => onEdit("jobs", job)}><Settings2 size={16} /></button></div></td></tr>; }
      if (section === "invoices") { const invoice = rows.invoices.find((item) => item.id === row.id)!; return <tr key={row.id}><td>{row.cells}</td><td>{dateLabel(invoice.issuedAt)}</td><td>{money(invoice.totalAmount)}</td><td className={invoice.balanceDue > 0 ? "balance-due" : "balance-clear"}>{money(invoice.balanceDue)}</td><td><div className="row-actions"><span className={statusClass(invoice.status)}>{invoice.status.replaceAll("_", " ")}</span>{invoice.balanceDue > 0 && <button className="text-action" onClick={() => onPay(invoice)}>Record payment</button>}</div></td></tr>; }
      if (section === "vehicles") { const vehicle = row.record as Vehicle; return <tr key={row.id}><td>{row.cells}</td><td>{vehicle.customerName || "Unassigned"}</td><td><span className={statusClass(vehicle.status)}>{vehicle.status.replaceAll("_", " ")}</span></td><td>{vehicle.plateNumber}</td><td><RowActions onEdit={() => onEdit("vehicles", vehicle)} onDelete={() => onDelete("vehicles", vehicle.id)} /></td></tr>; }
      if (section === "customers") { const customer = row.record as Customer; return <tr key={row.id}><td>{row.cells}</td><td>{customer.phone}</td><td>{customer.address || "—"}</td><td>{customer.email}</td><td><RowActions onEdit={() => onEdit("customers", customer)} onDelete={() => onDelete("customers", customer.id)} /></td></tr>; }
      if (section === "mechanics") { const mechanic = row.record as Mechanic; return <tr key={row.id}><td>{row.cells}</td><td>{mechanic.email}<div className="muted-cell">{mechanic.phone}</div></td><td><span className={mechanic.active ? "status status-paid" : "status status-cancelled"}>{mechanic.active ? "ACTIVE" : "INACTIVE"}</span></td><td>{mechanic.specialization || "General"}</td><td><RowActions onEdit={() => onEdit("mechanics", mechanic)} onDelete={() => onDelete("mechanics", mechanic.id)} /></td></tr>; }
      const part = row.record as SparePart; return <tr key={row.id}><td>{row.cells}</td><td>{money(part.unitPrice)}</td><td>{part.stockQuantity} <span className="muted-cell">units</span></td><td>{part.lowStock ? <span className="stock-warning"><ArrowDownRight size={14} /> Reorder at {part.reorderLevel}</span> : <span className="stock-okay">In stock</span>}</td><td><RowActions onEdit={() => onEdit("inventory", part)} onDelete={() => onDelete("inventory", part.id)} /></td></tr>;
    })}</tbody></table>}</div>
    {!loading && records.length > 0 && <div className="table-footer"><span>Showing <b>{records.length}</b> {records.length === 1 ? "record" : "records"}</span><span>Updated from live data</span></div>}
  </section>;
}

function RowActions({ onEdit, onDelete }: { onEdit: () => void; onDelete: () => void }) {
  return <div className="row-actions"><button className="icon-button" onClick={onEdit} title="Edit record" aria-label="Edit record"><Settings2 size={16} /></button><button className="icon-button danger-icon" onClick={onDelete} title="Delete record" aria-label="Delete record"><Trash2 size={16} /></button></div>;
}

function EditorDialog({ editor, data, working, onClose, onSubmit }: { editor: Editor; data: GarageData; working: boolean; onClose: () => void; onSubmit: (event: FormEvent<HTMLFormElement>) => void }) {
  const fields = fieldsFor(editor.section, data);
  const item = editor.item as unknown as Record<string, unknown> | undefined;
  const label = singularTitle(editor.section);
  return <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}><section className="modal" role="dialog" aria-modal="true" aria-labelledby="editor-title"><div className="modal-header"><div><span className="section-kicker">{editor.item ? "UPDATE RECORD" : "NEW RECORD"}</span><h2 id="editor-title">{editor.item ? `Edit ${label.toLowerCase()}` : `Add ${label.toLowerCase()}`}</h2></div><button className="icon-button" aria-label="Close" onClick={onClose}><X size={19} /></button></div><form onSubmit={onSubmit} className="modal-form"><div className="form-grid">{fields.map((field) => <label className={`form-field ${["complaint", "address", "description", "diagnosis", "repairDescription"].includes(field.name) ? "form-field-wide" : ""}`} key={field.name}><span>{field.label}{field.required && <i> *</i>}</span>{field.options ? <select name={field.name} required={field.required} defaultValue={String(item?.[field.name] ?? "")}><option value="">Select {field.label.toLowerCase()}</option>{field.options.map((option) => <option value={option.value} key={option.value}>{option.label}</option>)}</select> : field.type === "checkbox" ? <span className="checkbox-field"><input name={field.name} type="checkbox" defaultChecked={item ? Boolean(item[field.name]) : true} /><span>Available for assignments</span></span> : field.type === "textarea" ? <textarea name={field.name} required={field.required} rows={3} defaultValue={String(item?.[field.name] ?? "")} /> : <input name={field.name} type={field.type || "text"} required={field.required} min={field.type === "number" ? "0" : undefined} step={["cost", "unitPrice"].includes(field.name) ? "0.01" : undefined} defaultValue={inputValue(item?.[field.name], field)} />}</label>)}</div><div className="modal-actions"><button type="button" className="button button-secondary" onClick={onClose}>Cancel</button><button type="submit" className="button button-primary" disabled={working}>{working ? <LoaderCircle className="spin" size={16} /> : <Check size={16} />}{editor.item ? "Save changes" : `Create ${label.toLowerCase()}`}</button></div></form></section></div>;
}

function JobDialog({ job, mechanics, parts, assignments, jobParts, hasInvoice, working, onClose, onAssign, onAddPart, onIssueInvoice, onRemovePart }: {
  job: RepairJob; mechanics: Mechanic[]; parts: SparePart[]; assignments: JobAssignment[]; jobParts: JobPart[]; hasInvoice: boolean; working: boolean; onClose: () => void;
  onAssign: (event: FormEvent<HTMLFormElement>) => void; onAddPart: (event: FormEvent<HTMLFormElement>) => void; onIssueInvoice: () => void; onRemovePart: (usageId: number) => void;
}) {
  const [tab, setTab] = useState<"crew" | "parts">("crew");
  return <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}><section className="modal workflow-modal" role="dialog" aria-modal="true" aria-labelledby="workflow-title"><div className="modal-header"><div><span className="section-kicker">REPAIR ORDER · #{job.id}</span><h2 id="workflow-title">{job.plateNumber}</h2><p className="modal-subtitle">{job.complaint}</p></div><button className="icon-button" aria-label="Close" onClick={onClose}><X size={19} /></button></div><div className="job-summary"><span className={statusClass(job.status)}>{job.status.replaceAll("_", " ")}</span><span>Received {dateLabel(job.dateReceived)}</span><strong>{money(job.cost)} labor</strong></div><div className="workflow-tabs"><button className={tab === "crew" ? "workflow-tab-active" : ""} onClick={() => setTab("crew")}><UsersRound size={15} /> Assigned mechanics <b>{assignments.length}</b></button><button className={tab === "parts" ? "workflow-tab-active" : ""} onClick={() => setTab("parts")}><Boxes size={15} /> Parts used <b>{jobParts.length}</b></button></div>
    {tab === "crew" ? <div className="workflow-content"><div className="workflow-list">{assignments.length === 0 ? <EmptyCompact label="No mechanics assigned yet" /> : assignments.map((assignment) => <div className="workflow-row" key={assignment.mechanicId}><div className="mini-avatar">{assignment.mechanicName.split(" ").map((name) => name[0]).slice(0, 2).join("")}</div><div><strong>{assignment.mechanicName}</strong><span>{assignment.specialization || "General service"}</span></div><span className="workflow-date">Assigned {dateLabel(assignment.assignedAt)}</span></div>)}</div>{mechanics.length > 0 ? <form className="inline-add-form" onSubmit={onAssign}><label className="form-field"><span>Assign a mechanic</span><select name="mechanicId" required defaultValue=""><option value="">Choose mechanic</option>{mechanics.filter((mechanic) => !assignments.some((assigned) => assigned.mechanicId === mechanic.id)).map((mechanic) => <option key={mechanic.id} value={mechanic.id}>{mechanic.fullName} · {mechanic.specialization || "General"}</option>)}</select></label><button type="submit" className="button button-primary" disabled={working}><Plus size={15} /> Assign</button></form> : <div className="inline-hint">Add an active mechanic before assigning this job.</div>}</div>
      : <div className="workflow-content"><div className="workflow-list">{jobParts.length === 0 ? <EmptyCompact label="No parts charged to this job" /> : jobParts.map((usage) => <div className="workflow-row" key={usage.id}><div className="part-symbol"><Boxes size={16} /></div><div><strong>{usage.partName}</strong><span>{usage.sku} · {usage.quantity} × {money(usage.unitPrice)}</span></div><b className="workflow-total">{money(usage.lineTotal)}</b>{!hasInvoice && <button className="icon-button danger-icon" aria-label="Remove used part and return stock" title="Remove and return to stock" onClick={() => onRemovePart(usage.id)}><Trash2 size={15} /></button>}</div>)}</div>{!hasInvoice && parts.length > 0 && <form className="inline-add-form" onSubmit={onAddPart}><label className="form-field"><span>Part</span><select name="sparePartId" required defaultValue=""><option value="">Choose part</option>{parts.filter((part) => part.stockQuantity > 0).map((part) => <option key={part.id} value={part.id}>{part.name} · {part.stockQuantity} available</option>)}</select></label><label className="form-field qty-field"><span>Qty</span><input name="quantity" type="number" min="1" defaultValue="1" required /></label><button type="submit" className="button button-primary" disabled={working}><Plus size={15} /> Add</button></form>}<div className="parts-total"><span>Parts subtotal</span><strong>{money(jobParts.reduce((sum, usage) => sum + usage.lineTotal, 0))}</strong></div></div>}
    <div className="modal-actions workflow-actions"><span className="workflow-footnote"><Clock3 size={14} /> Job status: {job.status.replaceAll("_", " ")}</span>{job.status === "COMPLETED" && !hasInvoice ? <button className="button button-primary" onClick={onIssueInvoice} disabled={working}><CircleDollarSign size={16} /> Issue invoice</button> : <button className="button button-secondary" onClick={onClose}>Done</button>}</div>
  </section></div>;
}

function PaymentDialog({ invoice, working, onClose, onSubmit }: { invoice: Invoice; working: boolean; onClose: () => void; onSubmit: (event: FormEvent<HTMLFormElement>) => void }) {
  return <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}><section className="modal payment-modal" role="dialog" aria-modal="true" aria-labelledby="payment-title"><div className="modal-header"><div><span className="section-kicker">PAYMENT · {invoice.invoiceNumber}</span><h2 id="payment-title">Record payment</h2></div><button className="icon-button" aria-label="Close" onClick={onClose}><X size={19} /></button></div><div className="payment-balance"><span>Remaining balance</span><strong>{money(invoice.balanceDue)}</strong><small>Total invoice {money(invoice.totalAmount)} · Paid {money(invoice.amountPaid)}</small></div><form onSubmit={onSubmit} className="modal-form"><div className="form-grid"><label className="form-field"><span>Amount *</span><input name="amount" type="number" min="0.01" max={invoice.balanceDue} step="0.01" required autoFocus /></label><label className="form-field"><span>Payment method *</span><select name="paymentMethod" required defaultValue=""><option value="">Select method</option><option value="CASH">Cash</option><option value="CARD">Card</option><option value="BANK_TRANSFER">Bank transfer</option><option value="MOBILE_MONEY">Mobile money</option></select></label><label className="form-field form-field-wide"><span>Reference</span><input name="reference" placeholder="Transaction or receipt number" /></label></div><div className="modal-actions"><button type="button" className="button button-secondary" onClick={onClose}>Cancel</button><button type="submit" className="button button-primary" disabled={working}>{working ? <LoaderCircle className="spin" size={16} /> : <Check size={16} />} Record payment</button></div></form></section></div>;
}

function fieldsFor(section: Editor["section"], data: GarageData): Field[] {
  if (section === "customers") return [{ name: "fullName", label: "Full name", required: true }, { name: "phone", label: "Phone number", required: true }, { name: "email", label: "Email address", type: "email", required: true }, { name: "address", label: "Address", type: "textarea" }];
  if (section === "vehicles") return [{ name: "plateNumber", label: "Plate number", required: true }, { name: "make", label: "Make", required: true }, { name: "model", label: "Model", required: true }, { name: "year", label: "Year", type: "number", required: true }, { name: "color", label: "Color" }, { name: "customerId", label: "Customer", options: data.customers.map((row) => ({ value: row.id, label: row.fullName })) }];
  if (section === "mechanics") return [{ name: "fullName", label: "Full name", required: true }, { name: "phone", label: "Phone number", required: true }, { name: "email", label: "Email address", type: "email", required: true }, { name: "specialization", label: "Specialization" }, { name: "active", label: "Availability", type: "checkbox" }];
  if (section === "inventory") return [{ name: "sku", label: "SKU", required: true }, { name: "name", label: "Part name", required: true }, { name: "unitPrice", label: "Unit price (RWF)", type: "number", required: true }, { name: "stockQuantity", label: "Quantity on hand", type: "number", required: true }, { name: "reorderLevel", label: "Reorder level", type: "number" }, { name: "description", label: "Description", type: "textarea" }];
  return [{ name: "vehicleId", label: "Vehicle", required: true, options: data.vehicles.map((vehicle) => ({ value: vehicle.id, label: `${vehicle.plateNumber} · ${vehicle.make} ${vehicle.model}` })) }, { name: "complaint", label: "Customer complaint", type: "textarea", required: true }, { name: "diagnosis", label: "Diagnosis", type: "textarea" }, { name: "repairDescription", label: "Repair description", type: "textarea" }, { name: "cost", label: "Labor cost (RWF)", type: "number", required: true }, { name: "status", label: "Status", options: ["PENDING", "DIAGNOSING", "IN_PROGRESS", "WAITING_FOR_PARTS", "COMPLETED", "CANCELLED"].map((value) => ({ value, label: value.replaceAll("_", " ") })) }, { name: "expectedCompletionDate", label: "Expected completion", type: "datetime-local" }];
}

function inputValue(value: unknown, field: Field) {
  if (value === null || value === undefined) return "";
  if (field.type === "datetime-local" && typeof value === "string") return value.slice(0, 16);
  return String(value);
}

function singularTitle(section: Section) {
  return ({ jobs: "Repair job", customers: "Customer", vehicles: "Vehicle", mechanics: "Mechanic", inventory: "Spare part", invoices: "Invoice", overview: "Record", team: "Team member" })[section];
}

function sectionDescription(section: Section) {
  return ({ overview: "Your workshop at a glance.", jobs: "Track work orders from check-in through pickup.", customers: "Keep customer contacts and vehicle ownership together.", vehicles: "Browse the vehicles currently known to your workshop.", mechanics: "Manage technician availability and job assignments.", inventory: "Monitor stock, prices, and parts used on repair jobs.", invoices: "Review repair charges, payments, and balances due.", team: "Invite and manage access for your garage team." })[section];
}
