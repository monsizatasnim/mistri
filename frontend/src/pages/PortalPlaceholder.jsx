import { Link } from 'react-router-dom';
import { useAuth } from '../auth/authContext';
import { useT } from '../i18n/languageContext';
import Icon from '../components/Icon.jsx';

const TILES = {
  ROLE_CUSTOMER: [
    { to: '/customer/providers', icon: 'search', label: 'Find and compare providers' },
    { to: '/customer/book', icon: 'calendar', label: 'Book a provider' },
    { to: '/customer/bookings', icon: 'list', label: 'My bookings' },
  ],
  ROLE_PROVIDER: [
    { to: '/provider/bookings', icon: 'calendar', label: 'Booking requests' },
    { to: '/provider/orders', icon: 'box', label: 'My orders' },
    { to: '/provider/shops', icon: 'store', label: 'Find nearby partner shops' },
    { to: '/provider/price', icon: 'tag', label: 'Set my price' },
    { to: '/provider/pay', icon: 'wallet', label: 'Pay for an order' },
    { to: '/provider/availability', icon: 'clock', label: 'My availability' },
    { to: '/provider/profile', icon: 'user', label: 'Verification and profile' },
  ],
  ROLE_SHOP_OWNER: [
    { to: '/shop-owner/products', icon: 'box', label: 'Product catalog' },
    { to: '/shop-owner/orders', icon: 'inbox', label: 'Incoming orders' },
    { to: '/shop-owner/settings', icon: 'settings', label: 'Shop settings (hours, online orders, discount)' },
    { to: '/shop-owner/profile', icon: 'store', label: 'Shop profile and approval status' },
  ],
};

export default function PortalPlaceholder({ title }) {
  const { user } = useAuth();
  const { t } = useT();
  const role = user ? user.role : '';
  const tiles = TILES[role] || [];

  return (
    <div className="dash">
      <section className="dash-hero">
        <div className="orb orb-a" />
        <div className="orb orb-b" />
        <div className="dash-hero-inner">
          <h1>{t(title)}</h1>
          <p className="dash-welcome">{t('Welcome')}, {user ? user.fullName : 'guest'}.</p>
          <span className="role-pill">{t('Role')}: {user ? user.role : 'none'}</span>
        </div>
      </section>
      <div className="tile-grid">
        {tiles.map((x, i) => (
          <Link key={x.to} to={x.to} className="tile" style={{ animationDelay: (i * 70 + 120) + 'ms' }}>
            <span className="tile-ico"><Icon name={x.icon} /></span>
            <span className="tile-label">{t(x.label)}</span>
            <span className="tile-arrow">{'\u2192'}</span>
          </Link>
        ))}
      </div>
      <p className="small dash-note">{t('More pages will be added in later stories.')}</p>
    </div>
  );
}
