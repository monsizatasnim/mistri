import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth, homePathForRole } from '../auth/authContext';
import { useT } from '../i18n/languageContext';

export default function LoginPage() {
  const { t } = useT();
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const successMessage = location.state ? location.state.message : '';
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await login(identifier.trim(), password);
      navigate(homePathForRole(data.role), { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <div className="card">
        <h1>{t('Log in')}</h1>
        {successMessage && <div className="form-success">{t(successMessage)}</div>}
        {error && <div className="form-error">{t(error)}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="identifier">{t('Email or phone')}</label>
            <input
              id="identifier"
              value={identifier}
              onChange={(e) => setIdentifier(e.target.value)}
              required
            />
          </div>
          <div className="field">
            <label htmlFor="password">{t('Password')}</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button className="btn" type="submit" disabled={loading}>
            {loading ? t('Please wait...') : t('Log in')}
          </button>
        </form>
        <div className="links">
          <Link to="/forgot-password">{t('Forgot your password?')}</Link>
          <Link to="/register/customer">{t('Create a customer account')}</Link>
          <Link to="/register/shop-owner">{t('Register your shop')}</Link>
          <Link to="/register/provider">{t('Register as a service provider')}</Link>
        </div>
      </div>
    </div>
  );
}
