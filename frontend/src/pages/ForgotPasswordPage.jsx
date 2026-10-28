import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiRequest } from '../api/client';
import TextField from '../components/TextField.jsx';
import { useT } from '../i18n/languageContext.js';

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const { t } = useT();
  const [identifier, setIdentifier] = useState('');
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setFieldErrors({});
    setLoading(true);
    try {
      const data = await apiRequest('/api/auth/forgot-password', {
        method: 'POST',
        body: { identifier: identifier.trim() },
      });
      navigate('/reset-password', {
        state: { resetCode: data && data.resetCode ? data.resetCode : '' },
      });
    } catch (err) {
      setError(err.message);
      setFieldErrors(err.fieldErrors || {});
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <div className="card">
        <h1>{t('Forgot password')}</h1>
        {error && <div className="form-error">{t(error)}</div>}
        <form onSubmit={handleSubmit}>
          <TextField id="identifier" label={t('Email or phone')} value={identifier}
            onChange={(e) => setIdentifier(e.target.value)} error={fieldErrors.identifier} />
          <button className="btn" type="submit" disabled={loading}>
            {loading ? t('Please wait...') : t('Get reset code')}
          </button>
        </form>
        <div className="links">
          <Link to="/login">{t('Back to log in')}</Link>
        </div>
      </div>
    </div>
  );
}
