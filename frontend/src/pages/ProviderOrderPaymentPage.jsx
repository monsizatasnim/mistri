import { useState } from 'react';
import { apiRequest } from '../api/client';
import { useT } from '../i18n/languageContext';

export default function ProviderOrderPaymentPage() {
  const { t } = useT();
  const [orderId, setOrderId] = useState('');
  const [summary, setSummary] = useState(null);
  const [method, setMethod] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [history, setHistory] = useState([]);

  async function loadOrder(event) {
    event.preventDefault();
    setError('');
    setSuccess('');
    setSummary(null);
    try {
      const data = await apiRequest('/api/service-provider/orders/' + orderId + '/payment');
      setSummary(data);
      setMethod(data.availableMethods[0]);
    } catch (err) {
      setError(err.message);
    }
  }

  async function pay() {
    setError('');
    setSuccess('');
    try {
      const data = await apiRequest(
        '/api/service-provider/orders/' + orderId + '/payment?method=' + method,
        { method: 'POST' }
      );
      setSuccess(t('Payment recorded. Status:') + ' ' + data.status);
      setSummary(await apiRequest('/api/service-provider/orders/' + orderId + '/payment'));
    } catch (err) {
      setError(err.message);
    }
  }

  async function loadHistory() {
    setError('');
    try {
      setHistory(await apiRequest('/api/service-provider/payments'));
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="page">
      <div className="card">
        <h1>{t('Pay for shop order')}</h1>
        {success && <div className="form-success" id="pay-success">{success}</div>}
        {error && <div className="form-error" id="pay-error">{error}</div>}

        <form onSubmit={loadOrder}>
          <div className="field">
            <label htmlFor="orderId">{t('Order number')}</label>
            <input id="orderId" type="number" value={orderId}
                   onChange={(e) => setOrderId(e.target.value)} required />
          </div>
          <button className="btn" id="load-order" type="submit">{t('Show order')}</button>
        </form>

        {summary && (
          <div style={{ marginTop: '16px' }}>
            <p>{t('Total amount')}: <b id="order-total">{summary.totalAmount}</b></p>
            <p>{t('Payment status')}: <b id="payment-status">{t(summary.paymentStatus)}</b></p>
            {summary.paymentStatus === 'NOT_PAID' && (
              <>
                <div className="field">
                  <label htmlFor="method">{t('Payment method')}</label>
                  <select id="method" value={method} onChange={(e) => setMethod(e.target.value)}>
                    {summary.availableMethods.map((m) => <option key={m} value={m}>{t(m)}</option>)}
                  </select>
                </div>
                <button className="btn" id="pay-now" type="button" onClick={pay}>{t('Pay now')}</button>
              </>
            )}
          </div>
        )}

        <hr style={{ margin: '20px 0' }} />
        <button className="btn" id="load-history" type="button" onClick={loadHistory}>{t('Payment history')}</button>
        <table id="history-table" style={{ marginTop: '10px', width: '100%' }}>
          <tbody>
            {history.map((h) => (
              <tr key={h.id}>
                <td>{t('Order')} #{h.orderId}</td>
                <td>{h.amount}</td>
                <td>{t(h.paymentMethod)}</td>
                <td>{t(h.status)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
