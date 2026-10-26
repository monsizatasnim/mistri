import { useEffect, useRef, useState } from 'react';
import { apiRequest } from '../api/client';
import TextField from '../components/TextField.jsx';
import ProviderOrderForm from '../components/ProviderOrderForm.jsx';
import { useT } from '../i18n/languageContext';

function label(value) {
  if (!value) return '-';
  return String(value).replace(/_/g, ' ').toLowerCase();
}

function priceText(product, t) {
  const parts = [];
  if (product.salePrice != null) parts.push(t('Buy') + ': ' + product.salePrice);
  if (product.rentPrice != null) parts.push(t('Rent') + ': ' + product.rentPrice);
  return parts.length > 0 ? parts.join(' / ') : t('Not set');
}

export default function ProviderNearbyShopsPage() {
  const { t } = useT();
  const [locations, setLocations] = useState([]);
  const [jobLocation, setJobLocation] = useState('');
  const [shops, setShops] = useState([]);
  const [searched, setSearched] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [selected, setSelected] = useState(null);
  const [products, setProducts] = useState([]);
  const [shopLoading, setShopLoading] = useState(false);
  const [shopError, setShopError] = useState('');
  const shopRequest = useRef(0);

  function search(location) {
    const value = location.trim();
    const qs = value ? '?location=' + encodeURIComponent(value) : '';
    setLoading(true);
    setError('');
    setSelected(null);
    apiRequest('/api/shops/nearby' + qs)
      .then((data) => {
        setShops(data || []);
        setSearched(value);
      })
      .catch((err) => {
        setError(err.message);
        setShops([]);
      })
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    apiRequest('/api/shops/locations')
      .then((data) => setLocations(data || []))
      .catch(() => setLocations([]));
    search('');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function handleSubmit(event) {
    event.preventDefault();
    search(jobLocation);
  }

  function handleClear() {
    setJobLocation('');
    search('');
  }

  function openShop(shopOwnerId) {
    const requestId = shopRequest.current + 1;
    shopRequest.current = requestId;
    setShopLoading(true);
    setShopError('');
    setSelected(null);
    setProducts([]);
    Promise.all([
      apiRequest('/api/shops/' + shopOwnerId),
      apiRequest('/api/shops/' + shopOwnerId + '/products'),
    ])
      .then(([info, items]) => {
        if (shopRequest.current !== requestId) return;
        setSelected(info);
        setProducts(items || []);
      })
      .catch((err) => {
        if (shopRequest.current !== requestId) return;
        setShopError(err.message);
      })
      .finally(() => {
        if (shopRequest.current === requestId) setShopLoading(false);
      });
  }

  return (
    <div className="card card-wide">
      <h1>{t('Nearby partner shops')}</h1>
      <p>
        {t('Choose or type the place where your job is. You will see the partner shops near that place.')}
      </p>

      <form onSubmit={handleSubmit} className="filters">
        <TextField id="jobLocation" label={t('Job location')}
          value={jobLocation} list="shop-locations"
          onChange={(event) => setJobLocation(event.target.value)} />
        <datalist id="shop-locations">
          {locations.map((place) => (
            <option key={place} value={place} />
          ))}
        </datalist>
        <div className="filter-buttons">
          <button className="btn" type="submit">{t('Find shops')}</button>
          <button className="btn btn-grey" type="button" onClick={handleClear}>{t('Clear')}</button>
        </div>
      </form>

      {error && <div className="form-error">{error}</div>}
      {loading && <p>{t('Loading...')}</p>}
      {!loading && !error && shops.length === 0 && (
        <p>{searched ? t('No partner shops found near') + ' ' + searched + '.' : t('No partner shops found.')}</p>
      )}

      {shops.length > 0 && (
        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr>
                <th>{t('Shop')}</th>
                <th>{t('Owner')}</th>
                <th>{t('Location')}</th>
                <th>{t('Contact')}</th>
                <th>{t('Matched')}</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {shops.map((shop) => (
                <tr key={shop.shopOwnerId}>
                  <td><strong>{shop.shopName}</strong></td>
                  <td>{shop.ownerName || '-'}</td>
                  <td>{shop.location || '-'}</td>
                  <td>{shop.contactInfo || '-'}</td>
                  <td>
                    {shop.matchedTerms && shop.matchedTerms.length > 0
                      ? shop.matchedTerms.join(', ')
                      : '-'}
                  </td>
                  <td>
                    <button className="btn" type="button"
                      onClick={() => openShop(shop.shopOwnerId)}>
                      {t('View products')}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {shopLoading && <p>{t('Loading shop...')}</p>}
      {shopError && <div className="form-error">{shopError}</div>}

      {selected && (
        <div>
          <h2>{selected.shopName}</h2>
          <p>
            {t('Owner')}: {selected.ownerName || '-'}
            <br />
            {t('Location')}: {selected.location || '-'}
            <br />
            {t('Contact')}: {selected.contactInfo || '-'}
          </p>

          <h3>{t('Available products')}</h3>
          {products.length === 0 && <p>{t('This shop has no products yet.')}</p>}
          {products.length > 0 && (
            <div className="table-scroll">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>{t('Product')}</th>
                    <th>{t('Category')}</th>
                    <th>{t('Type')}</th>
                    <th>{t('Price')}</th>
                    <th>{t('Stock')}</th>
                  </tr>
                </thead>
                <tbody>
                  {products.map((product) => (
                    <tr key={product.id}>
                      <td>
                        {product.name}
                        {product.description && (
                          <div className="small">{product.description}</div>
                        )}
                      </td>
                      <td>{product.category || '-'}</td>
                      <td>{t(label(product.availabilityType))}</td>
                      <td>{priceText(product, t)}</td>
                      <td>{t(label(product.stockStatus))}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          <ProviderOrderForm shop={selected} products={products} />
        </div>
      )}
    </div>
  );
}
