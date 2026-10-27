import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import { useT } from '../i18n/languageContext';

export default function CustomerBookingsPage() {
  const { t } = useT();
  const [bookings, setBookings] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiRequest('/api/customer/bookings')
      .then((data) => setBookings(data || []))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="card card-wide">
      <h1>{t('My bookings')}</h1>
      {error && <div className="form-error">{error}</div>}
      {loading && <p>{t('Loading bookings...')}</p>}
      {!loading && bookings.length === 0 && !error && <p>{t('No bookings yet.')}</p>}
      {bookings.map((b) => (
        <div key={b.id} className="order-card">
          <div className="order-head">
            <strong>{t('Booking')} #{b.id}</strong>
            <span className={'badge badge-' + b.status}>{t(b.status)}</span>
          </div>
          <p className="small">
            {t('Service')}: {b.serviceName}
            <br />
            {t('Requested time')}: {b.appointmentTime ? new Date(b.appointmentTime).toLocaleString() : '-'}
            {b.suggestedTime && (
              <>
                <br />
                {t('Suggested time')}: {new Date(b.suggestedTime).toLocaleString()}
              </>
            )}
          </p>
        </div>
      ))}
    </div>
  );
}
