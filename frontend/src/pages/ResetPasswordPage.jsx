import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { apiRequest } from '../api/client';
import TextField from '../components/TextField.jsx';
import { useT } from '../i18n/languageContext.js';

export default function ResetPasswordPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { t } = useT();
  const shownCode = location.state ? location.state.resetCode : '';
  const [form, setForm] = useState({
    resetCode: shownCode || '',
    newPassword: '',
    confirmPassword: '',
  });
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [loading, setLoading] = useState(false);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setFieldErrors({});

    if (form.newPassword !== form.confirmPassword) {
      setFieldErrors({ confirmPassword: t('Passwords do not match') });
      setError('Please fix the highlighted fields.');
      return;
    }

    setLoading(true);
    try {
      await apiRequest('/api/auth/reset-password', {
        method: 'POST',
        body: { resetCode: form.resetCode.trim(), newPassword: form.newPassword },
      });
      navigate('/login', {
        replace: true,
        state: { message: 'Password changed. Please log in with your new password.' },
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
        <h1>{t('Reset password')}</h1>
        {shownCode && (
          <div className="form-success">
            {t('Your reset code is')} <strong>{shownCode}</strong>. {t('It is shown here only because email sending is not set up yet.')}
          </div>
        )}
        {error && <div className="form-error">{t(error)}</div>}
        <form onSubmit={handleSubmit}>
          <TextField id="resetCode" label={t('Reset code')} value={form.resetCode}
            onChange={handleChange} error={fieldErrors.resetCode} />
          <TextField id="newPassword" label={t('New password')} type="password" value={form.newPassword}
            onChange={handleChange} error={fieldErrors.newPassword} />
          <TextField id="confirmPassword" label={t('Confirm new password')} type="password" value={form.confirmPassword}
            onChange={handleChange} error={fieldErrors.confirmPassword} />
          <button className="btn" type="submit" disabled={loading}>
            {loading ? t('Please wait...') : t('Change password')}
          </button>
        </form>
        <div className="links">
          <Link to="/login">{t('Back to log in')}</Link>
        </div>
      </div>
    </div>
  );
}
