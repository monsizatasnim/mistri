import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import TextField from '../components/TextField.jsx';
import { useT } from '../i18n/languageContext.js';

const EMPTY = { category: '', area: '', minPrice: '', maxPrice: '' };

function priceText(p, t) {
  if (p.priceMin == null && p.priceMax == null) return t('Not set');
  if (p.priceMin != null && p.priceMax != null) return p.priceMin + ' - ' + p.priceMax;
  return String(p.priceMin != null ? p.priceMin : p.priceMax);
}

export default function RankedProvidersPage() {
  const { t } = useT();
  const [form, setForm] = useState(EMPTY);
  const [query, setQuery] = useState(EMPTY);
  const [providers, setProviders] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const params = new URLSearchParams();
    Object.entries(query).forEach(([key, value]) => {
      if (value.trim() !== '') params.set(key, value.trim());
    });
    const qs = params.toString();
    let cancelled = false;
    setLoading(true);
    setError('');
    apiRequest('/api/providers/ranked' + (qs ? '?' + qs : ''))
      .then((data) => {
        if (!cancelled) setProviders(data || []);
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err.message);
          setProviders([]);
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [query]);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  function handleSubmit(event) {
    event.preventDefault();
    setQuery(form);
  }

  function handleClear() {
    setForm(EMPTY);
    setQuery(EMPTY);
  }

  return (
    <div className="card card-wide">
      <h1>{t('Find and compare providers')}</h1>
      <p>
        {t('Providers are ranked by rating, closeness, price and availability. The importance of each part is set by the admin.')}
      </p>
      <form onSubmit={handleSubmit} className="filters">
        <TextField id="category" label={t('Service (for example Electrician)')}
          value={form.category} onChange={handleChange} />
        <TextField id="area" label={t('Your area (for closeness)')}
          value={form.area} onChange={handleChange} />
        <TextField id="minPrice" label={t('Lowest price')} type="number" min="0"
          value={form.minPrice} onChange={handleChange} />
        <TextField id="maxPrice" label={t('Highest price')} type="number" min="0"
          value={form.maxPrice} onChange={handleChange} />
        <div className="filter-buttons">
          <button className="btn" type="submit">{t('Rank providers')}</button>
          <button className="btn btn-grey" type="button" onClick={handleClear}>{t('Clear')}</button>
        </div>
      </form>

      {error && <div className="form-error">{t(error)}</div>}
      {loading && <p>{t('Loading...')}</p>}
      {!loading && !error && providers.length === 0 && <p>{t('No providers found.')}</p>}

      {providers.length > 0 && (
        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr>
                <th>{t('Rank')}</th>
                <th>{t('Provider')}</th>
                <th>{t('Service')}</th>
                <th>{t('Area')}</th>
                <th>{t('Price')}</th>
                <th>{t('Rating')}</th>
                <th>{t('Booked (7 days)')}</th>
                <th>{t('Score')}</th>
                <th>{t('Rating part')}</th>
                <th>{t('Closeness part')}</th>
                <th>{t('Price part')}</th>
                <th>{t('Availability part')}</th>
              </tr>
            </thead>
            <tbody>
              {providers.map((p) => (
                <tr key={p.id}>
                  <td>{p.rank}</td>
                  <td>
                    {p.fullName}
                    <div className="small">{p.phone}</div>
                  </td>
                  <td>{p.serviceCategory || '-'}</td>
                  <td>{p.serviceLocation || '-'}</td>
                  <td>{priceText(p, t)}</td>
                  <td>
                    {p.averageRating == null
                      ? t('No ratings yet')
                      : p.averageRating + ' (' + p.reviewCount + ')'}
                  </td>
                  <td>{p.upcomingBookings}</td>
                  <td><strong>{p.score}</strong></td>
                  <td>{p.ratingScore}</td>
                  <td>{p.closenessScore}</td>
                  <td>{p.priceScore}</td>
                  <td>{p.availabilityScore}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <p className="small">
        {t('Each part score is from 0 to 1. Closeness compares area names, because providers do not have map locations yet. A provider with no price or no reviews gets a neutral 0.5 for that part.')}
      </p>
    </div>
  );
}
