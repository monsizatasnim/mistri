import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import { formRequest } from '../api/formRequest';
import TextField from '../components/TextField.jsx';
import { useT } from '../i18n/languageContext';

const STATUS_TEXT = {
  PENDING: 'Your documents are waiting for admin approval.',
  VERIFIED: 'You are approved. Customers can find you.',
  REJECTED: 'Your documents were rejected.',
};

export default function ProviderProfileEditPage() {
  const { t } = useT();
  const [info, setInfo] = useState(null);
  const [form, setForm] = useState({ fullName: '', phone: '', serviceCategory: '', serviceDescription: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    apiRequest('/api/service-provider/verification-status')
      .then((d) => {
        setInfo(d);
        setForm({
          fullName: d.fullName || '',
          phone: d.phone || '',
          serviceCategory: d.serviceCategory || '',
          serviceDescription: d.serviceDescription || '',
        });
      })
      .catch((err) => setError(err.message));
  }, []);

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setSaving(true);
    try {
      await formRequest('/api/service-provider/profile', 'PUT', form);
      setSuccess('Saved.');
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="card">
      <h1>{t('Verification and profile')}</h1>
      {info && (
        <div className="status-line">
          <span className={'badge badge-' + info.verificationStatus}>{t(info.verificationStatus)}</span>
          <span className="small">{t(STATUS_TEXT[info.verificationStatus] || '')}</span>
        </div>
      )}
      {info && info.verificationStatus === 'REJECTED' && info.rejectionReason && (
        <div className="form-error">{t('Reason for rejection')}: {info.rejectionReason}</div>
      )}
      {success && <div className="form-success">{t(success)}</div>}
      {error && <div className="form-error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <TextField id="fullName" label={t('Full name')} value={form.fullName} onChange={handleChange} />
        <TextField id="phone" label={t('Phone number')} value={form.phone} onChange={handleChange} />
        <TextField id="serviceCategory" label={t('Service type (for example Electrician)')}
          value={form.serviceCategory} onChange={handleChange} />
        <TextField id="serviceDescription" label={t('Service description')}
          value={form.serviceDescription} onChange={handleChange} />
        <button className="btn" type="submit" disabled={saving}>
          {saving ? t('Please wait...') : t('Save')}
        </button>
      </form>
    </div>
  );
}
