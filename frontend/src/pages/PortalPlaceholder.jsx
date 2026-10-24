import { Link } from 'react-router-dom';
import { useAuth } from '../auth/authContext';
import { useT } from '../i18n/languageContext';

export default function PortalPlaceholder({ title }) {
  const { user } = useAuth();
  const { t } = useT();
  const role = user ? user.role : '';

  return (
    <div className="card">
      <h1>{t(title)}</h1>
      <p>{t('Welcome')}, {user ? user.fullName : 'guest'}.</p>
      <p>{t('Role')}: {user ? user.role : 'none'}</p>
      {role === 'ROLE_CUSTOMER' && (
        <p><Link to="/customer/providers">{t('Find and compare providers')}</Link></p>
      )}
      {role === 'ROLE_PROVIDER' && (
        <p><Link to="/provider/shops">{t('Find nearby partner shops')}</Link></p>
      )}
      {role === 'ROLE_SHOP_OWNER' && (
        <p><Link to="/shop-owner/settings">{t('Shop settings (hours, online orders, discount)')}</Link></p>
      )}
      {role === 'ROLE_SHOP_OWNER' && (
        <p><Link to="/shop-owner/orders">{t('Incoming orders')}</Link></p>
      )}
      {role === 'ROLE_SHOP_OWNER' && (
        <p><Link to="/shop-owner/profile">{t('Shop profile and approval status')}</Link></p>
      )}
      {role === 'ROLE_SHOP_OWNER' && (
        <p><Link to="/shop-owner/products">{t('Product catalog')}</Link></p>
      )}
      <p>{t('More pages will be added in later stories.')}</p>
    </div>
  );
}
