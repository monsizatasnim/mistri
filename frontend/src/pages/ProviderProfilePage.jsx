import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { apiRequest } from '../api/client';

function priceText(p) {
  if (p.priceMin == null) return 'Price not set yet';
  if (p.priceMax != null && p.priceMax > p.priceMin) return '৳' + p.priceMin + ' - ৳' + p.priceMax;
  return '৳' + p.priceMin;
}

const ProviderProfilePage = () => {
  const { id } = useParams();
  const [provider, setProvider] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    apiRequest('/api/providers/' + id + '/profile')
      .then(setProvider)
      .catch((err) => setError(err.message));
  }, [id]);

  if (error) return <div style={{ padding: '20px' }} id="profile-error">{error}</div>;
  if (!provider) return <div style={{ padding: '20px' }}>Loading...</div>;

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif', backgroundColor: '#f8f9fa', minHeight: '100vh' }}>
      <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
        <h2 style={{ margin: 0 }}>
          {provider.fullName}
          {provider.verified && (
            <span style={{ background: '#e0f2fe', color: '#0369a1', padding: '4px 10px', borderRadius: '12px', fontSize: '14px', marginLeft: '10px' }}>
              ✓ Verified Provider
            </span>
          )}
        </h2>
        <p style={{ color: '#666' }}>{provider.serviceCategory || 'Service'} {provider.serviceLocation ? '• ' + provider.serviceLocation : ''}</p>
        {provider.serviceDescription && <p>{provider.serviceDescription}</p>}
        <hr style={{ margin: '15px 0', border: '0.5px solid #eee' }} />
        <span style={{ color: '#888', fontSize: '14px' }}>Price</span>
        <h3 id="provider-price" style={{ color: '#28a745', margin: '5px 0' }}>{priceText(provider)}</h3>
        {provider.priceNote && <p id="provider-price-note" style={{ color: '#555' }}>{provider.priceNote}</p>}
      </div>
    </div>
  );
};

export default ProviderProfilePage;