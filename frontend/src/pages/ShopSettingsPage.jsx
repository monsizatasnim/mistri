import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import TextField from '../components/TextField.jsx';

export default function ShopSettingsPage() {
  const [form, setForm] = useState({
    opensAt: '',
    closesAt: '',
    acceptingOnlineOrders: true,
    providerDiscountPercent: '',
  });
  const [shop, setShop] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    apiRequest('/api/shop-owner/settings')
      .then((data) => {
        setShop(data);
        setForm({
          opensAt: data.opensAt || '',
          closesAt: data.closesAt || '',
          acceptingOnlineOrders: data.acceptingOnlineOrders,
          providerDiscountPercent:
            data.providerDiscountPercent === null || data.providerDiscountPercent === undefined
              ? ''
              : String(data.providerDiscountPercent),
        });
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  function handleChange(event) {
    const { name, value, type, checked } = event.target;
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSuccess('');
    setFieldErrors({});
    setSaving(true);

    const body = {
      opensAt: form.opensAt || null,
      closesAt: form.closesAt || null,
      acceptingOnlineOrders: form.acceptingOnlineOrders,
      providerDiscountPercent:
        form.providerDiscountPercent === '' ? null : Number(form.providerDiscountPercent),
    };

    try {
      await apiRequest('/api/shop-owner/settings', { method: 'PUT', body });
      setSuccess('Settings saved. Providers can see the changes now.');
    } catch (err) {
      setError(err.message);
      setFieldErrors(err.fieldErrors || {});
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <div className="card">Loading...</div>;
  }

  return (
    <div className="card">
      <h1>Shop settings</h1>
      {shop && <p>{shop.shopName}</p>}
      {success && <div className="form-success">{success}</div>}
      {error && <div className="form-error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <TextField id="opensAt" label="Opening time" type="time" value={form.opensAt}
          onChange={handleChange} error={fieldErrors.opensAt} />
        <TextField id="closesAt" label="Closing time" type="time" value={form.closesAt}
          onChange={handleChange} error={fieldErrors.closesAt} />
        <div className="field">
          <label htmlFor="acceptingOnlineOrders">
            <input
              id="acceptingOnlineOrders"
              name="acceptingOnlineOrders"
              type="checkbox"
              checked={form.acceptingOnlineOrders}
              onChange={handleChange}
            />{' '}
            Accept online orders
          </label>
          {!form.acceptingOnlineOrders && (
            <div className="field-error">
              Providers can still see your products and contact details, but cannot order.
            </div>
          )}
        </div>
        <TextField id="providerDiscountPercent" label="Discount for providers (%, optional)"
          type="number" min="0" max="100" step="0.01" value={form.providerDiscountPercent}
          onChange={handleChange} error={fieldErrors.providerDiscountPercent} />
        <button className="btn" type="submit" disabled={saving}>
          {saving ? 'Please wait...' : 'Save settings'}
        </button>
      </form>
    </div>
  );
}
