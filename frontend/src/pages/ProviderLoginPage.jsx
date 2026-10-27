import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/authContext';
import { useT } from '../i18n/languageContext';

export default function ProviderLoginPage() {
  const { t } = useT();
  const { loginProvider } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      await loginProvider(email.trim(), password);
      navigate('/provider/price', { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <div className="card">
        <h1>{t('Provider log in')}</h1>
        {error && <div className="form-error" id="login-error">{t(error)}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="email">{t('Email')}</label>
            <input id="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <div className="field">
            <label htmlFor="password">{t('Password')}</label>
            <input id="password" type="password" value={password}
                   onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <button className="btn" id="login-submit" type="submit" disabled={loading}>
            {loading ? t('Please wait...') : t('Log in')}
          </button>
        </form>
      </div>
    </div>
  );
}
