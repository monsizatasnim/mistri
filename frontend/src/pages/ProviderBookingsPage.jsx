import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import { useT } from '../i18n/languageContext';

function BookingRow({ booking, onChanged, onError }) {
  const { t } = useT();
  const [busy, setBusy] = useState(false);
  const [newTime, setNewTime] = useState('');

  async function act(path, body) {
    setBusy(true);
    onError('');
    try {
      await apiRequest('/api/provider/bookings/' + booking.id + path, { method: 'POST', body });
      await onChanged();
    } catch (err) {
      onError(err.message);
    } finally {
      setBusy(false);
    }
  }

  function suggest() {
    if (!newTime) {
      onError(t('Please choose a new date and time.'));
      return;
    }
    act('/suggest-time', { suggestedTime: newTime + ':00' });
  }

  return (
    <div className="order-card">
      <div className="order-head">
        <strong>{t('Booking')} #{booking.id}</strong>
        <span className={'badge badge-' + booking.status}>{t(booking.status)}</span>
      </div>
      <p className="small">
        {t('Service')}: {booking.serviceName} - {t('Customer')} #{booking.customerId}
        <br />
        {t('Requested time')}: {booking.appointmentTime ? new Date(booking.appointmentTime).toLocaleString() : '-'}
        {booking.suggestedTime && (
          <>
            <br />
            {t('Suggested time')}: {new Date(booking.suggestedTime).toLocaleString()}
          </>
        )}
      </p>

      <div className="order-actions">
        {booking.status === 'PENDING' && (
          <>
            <button className="btn-small-dark" disabled={busy} onClick={() => act('/accept')}>
              {t('Accept')}
            </button>
            <button className="btn-small-red" disabled={busy} onClick={() => act('/decline')}>
              {t('Decline')}
            </button>
          </>
        )}
        {booking.status === 'CONFIRMED' && (
          <button className="btn-small-dark" disabled={busy} onClick={() => act('/start')}>
            {t('Start job')}
          </button>
        )}
        {booking.status === 'IN_PROGRESS' && (
          <button className="btn-small-dark" disabled={busy} onClick={() => act('/complete')}>
            {t('Complete job')}
          </button>
        )}
      </div>

      {booking.status === 'PENDING' && (
        <div className="order-actions">
          <input id={'suggest-time-' + booking.id} type="datetime-local" value={newTime}
            onChange={(event) => setNewTime(event.target.value)} />
          <button className="btn-small-dark" disabled={busy} onClick={suggest}>
            {t('Suggest time')}
          </button>
        </div>
      )}
    </div>
  );
}

export default function ProviderBookingsPage() {
  const { t } = useT();
  const [bookings, setBookings] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  async function load() {
    try {
      setBookings((await apiRequest('/api/provider/bookings')) || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  return (
    <div className="card card-wide">
      <h1>{t('Booking requests')}</h1>
      {error && <div className="form-error">{error}</div>}
      {loading && <p>{t('Loading bookings...')}</p>}
      {!loading && bookings.length === 0 && !error && <p>{t('No booking requests yet.')}</p>}
      {bookings.map((b) => (
        <BookingRow key={b.id} booking={b} onChanged={load} onError={setError} />
      ))}
    </div>
  );
}
