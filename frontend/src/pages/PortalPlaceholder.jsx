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
        <p><Link to="/customer/providers">{'\u{1F50D} '}{t('Find and compare providers')}</Link></p>
      )}
      {role === 'ROLE_PROVIDER' && (
        <p><Link to="/provider/shops">{'\u{1F3EA} '}{t('Find nearby partner shops')}</Link></p>
      )}
      {role === 'ROLE_PROVIDER' && (
        <p><Link to="/provider/orders">{'\u{1F4CB} '}{t('My orders')}</Link></p>
      )}
      {role === 'ROLE_PROVIDER' && (
        <p><Link to="/provider/price">{'\u{1F4B0} '}{t('Set my price')}</Link></p>
      )}
      {role === 'ROLE_PROVIDER' && (
        <p><Link to="/provider/pay">{'\u{1F4B3} '}{t('Pay for an order')}</Link></p>
      )}
      {role === 'ROLE_SHOP_OWNER' && (
        <p><Link to="/shop-owner/settings">{'\u2699\uFE0F '}{t('Shop settings (hours, online orders, discount)')}</Link></p>
      )}
      {role === 'ROLE_SHOP_OWNER' && (
        <p><Link to="/shop-owner/orders">{'\u{1F4E6} '}{t('Incoming orders')}</Link></p>
      )}
      {role === 'ROLE_SHOP_OWNER' && (
        <p><Link to="/shop-owner/profile">{'\u{1F464} '}{t('Shop profile and approval status')}</Link></p>
      )}
      {role === 'ROLE_SHOP_OWNER' && (
        <p><Link to="/shop-owner/products">{'\u{1F6E0}\uFE0F '}{t('Product catalog')}</Link></p>
      )}
      <p>{t('More pages will be added in later stories.')}</p>
    </div>
  );
}


