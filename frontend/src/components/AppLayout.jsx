import { useEffect, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/authContext';
import LanguageProvider from '../i18n/LanguageProvider.jsx';
import { useT } from '../i18n/languageContext';
import Icon from './Icon.jsx';

const CUSTOMER = [
  { to: '/customer', icon: 'home', label: 'Customer Portal', end: true },
  { to: '/customer/providers', icon: 'search', label: 'Find and compare providers' },
  { to: '/customer/book', icon: 'calendar', label: 'Book a provider' },
  { to: '/customer/bookings', icon: 'list', label: 'My bookings' },
];
const PROVIDER = [
  { to: '/provider', icon: 'home', label: 'Service Provider Portal', end: true },
  { to: '/provider/bookings', icon: 'calendar', label: 'Booking requests' },
  { to: '/provider/orders', icon: 'box', label: 'My orders' },
  { to: '/provider/shops', icon: 'store', label: 'Find nearby partner shops' },
  { to: '/provider/price', icon: 'tag', label: 'Set my price' },
  { to: '/provider/pay', icon: 'wallet', label: 'Pay for an order' },
  { to: '/provider/availability', icon: 'clock', label: 'My availability' },
  { to: '/provider/profile', icon: 'user', label: 'Verification and profile' },
];
const SHOP = [
  { to: '/shop-owner', icon: 'home', label: 'Shop Owner Portal', end: true },
  { to: '/shop-owner/products', icon: 'box', label: 'Product catalog' },
  { to: '/shop-owner/orders', icon: 'inbox', label: 'Incoming orders' },
  { to: '/shop-owner/settings', icon: 'settings', label: 'Shop settings' },
  { to: '/shop-owner/profile', icon: 'store', label: 'Shop profile' },
];

function navFor(role, path) {
  const r = String(role || '').toUpperCase();
  if (r.includes('CUSTOMER') || path.startsWith('/customer')) return CUSTOMER;
  if (r.includes('PROVIDER') || path.startsWith('/provider')) return PROVIDER;
  if (r.includes('SHOP') || path.startsWith('/shop-owner')) return SHOP;
  return [];
}

function LayoutInner() {
  const { user, logout } = useAuth();
  const { lang, setLang, t } = useT();
  const navigate = useNavigate();
  const location = useLocation();
  const [open, setOpen] = useState(false);
  const links = navFor(user && user.role, location.pathname);
  const initial = (user && user.fullName ? user.fullName.trim().charAt(0) : '?').toUpperCase();

  useEffect(() => {
    setOpen(false);
  }, [location.pathname]);

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <div className={links.length ? 'shell' : 'shell no-side'}>
      {links.length > 0 && (
        <aside className={open ? 'sidebar open' : 'sidebar'}>
          <div className="side-brand">
            <span className="logo-mark">M</span>
            <strong>Mistri</strong>
          </div>
          <nav className="side-nav">
            {links.map((l, i) => (
              <NavLink key={l.to} to={l.to} end={l.end} className="side-link"
                style={{ animationDelay: (i * 55 + 80) + 'ms' }}>
                <Icon name={l.icon} />
                <span>{t(l.label)}</span>
              </NavLink>
            ))}
          </nav>
        </aside>
      )}
      {open && <div className="scrim" onClick={() => setOpen(false)} />}
      <div className="content">
        <header className="topbar">
          <div className="topbar-left">
            {links.length > 0 && (
              <button type="button" className="menu-btn" aria-label="Menu" onClick={() => setOpen(!open)}>
                <Icon name="menu" />
              </button>
            )}
            <strong className="brand">Mistri</strong>
          </div>
          <div className="topbar-right">
            <div className="lang-switch">
              <button type="button" id="lang-en"
                className={lang === 'en' ? 'lang-btn lang-on' : 'lang-btn'}
                onClick={() => setLang('en')}>EN</button>
              <button type="button" id="lang-bn"
                className={lang === 'bn' ? 'lang-btn lang-on' : 'lang-btn'}
                onClick={() => setLang('bn')}>{'\u09AC\u09BE\u0982\u09B2\u09BE'}</button>
            </div>
            <div className="user-chip">
              <span className="avatar">{initial}</span>
              <span className="user-name">{user ? user.fullName : ''}</span>
            </div>
            <button className="btn-small" onClick={handleLogout}>{t('Log out')}</button>
          </div>
        </header>
        <main className="main">
          <div className="route" key={location.pathname}>
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}

export default function AppLayout() {
  return (
    <LanguageProvider>
      <LayoutInner />
    </LanguageProvider>
  );
}
