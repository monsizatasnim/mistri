import { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/authContext';
import { errorMessage } from './format';
import './admin.css';

export default function AdminLoginPage() {
  const { user, loginAdmin } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const destination = location.state && location.state.from ? location.state.from.pathname : '/admin';

  if (user && user.role === 'ROLE_ADMIN') {
    return <Navigate to={destination} replace />;
  }

  async function submit(event) {
    event.preventDefault();
    if (!identifier.trim() || !password) {
      setError('Enter your email and password.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      await loginAdmin(identifier.trim(), password);
      navigate(destination, { replace: true });
    } catch (err) {
      setError(errorMessage(err));
      setBusy(false);
    }
  }

  return (
    <div className="adm-root">
      <div className="adm-login-wrap">
        <form className="adm-login" onSubmit={submit} noValidate>
          <h1>Admin login</h1>
          <p className="adm-muted">Sign in to manage the Mistri platform.</p>
          <div className="adm-form">
            {error && (
              <div className="adm-notice adm-notice-error" role="alert">
                {error}
              </div>
            )}
            <div className="adm-field">
              <label htmlFor="adm-email">Email</label>
              <input
                id="adm-email"
                type="email"
                autoComplete="username"
                value={identifier}
                onChange={(event) => setIdentifier(event.target.value)}
              />
            </div>
            <div className="adm-field">
              <label htmlFor="adm-password">Password</label>
              <input
                id="adm-password"
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
            </div>
            <button type="submit" className="adm-btn adm-btn-primary" disabled={busy}>
              {busy ? 'Signing in...' : 'Sign in'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
