import { useEffect, useState, type FormEvent } from "react";
import { ArrowRight, Gauge, LoaderCircle, MailWarning } from "lucide-react";
import { useNavigate } from "react-router";
import { acceptInvitation, previewInvitation, type InvitationPreview } from "../api";

export function meta() {
  return [{ title: "Welcome | Pitlane" }];
}

type Status =
  | { kind: "checking" }
  | { kind: "invalid"; message: string }
  | { kind: "ready"; token: string; invitation: InvitationPreview };

function roleLabel(role: string) {
  return role.replaceAll("_", " ").toLowerCase().replace(/^\w/, (letter) => letter.toUpperCase());
}

function expiryLabel(value: string) {
  return new Date(value).toLocaleString(undefined, { dateStyle: "medium", timeStyle: "short" });
}

export default function AcceptInvitation() {
  const navigate = useNavigate();
  const [status, setStatus] = useState<Status>({ kind: "checking" });
  const [working, setWorking] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const token = new URLSearchParams(window.location.search).get("token") || "";
    if (!token) {
      setStatus({ kind: "invalid", message: "This invitation link is incomplete. Open the link from your invitation email again." });
      return;
    }
    previewInvitation(token)
      .then((invitation) => setStatus({ kind: "ready", token, invitation }))
      .catch((cause: unknown) => setStatus({
        kind: "invalid",
        message: cause instanceof Error ? cause.message : "This invitation could not be checked",
      }));
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (status.kind !== "ready") return;
    const form = new FormData(event.currentTarget);
    const password = String(form.get("password"));
    if (password !== String(form.get("confirmPassword"))) {
      setError("The two passwords do not match.");
      return;
    }
    setWorking(true);
    setError("");
    try {
      await acceptInvitation({
        token: status.token,
        fullName: String(form.get("fullName")).trim(),
        phone: String(form.get("phone")).trim(),
        username: String(form.get("username")).trim(),
        password,
      });
      navigate("/", { replace: true });
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Your account could not be created");
      setWorking(false);
    }
  }

  const invitation = status.kind === "ready" ? status.invitation : null;

  return <main className="auth-screen">
    <div className="auth-panel">
      <aside className="auth-brand">
        <div className="auth-brand-lockup"><span className="auth-brand-mark"><Gauge size={22} /></span><strong>pitlane</strong></div>
        <div className="auth-brand-message">
          <span className="auth-brand-kicker">YOU'RE INVITED <i /></span>
          {invitation
            ? <><h1>Welcome to<br /><em>{invitation.garageName}</em></h1><p>{invitation.invitedBy} invited you to join as {roleLabel(invitation.role)}.</p></>
            : <h1>Every repair<br /><em>has a next move.</em></h1>}
        </div>
        <div className="auth-brand-bottom"><span>GARAGE OPERATIONS</span><div><i /> WORKSHOP SYSTEM</div></div>
      </aside>
      <section className="auth-form-panel">
        {status.kind === "checking" && <div className="loading-state"><LoaderCircle className="spin" size={20} /> Checking your invitation</div>}

        {status.kind === "invalid" && <>
          <div className="auth-form-heading"><span className="eyebrow-line" /><span>INVITATION</span><h1>Link unavailable</h1></div>
          <div className="alert alert-error" role="alert"><MailWarning size={16} /><span>{status.message}</span></div>
          <a className="button button-secondary auth-submit" href="/">Go to sign in <ArrowRight size={16} /></a>
        </>}

        {invitation && <>
          <div className="auth-form-heading"><span className="eyebrow-line" /><span>ACCOUNT SETUP</span><h1>Complete your profile</h1><p>Signing up as <strong>{invitation.email}</strong>. This link expires {expiryLabel(invitation.expiresAt)}.</p></div>
          {error && <div className="alert alert-error" role="alert"><span>{error}</span></div>}
          <form className="auth-form" onSubmit={submit}>
            <div className="auth-form-row">
              <label className="form-field">Full name<input name="fullName" autoComplete="name" minLength={2} maxLength={120} required autoFocus /></label>
              <label className="form-field">Phone <span className="field-hint">optional</span><input name="phone" type="tel" autoComplete="tel" maxLength={30} pattern="\+?[0-9 \(\)\-]{7,30}" title="7 to 30 digits, spaces, dashes or brackets" /></label>
            </div>
            <label className="form-field">Username<input name="username" autoComplete="username" minLength={3} maxLength={50} pattern="[A-Za-z0-9._\-]+" title="Letters, numbers, dots, dashes and underscores" required /></label>
            <div className="auth-form-row">
              <label className="form-field">Password<input name="password" type="password" autoComplete="new-password" minLength={12} maxLength={72} required /></label>
              <label className="form-field">Confirm password<input name="confirmPassword" type="password" autoComplete="new-password" minLength={12} maxLength={72} required /></label>
            </div>
            <span className="field-hint">Use at least 12 characters.</span>
            <button className="button button-primary auth-submit" type="submit" disabled={working}>{working && <LoaderCircle size={16} className="spin" />}{working ? "Setting up your account" : "Continue to dashboard"}<ArrowRight size={16} /></button>
          </form>
        </>}
      </section>
    </div>
  </main>;
}
