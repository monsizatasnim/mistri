import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { apiRequest } from '../api/client';
import { useT } from '../i18n/languageContext.js';

function priceText(p, t) {
  if (p.priceMin == null) return t('Price not set yet');
  if (p.priceMax != null && p.priceMax > p.priceMin) return '\u09F3' + p.priceMin + ' - \u09F3' + p.priceMax;
  return '\u09F3' + p.priceMin;
}

const ProviderProfilePage = () => {
  const { id } = useParams();
  const { t } = useT();
  const [provider, setProvider] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    apiRequest('/api/providers/' + id + '/profile')
      .then(setProvider)
      .catch((err) => setError(err.message));
  }, [id]);

  if (error) return <div className="form-error" id="profile-error">{t(error)}</div>;
  if (!provider) return <div className="small">{t('Loading...')}</div>;

  return (
    <div className="profile-wrap">
      <section className="panel prov-card">
        <div className="prov-head">
          <span className="avatar prov-avatar">{(provider.fullName || '?').charAt(0).toUpperCase()}</span>
          <div>
            <h2 className="prov-name">
              {provider.fullName}
              {provider.verified && (
                <span className="badge badge-VERIFIED prov-badge">{'\u2713'} {t('Verified Provider')}</span>
              )}
            </h2>
            <p className="small">{provider.serviceCategory || t('Service')} {provider.serviceLocation ? '\u2022 ' + provider.serviceLocation : ''}</p>
          </div>
        </div>
        {provider.serviceDescription && <p>{provider.serviceDescription}</p>}
        <hr className="prov-hr" />
        <span className="small">{t('Price')}</span>
        <h3 id="provider-price" className="prov-price">{priceText(provider, t)}</h3>
        {provider.priceNote && <p id="provider-price-note" className="small">{provider.priceNote}</p>}
      </section>
    </div>
  );
};

export default ProviderProfilePage;
