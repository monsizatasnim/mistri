import { useState } from 'react';
import { apiRequest } from '../api/client';
import { useT } from '../i18n/languageContext';

export default function ProviderOrderForm({ shop, products }) {
  const { t } = useT();
  const [lines, setLines] = useState({});
  const [urgent, setUrgent] = useState(false);
  const [deliveryLocation, setDeliveryLocation] = useState('');
  const [note, setNote] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [done, setDone] = useState('');

  const shopId = shop.shopOwnerId ?? shop.id;
  const orderable = (products || []).filter((p) => p.stockStatus === 'AVAILABLE'
    && (p.salePrice != null || p.rentPrice != null));

  if (shop.acceptingOnlineOrders === false) {
    return <p className="small">{t('This shop is not taking online orders right now.')}</p>;
  }
  if (orderable.length === 0) return null;

  function change(product, field, value) {
    const current = lines[product.id] || {};
    setLines({ ...lines, [product.id]: { ...current, [field]: value } });
  }

  function termFor(product) {
    const line = lines[product.id] || {};
    if (line.term) return line.term;
    return product.salePrice != null ? 'SALE' : 'RENT';
  }

  async function submit(event) {
    event.preventDefault();
    setError('');
    setDone('');
    if (!shopId) {
      setError(t('Cannot find the shop id. Please tell your team lead.'));
      return;
    }
    const items = orderable
      .filter((p) => Number((lines[p.id] || {}).quantity) > 0)
      .map((p) => ({
        productId: p.id,
        quantity: Number(lines[p.id].quantity),
        term: termFor(p),
      }));
    if (items.length === 0) {
      setError(t('Enter a quantity for at least one product.'));
      return;
    }
    setBusy(true);
    try {
      const order = await apiRequest('/api/provider/orders', {
        method: 'POST',
        body: { shopOwnerId: shopId, urgent, deliveryLocation, note, items },
      });
      setDone(t('Order placed.') + ' #' + order.id + ' - ' + t('Total') + ': ' + order.totalAmount);
      setLines({});
      setUrgent(false);
      setNote('');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form onSubmit={submit} className="order-card">
      <h3>{t('Order from this shop')}</h3>
      <table className="data-table">
        <thead>
          <tr><th>{t('Product')}</th><th>{t('Buy or rent')}</th><th>{t('Qty')}</th></tr>
        </thead>
        <tbody>
          {orderable.map((p) => (
            <tr key={p.id}>
              <td>{p.name}</td>
              <td>
                <select id={'order-term-' + p.id} value={termFor(p)}
                  onChange={(event) => change(p, 'term', event.target.value)}>
                  {p.salePrice != null && <option value="SALE">{t('Buy')} ({p.salePrice})</option>}
                  {p.rentPrice != null && <option value="RENT">{t('Rent')} ({p.rentPrice})</option>}
                </select>
              </td>
              <td>
                <input id={'order-qty-' + p.id} type="number" min="0" max="1000"
                  value={(lines[p.id] || {}).quantity || ''}
                  onChange={(event) => change(p, 'quantity', event.target.value)} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <p>
        <label>
          <input id="order-urgent" type="checkbox" checked={urgent}
            onChange={(event) => setUrgent(event.target.checked)} />
          {' '}{t('Urgent order')}
        </label>
      </p>
      <p>
        <label htmlFor="order-delivery">{t('Delivery place')}</label><br />
        <input id="order-delivery" type="text" value={deliveryLocation}
          onChange={(event) => setDeliveryLocation(event.target.value)} />
      </p>
      <p>
        <label htmlFor="order-note">{t('Note')}</label><br />
        <input id="order-note" type="text" value={note}
          onChange={(event) => setNote(event.target.value)} />
      </p>

      {error && <div className="form-error">{error}</div>}
      {done && <div className="form-success">{done}</div>}
      <button className="btn" type="submit" disabled={busy}>
        {busy ? t('Please wait...') : t('Place order')}
      </button>
    </form>
  );
}
