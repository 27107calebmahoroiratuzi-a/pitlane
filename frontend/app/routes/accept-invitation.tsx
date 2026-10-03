import { useEffect, useState, type FormEvent } from "react";
import { ArrowRight, Gauge, LoaderCircle } from "lucide-react";
import { useNavigate } from "react-router";
import { acceptInvitation } from "../api";

export function meta() {
  return [{ title: "Accept invitation | Pitlane" }];
}

export default function AcceptInvitation() {
  const navigate = useNavigate();
  const [token, setToken] = useState("");
  const [tokenReady, setTokenReady] = useState(false);
  const [working, setWorking] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    setToken(new URLSearchParams(window.location.search).get("token") || "");
    setTokenReady(true);
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token) {
      setError("This invitation link is missing its token.");
      return;
    }
    const form = new FormData(event.currentTarget);
    setWorking(true);
    setError("");
    try {
      await acceptInvitation(token, String(form.get("username")), String(form.get("password")));
      navigate("/", { replace: true });
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Invitation could not be accepted");
    } finally {
      setWorking(false);
    }
  }

  return <main className="auth-screen">
    <div className="auth-panel">
      <aside className="auth-brand">
        <div className="auth-brand-lockup"><span className="auth-brand-mark"><Gauge size={22} /></span><strong>pitlane</strong></div>
        <div className="auth-brand-message"><span className="auth-brand-kicker">GARAGE ACCESS <i /></span><h1>Every repair<br /><em>has a next move.</em></h1></div>
        <div className="auth-brand-bottom"><span>GARAGE OPERATIONS</span><div><i /> WORKSHOP SYSTEM</div></div>
      </aside>
      <section className="auth-form-panel">
        <div className="auth-form-heading"><span className="eyebrow-line" /><span>INVITATION</span><h1>Join your garage</h1><p>Create your account to accept access.</p></div>
        {error && <div className="alert alert-error" role="alert"><span>{error}</span></div>}
        {!tokenReady ? <div className="loading-state"><LoaderCircle className="spin" size={20} /> Checking invitation</div> : <form className="auth-form" onSubmit={submit}>
          <label className="form-field">Username<input name="username" autoComplete="username" minLength={3} maxLength={50} required autoFocus /></label>
          <label className="form-field">Password<input name="password" type="password" autoComplete="new-password" minLength={12} maxLength={72} required /></label>
          <button className="button button-primary auth-submit" type="submit" disabled={working || !token}>{working && <LoaderCircle size={16} className="spin" />}{working ? "Accepting invitation" : "Accept invitation"}<ArrowRight size={16} /></button>
        </form>}
      </section>
    </div>
  </main>;
}