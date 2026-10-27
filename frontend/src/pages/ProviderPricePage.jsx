import { useState } from 'react';
import { apiRequest } from '../api/client';
import { useT } from '../i18n/languageContext';

export default function ProviderPricePage() {
  const { t } = useT();
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
      setError(t('Please enter a price greater than zero.'));
      return;
    }
    if (priceMax && Number(priceMax) < min) {
      setError(t('Maximum price cannot be less than the minimum price.'));
      return;
    }

    const params = new URLSearchParams();
    params.set('priceMin', priceMin);
    if (priceMax) params.set('priceMax', priceMax);
    if (priceNote.trim()) params.set('priceNote', priceNote.trim());

    setLoading(true);
    try {
      await apiRequest('/api/service-provider/profile?' + params.toString(), { method: 'PUT' });
      setSuccess(t('Price saved successfully.'));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <div className="card">
        <h1>{t('Set your price')}</h1>
        <p>{t('Enter one price, or a minimum and maximum if the final price depends on the work.')}</p>
        {success && <div className="form-success" id="price-success">{success}</div>}
        {error && <div className="form-error" id="price-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="priceMin">{t('Price / minimum price (BDT)')}</label>
            <input id="priceMin" type="number" min="0" value={priceMin}
                   onChange={(e) => setPriceMin(e.target.value)} required />
          </div>
          <div className="field">
            <label htmlFor="priceMax">{t('Maximum price (optional)')}</label>
            <input id="priceMax" type="number" min="0" value={priceMax}
                   onChange={(e) => setPriceMax(e.target.value)} />
          </div>
          <div className="field">
            <label htmlFor="priceNote">{t('Note (optional)')}</label>
            <input id="priceNote" value={priceNote}
                   onChange={(e) => setPriceNote(e.target.value)} />
          </div>
          <button className="btn" id="save-price" type="submit" disabled={loading}>
            {loading ? t('Saving...') : t('Save price')}
          </button>
        </form>
      </div>
    </div>
  );
}
