import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import { useT } from '../i18n/languageContext';

const COMPLAINT_CATEGORIES = [
  ['NO_SHOW', 'Provider did not show up'],
  ['QUALITY_ISSUE', 'Poor quality of work'],
  ['PAYMENT_ISSUE', 'Payment problem'],
  ['SAFETY_CONCERN', 'Safety concern'],
  ['OTHER', 'Other'],
];

const PAYMENT_METHODS = [
  ['CASH', 'Cash'],
  ['BKASH', 'bKash'],
];

function ComplaintForm({ booking, onDone }) {
  const { t } = useT();
  const [category, setCategory] = useState('');
  const [description, setDescription] = useState('');
  const [sending, setSending] = useState(false);
  const [formError, setFormError] = useState('');

  async function submit() {
    if (!category) {
      setFormError(t('Please choose a category.'));
      return;
    }
    if (!description.trim()) {
      setFormError(t('Please describe the problem.'));
      return;
    }
    setSending(true);
    setFormError('');
    try {
      await apiRequest('/api/customer/complaints', {
        method: 'POST',
        body: { bookingId: String(booking.id), category, description: description.trim() },
      });
      onDone(t('Your complaint was sent. The admin will review it.'));
    } catch (err) {
      const firstField = err.fieldErrors ? Object.values(err.fieldErrors)[0] : '';
      setFormError(firstField || err.message);
    } finally {
      setSending(false);
    }
  }

  return (
    <div style={{ marginTop: '12px' }}>
      <p className="small">{t('What went wrong?')}</p>
      <select
        value={category}
        onChange={(e) => setCategory(e.target.value)}
        style={{ width: '100%', marginBottom: '8px' }}
      >
        <option value="">{t('Choose a category')}</option>
        {COMPLAINT_CATEGORIES.map(([value, label]) => (
          <option key={value} value={value}>{t(label)}</option>
        ))}
      </select>
      <textarea
        rows={4}
        maxLength={2000}
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        placeholder={t('Describe the problem (max 2000 characters)')}
        style={{ width: '100%', marginBottom: '8px' }}
      />
      {formError && <div className="form-error">{formError}</div>}
      <div className="order-actions">
        <button className="btn-small-dark" disabled={sending} onClick={submit}>
          {sending ? t('Please wait...') : t('Send complaint')}
        </button>
        <button className="btn-small-red" disabled={sending} onClick={() => onDone('')}>
          {t('Close')}
        </button>
      </div>
    </div>
  );
}

function PaymentForm({ booking, onDone }) {
  const { t } = useT();
  const [method, setMethod] = useState('CASH');
  const [amount, setAmount] = useState('');
  const [sending, setSending] = useState(false);
  const [formError, setFormError] = useState('');

  async function submit() {
    const value = Number(amount);
    if (!amount || Number.isNaN(value) || value <= 0) {
      setFormError(t('Please enter a valid amount.'));
      return;
    }
    setSending(true);
    setFormError('');
    try {
      const data = await apiRequest(
        '/api/customer/payments?bookingId=' + booking.id +
          '&amount=' + value +
          '&paymentMethod=' + method,
        { method: 'POST' }
      );
      onDone(t('Payment recorded. Status:') + ' ' + (data && data.status ? data.status : ''));
    } catch (err) {
      setFormError(err.message);
    } finally {
      setSending(false);
    }
  }

  return (
    <div style={{ marginTop: '12px' }}>
      <p className="small">{t('Pay your provider')}</p>
      <select
        value={method}
        onChange={(e) => setMethod(e.target.value)}
        style={{ width: '100%', marginBottom: '8px' }}
      >
        {PAYMENT_METHODS.map(([value, label]) => (
          <option key={value} value={value}>{t(label)}</option>
        ))}
      </select>
      <input
        type="number"
        min="1"
        value={amount}
        onChange={(e) => setAmount(e.target.value)}
        placeholder={t('Amount')}
        style={{ width: '100%', marginBottom: '8px' }}
      />
      {formError && <div className="form-error">{formError}</div>}
      <div className="order-actions">
        <button className="btn-small-dark" disabled={sending} onClick={submit}>
          {sending ? t('Please wait...') : t('Confirm payment')}
        </button>
        <button className="btn-small-red" disabled={sending} onClick={() => onDone('')}>
          {t('Close')}
        </button>
      </div>
    </div>
  );
}

function BookingCard({ booking, paid, onChanged, onError }) {
  const { t } = useT();
  const [busy, setBusy] = useState(false);
  const [showComplaint, setShowComplaint] = useState(false);
  const [showPayment, setShowPayment] = useState(false);
  const [notice, setNotice] = useState('');

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

  function complaintDone(message) {
    setShowComplaint(false);
    setNotice(message);
  }

  async function paymentDone(message) {
    setShowPayment(false);
    setNotice(message);
    // reload so the card shows "Paid" instead of "Pay now"
    await onChanged();
  }

  const s = booking.status;
  const canCancel = s === 'PENDING' || s === 'CONFIRMED' || s === 'SUGGESTED_ALTERNATIVE';
  const canPay = s === 'COMPLETED' && !paid;

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
        {paid && (
          <span className="badge badge-COMPLETED">{'\u2714'} {t('Paid')}</span>
        )}
        {canPay && !showPayment && (
          <button className="btn-small-dark" onClick={() => { setNotice(''); setShowPayment(true); }}>
            {t('Pay now')}
          </button>
        )}
        {!showComplaint && (
          <button className="btn-small-dark" onClick={() => { setNotice(''); setShowComplaint(true); }}>
            {t('Report a problem')}
          </button>
        )}
      </div>
      {notice && <p className="small" style={{ color: 'green' }}>{notice}</p>}
      {showPayment && <PaymentForm booking={booking} onDone={paymentDone} />}
      {showComplaint && <ComplaintForm booking={booking} onDone={complaintDone} />}
    </div>
  );
}

export default function CustomerBookingsPage() {
  const { t } = useT();
  const [bookings, setBookings] = useState([]);
  const [paidIds, setPaidIds] = useState([]);
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
    // payments are loaded separately, so a payment error never hides the bookings
    try {
      const payments = (await apiRequest('/api/customer/payments')) || [];
      const ids = payments
        .filter((p) => !/fail/i.test(String(p.status || '')))
        .map((p) => p.bookingId);
      setPaidIds(ids);
    } catch {
      setPaidIds([]);
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
        <BookingCard
          key={b.id}
          booking={b}
          paid={paidIds.includes(b.id)}
          onChanged={load}
          onError={setError}
        />
      ))}
    </div>
  );
}