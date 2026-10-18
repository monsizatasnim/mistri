import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';

const FILTERS = ['', 'PLACED', 'ACCEPTED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'MODIFIED', 'COMPLETED', 'DECLINED', 'CANCELLED'];

function OrderCard({ order, onChange, onError }) {
  const [reason, setReason] = useState('');
  const [fast, setFast] = useState(order.fastDeliveryPossible === true);
  const [minutes, setMinutes] = useState(order.estimatedMinutes ? String(order.estimatedMinutes) : '');
  const [busy, setBusy] = useState(false);

  async function act(path, method, body) {
    setBusy(true);
    onError('');
    try {
      const updated = await apiRequest('/api/shop-owner/orders/' + order.id + path, { method, body });
      onChange(updated);
    } catch (err) {
      onError(err.message + (err.fieldErrors && err.fieldErrors.reason ? ' ' + err.fieldErrors.reason : ''));
    } finally {
      setBusy(false);
    }
  }

  const s = order.status;
  const canMarkItems = s === 'PLACED' || s === 'ACCEPTED' || s === 'MODIFIED';
  const urgentOpen = order.urgent && (s === 'PLACED' || s === 'ACCEPTED' || s === 'PREPARING');

  return (
    <div className="order-card">
      <div className="order-head">
        <strong>Order #{order.id}</strong>
        <span className={'badge badge-' + s}>{s}</span>
        {order.urgent && <span className="badge badge-URGENT">URGENT</span>}
      </div>
      <p className="small">
        {order.providerName} ({order.providerPhone}) - deliver to {order.deliveryLocation || 'not given'}
      </p>
      {order.note && <p className="small">Note: {order.note}</p>}

      <table className="data-table">
        <thead>
          <tr><th>Item</th><th>Type</th><th>Qty</th><th>Line total</th><th></th></tr>
        </thead>
        <tbody>
          {order.items.map((i) => (
            <tr key={i.itemId}>
              <td>{i.unavailable ? <s>{i.productName}</s> : i.productName}</td>
              <td>{i.term}</td>
              <td>{i.quantity}</td>
              <td>{i.lineTotal}</td>
              <td>
                {i.unavailable && <span className="small">Unavailable</span>}
                {!i.unavailable && canMarkItems && (
                  <button className="btn-small-dark" disabled={busy}
                    onClick={() => act('/items/' + i.itemId + '/unavailable', 'PATCH')}>
                    Mark unavailable
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <p><strong>Total: {order.totalAmount}</strong></p>

      {s === 'MODIFIED' && (
        <div className="form-success">Waiting for the provider to accept the partial order or cancel.</div>
      )}
      {s === 'DECLINED' && order.declineReason && (
        <div className="form-error">Declined: {order.declineReason}</div>
      )}

      {s === 'PLACED' && (
        <div className="order-actions">
          <button className="btn-small-dark" disabled={busy} onClick={() => act('/accept', 'POST')}>
            Accept
          </button>
          <input placeholder="Reason for declining" value={reason}
            onChange={(e) => setReason(e.target.value)} />
          <button className="btn-small-red" disabled={busy}
            onClick={() => act('/decline', 'POST', { reason })}>
            Decline
          </button>
        </div>
      )}

      {s === 'ACCEPTED' && (
        <div className="order-actions">
          <button className="btn-small-dark" disabled={busy}
            onClick={() => act('/status', 'PATCH', { status: 'PREPARING' })}>
            Start preparing
          </button>
        </div>
      )}
      {s === 'PREPARING' && (
        <div className="order-actions">
          <button className="btn-small-dark" disabled={busy}
            onClick={() => act('/status', 'PATCH', { status: 'READY' })}>
            Ready for pickup
          </button>
          <button className="btn-small-dark" disabled={busy}
            onClick={() => act('/status', 'PATCH', { status: 'OUT_FOR_DELIVERY' })}>
            Out for delivery
          </button>
        </div>
      )}
      {(s === 'READY' || s === 'OUT_FOR_DELIVERY') && (
        <div className="order-actions">
          <button className="btn-small-dark" disabled={busy}
            onClick={() => act('/status', 'PATCH', { status: 'COMPLETED' })}>
            Mark completed
          </button>
        </div>
      )}

      {urgentOpen && (
        <div className="order-actions">
          <label>
            <input type="checkbox" checked={fast} onChange={(e) => setFast(e.target.checked)} />{' '}
            Fast delivery possible
          </label>
          {fast && (
            <input type="number" min="1" placeholder="Minutes" value={minutes}
              onChange={(e) => setMinutes(e.target.value)} />
          )}
          <button className="btn-small-dark" disabled={busy}
            onClick={() => act('/urgent-delivery', 'PUT', {
              fastDeliveryPossible: fast,
              estimatedMinutes: fast && minutes !== '' ? Number(minutes) : null,
            })}>
            Save delivery answer
          </button>
        </div>
      )}
      {order.urgent && order.fastDeliveryPossible !== null && order.fastDeliveryPossible !== undefined && (
        <p className="small">
          Delivery answer: {order.fastDeliveryPossible
            ? 'fast delivery in about ' + order.estimatedMinutes + ' minutes'
            : 'fast delivery not possible'}
        </p>
      )}

      <details>
        <summary>Status history</summary>
        <ul className="history">
          {order.history.map((h, idx) => (
            <li key={idx}>
              {h.status} - {new Date(h.changedAt).toLocaleString()}
              {h.note ? ' - ' + h.note : ''}
            </li>
          ))}
        </ul>
      </details>
    </div>
  );
}

export default function ShopOrdersPage() {
  const [filter, setFilter] = useState('');
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    apiRequest('/api/shop-owner/orders' + (filter ? '?status=' + filter : ''))
      .then((data) => { if (!cancelled) setOrders(data || []); })
      .catch((err) => { if (!cancelled) { setError(err.message); setOrders([]); } })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [filter]);

  function replaceOrder(updated) {
    setOrders((list) => list.map((o) => (o.id === updated.id ? updated : o)));
  }

  return (
    <div className="card card-wide">
      <h1>Incoming orders</h1>
      <div className="field">
        <label htmlFor="statusFilter">Show</label>
        <select id="statusFilter" value={filter} onChange={(e) => setFilter(e.target.value)}>
          {FILTERS.map((f) => (
            <option key={f} value={f}>{f === '' ? 'All orders' : f}</option>
          ))}
        </select>
      </div>
      {error && <div className="form-error">{error}</div>}
      {loading && <p>Loading...</p>}
      {!loading && orders.length === 0 && !error && <p>No orders found.</p>}
      {orders.map((o) => (
        <OrderCard key={o.id} order={o} onChange={replaceOrder} onError={setError} />
      ))}
    </div>
  );
}
