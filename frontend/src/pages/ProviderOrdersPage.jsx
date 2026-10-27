import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import { useT } from '../i18n/languageContext';

function OrderRow({ order, onChanged, onError }) {
  const { t } = useT();
  const [open, setOpen] = useState(false);
  const [detail, setDetail] = useState(null);
  const [busy, setBusy] = useState(false);

  async function loadDetail() {
    try {
      setDetail(await apiRequest('/api/provider/orders/' + order.id + '/details'));
    } catch (err) {
      onError(err.message);
    }
  }

  async function toggle() {
    if (!open) await loadDetail();
    setOpen(!open);
  }

  async function act(path, method) {
    setBusy(true);
    onError('');
    try {
      await apiRequest('/api/provider/orders/' + order.id + path, { method });
      await onChanged();
      if (open) await loadDetail();
    } catch (err) {
      onError(err.message);
    } finally {
      setBusy(false);
    }
  }

  const status = detail ? detail.status : order.status;

  return (
    <div className="order-card">
      <div className="order-head">
        <strong>{t('Order')} #{order.id}</strong>
        <span className={'badge badge-' + order.status}>{t(order.status)}</span>
        {order.urgent && <span className="badge badge-URGENT">{t('URGENT')}</span>}
      </div>
      <p className="small">
        {t('Shop')}: {order.shopName} - {new Date(order.createdAt).toLocaleString()} - {t('Total')}: {order.totalAmount}
      </p>
      <div className="order-actions">
        <button className="btn-small-dark" onClick={toggle}>{open ? t('Hide') : t('Details')}</button>
        {order.status === 'PLACED' && !order.urgent && (
          <button className="btn-small-dark" disabled={busy} onClick={() => act('/urgent', 'PATCH')}>
            {t('Mark urgent')}
          </button>
        )}
        {order.status === 'MODIFIED' && (
          <>
            <button className="btn-small-dark" disabled={busy} onClick={() => act('/accept-partial', 'POST')}>
              {t('Accept partial order')}
            </button>
            <button className="btn-small-red" disabled={busy} onClick={() => act('/cancel', 'POST')}>
              {t('Cancel the rest')}
            </button>
          </>
        )}
      </div>
      {order.status === 'MODIFIED' && (
        <div className="form-success">{t('The shop could not supply some items. Accept the rest or cancel the order.')}</div>
      )}
      {open && detail && (
        <div>
          <table className="data-table">
            <thead>
              <tr><th>{t('Item')}</th><th>{t('Type')}</th><th>{t('Qty')}</th><th>{t('Line total')}</th><th></th></tr>
            </thead>
            <tbody>
              {detail.items.map((i) => (
                <tr key={i.itemId}>
                  <td>{i.unavailable ? <s>{i.productName}</s> : i.productName}</td>
                  <td>{i.term}</td>
                  <td>{i.quantity}</td>
                  <td>{i.lineTotal}</td>
                  <td>{i.unavailable ? t('Unavailable') : ''}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {status === 'DECLINED' && detail.declineReason && (
            <div className="form-error">{t('Declined')}: {detail.declineReason}</div>
          )}
          {detail.urgent && detail.fastDeliveryPossible !== null && detail.fastDeliveryPossible !== undefined && (
            <p className="small">
              {t('Delivery answer')}: {detail.fastDeliveryPossible
                ? t('fast delivery in about') + ' ' + detail.estimatedMinutes + ' ' + t('minutes')
                : t('fast delivery not possible')}
            </p>
          )}
          <p><strong>{t('Status history')}</strong></p>
          <ul className="history">
            {detail.history.map((h, idx) => (
              <li key={idx}>
                {t(h.status)} - {new Date(h.changedAt).toLocaleString()}{h.note ? ' - ' + h.note : ''}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

export default function ProviderOrdersPage() {
  const { t } = useT();
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  async function load() {
    try {
      setOrders((await apiRequest('/api/provider/orders')) || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  return (
    <div className="card card-wide">
      <h1>{t('My orders')}</h1>
      {error && <div className="form-error">{error}</div>}
      {loading && <p>{t('Loading orders...')}</p>}
      {!loading && orders.length === 0 && !error && <p>{t('No orders yet.')}</p>}
      {orders.map((o) => (
        <OrderRow key={o.id} order={o} onChanged={load} onError={setError} />
      ))}
    </div>
  );
}
