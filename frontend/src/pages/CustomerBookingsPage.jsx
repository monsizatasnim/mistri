import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import { useT } from '../i18n/languageContext';

function BookingCard({ booking, onChanged, onError }) {
  const { t } = useT();
  const [busy, setBusy] = useState(false);

  async function act(path) {
    setBusy(true);
    onError('');
    try {
      await apiRequest('/api/customer/bookings/' + booking.id + path, { method: 'POST' });
      await onChanged();
    } catch (err) {
      onError(err.message);
    } finally {
      setBusy(false);
    }
  }

  const s = booking.status;
  const canCancel = s === 'PENDING' || s === 'CONFIRMED' || s === 'SUGGESTED_ALTERNATIVE';

  return (
    <div className="order-card">
      <div className="order-head">
        <strong>{t('Booking')} #{booking.id}</strong>
        <span className={'badge badge-' + s}>{t(s)}</span>
      </div>
      <p className="small">
        {t('Service')}: {booking.serviceName}
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
        {s === 'SUGGESTED_ALTERNATIVE' && (
          <button className="btn-small-dark" disabled={busy} onClick={() => act('/accept-suggestion')}>
            {t('Use suggested time')}
          </button>
        )}
        {canCancel && (
          <button className="btn-small-red" disabled={busy} onClick={() => act('/cancel')}>
            {t('Cancel booking')}
          </button>
        )}
      </div>
    </div>
  );
}

export default function CustomerBookingsPage() {
  const { t } = useT();
  const [bookings, setBookings] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  async function load() {
    try {
      setBookings((await apiRequest('/api/customer/bookings')) || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  return (
    <div className="card card-wide">
      <h1>{t('My bookings')}</h1>
      {error && <div className="form-error">{error}</div>}
      {loading && <p>{t('Loading bookings...')}</p>}
      {!loading && bookings.length === 0 && !error && <p>{t('No bookings yet.')}</p>}
      {bookings.map((b) => (
        <BookingCard key={b.id} booking={b} onChanged={load} onError={setError} />
      ))}
    </div>
  );
}
