import { useEffect, useRef, useState } from 'react';
import { apiRequest } from '../api/client';
import TextField from '../components/TextField.jsx';

function label(value) {
  if (!value) return '-';
  return String(value).replace(/_/g, ' ').toLowerCase();
}

function priceText(product) {
  const parts = [];
  if (product.salePrice != null) parts.push('Buy: ' + product.salePrice);
  if (product.rentPrice != null) parts.push('Rent: ' + product.rentPrice);
  return parts.length > 0 ? parts.join(' / ') : 'Not set';
}

export default function ProviderNearbyShopsPage() {
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
      <h1>Nearby partner shops</h1>
      <p>
        Choose or type the place where your job is. You will see the partner
        shops near that place.
      </p>

      <form onSubmit={handleSubmit} className="filters">
        <TextField id="jobLocation" label="Job location"
          value={jobLocation} list="shop-locations"
          onChange={(event) => setJobLocation(event.target.value)} />
        <datalist id="shop-locations">
          {locations.map((place) => (
            <option key={place} value={place} />
          ))}
        </datalist>
        <div className="filter-buttons">
          <button className="btn" type="submit">Find shops</button>
          <button className="btn btn-grey" type="button" onClick={handleClear}>Clear</button>
        </div>
      </form>

      {error && <div className="form-error">{error}</div>}
      {loading && <p>Loading...</p>}
      {!loading && !error && shops.length === 0 && (
        <p>{searched ? 'No partner shops found near ' + searched + '.' : 'No partner shops found.'}</p>
      )}

      {shops.length > 0 && (
        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr>
                <th>Shop</th>
                <th>Owner</th>
                <th>Location</th>
                <th>Contact</th>
                <th>Matched</th>
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
                      View products
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {shopLoading && <p>Loading shop...</p>}
      {shopError && <div className="form-error">{shopError}</div>}

      {selected && (
        <div>
          <h2>{selected.shopName}</h2>
          <p>
            Owner: {selected.ownerName || '-'}
            <br />
            Location: {selected.location || '-'}
            <br />
            Contact: {selected.contactInfo || '-'}
          </p>

          <h3>Available products</h3>
          {products.length === 0 && <p>This shop has no products yet.</p>}
          {products.length > 0 && (
            <div className="table-scroll">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>Category</th>
                    <th>Type</th>
                    <th>Price</th>
                    <th>Stock</th>
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
                      <td>{label(product.availabilityType)}</td>
                      <td>{priceText(product)}</td>
                      <td>{label(product.stockStatus)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
}