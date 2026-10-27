import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiRequest } from '../api/client';
import { useT } from '../i18n/languageContext';

export default function CustomerBookPage() {
  const { t } = useT();
  const [providers, setProviders] = useState([]);
  const [providerId, setProviderId] = useState('');
  const [serviceName, setServiceName] = useState('');
  const [when, setWhen] = useState('');
  const [error, setError] = useState('');
  const [done, setDone] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    apiRequest('/api/providers/ranked')
      .then((data) => setProviders(data || []))
      .catch((err) => setError(err.message));
  }, []);

  function idOf(p) {
    return p.id ?? p.providerId ?? p.serviceProviderId;
  }

  async function submit(event) {
    event.preventDefault();
    setError('');
    setDone('');
    if (!providerId || !when) {
      setError(t('Please choose a provider and a date and time.'));
      return;
    }
    setBusy(true);
    try {
      const b = await apiRequest('/api/customer/bookings', {
        method: 'POST',
        body: { providerId: Number(providerId), serviceName, appointmentTime: when + ':00' },
      });
      setDone(t('Booking request sent.') + ' #' + b.id);
      setServiceName('');
      setWhen('');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="card">
      <h1>{t('Book a provider')}</h1>
      <form onSubmit={submit}>
        <div className="field">
          <label htmlFor="book-provider">{t('Provider')}</label>
          <select id="book-provider" value={providerId} onChange={(e) => setProviderId(e.target.value)}>
            <option value="">{t('Choose a provider')}</option>
            {providers.map((p) => (
              <option key={idOf(p)} value={idOf(p)}>
                {(p.fullName || p.name || 'Provider') + (p.serviceCategory ? ' - ' + p.serviceCategory : '')}
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="book-service">{t('Service needed (optional)')}</label>
          <input id="book-service" type="text" value={serviceName}
            onChange={(e) => setServiceName(e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="book-time">{t('Date and time')}</label>
          <input id="book-time" type="datetime-local" value={when}
            onChange={(e) => setWhen(e.target.value)} />
        </div>
        {error && <div className="form-error">{error}</div>}
        {done && <div className="form-success">{done} - <Link to="/customer/bookings">{t('My bookings')}</Link></div>}
        <button className="btn" id="book-submit" type="submit" disabled={busy}>
          {busy ? t('Please wait...') : t('Send booking request')}
        </button>
      </form>
    </div>
  );
}
