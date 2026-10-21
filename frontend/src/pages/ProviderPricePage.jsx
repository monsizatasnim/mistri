import { useState } from 'react';
import { apiRequest } from '../api/client';

export default function ProviderPricePage() {
  const [priceMin, setPriceMin] = useState('');
  const [priceMax, setPriceMax] = useState('');
  const [priceNote, setPriceNote] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSuccess('');

    const min = Number(priceMin);
    if (!priceMin || min <= 0) {
      setError('Please enter a price greater than zero.');
      return;
    }
    if (priceMax && Number(priceMax) < min) {
      setError('Maximum price cannot be less than the minimum price.');
      return;
    }

    const params = new URLSearchParams();
    params.set('priceMin', priceMin);
    if (priceMax) params.set('priceMax', priceMax);
    if (priceNote.trim()) params.set('priceNote', priceNote.trim());

    setLoading(true);
    try {
      await apiRequest('/api/service-provider/profile?' + params.toString(), { method: 'PUT' });
      setSuccess('Price saved successfully.');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <div className="card">
        <h1>Set your price</h1>
        <p>Enter one price, or a minimum and maximum if the final price depends on the work.</p>
        {success && <div className="form-success" id="price-success">{success}</div>}
        {error && <div className="form-error" id="price-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="priceMin">Price / minimum price (BDT)</label>
            <input id="priceMin" type="number" min="0" value={priceMin}
                   onChange={(e) => setPriceMin(e.target.value)} required />
          </div>
          <div className="field">
            <label htmlFor="priceMax">Maximum price (optional)</label>
            <input id="priceMax" type="number" min="0" value={priceMax}
                   onChange={(e) => setPriceMax(e.target.value)} />
          </div>
          <div className="field">
            <label htmlFor="priceNote">Note (optional)</label>
            <input id="priceNote" value={priceNote}
                   onChange={(e) => setPriceNote(e.target.value)} />
          </div>
          <button className="btn" id="save-price" type="submit" disabled={loading}>
            {loading ? 'Saving...' : 'Save price'}
          </button>
        </form>
      </div>
    </div>
  );
}