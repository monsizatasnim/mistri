import { Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/authContext';
import LanguageProvider from '../i18n/LanguageProvider.jsx';
import { useT } from '../i18n/languageContext';

function LayoutInner() {
  const { user, logout } = useAuth();
  const { lang, setLang, t } = useT();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <div className="layout">
      <header className="topbar">
        <strong className="brand">Mistri</strong>
        <div className="topbar-right">
          <div className="lang-switch">
            <button type="button" id="lang-en"
              className={lang === 'en' ? 'lang-btn lang-on' : 'lang-btn'}
              onClick={() => setLang('en')}>EN</button>
            <button type="button" id="lang-bn"
              className={lang === 'bn' ? 'lang-btn lang-on' : 'lang-btn'}
              onClick={() => setLang('bn')}>{'\u09AC\u09BE\u0982\u09B2\u09BE'}</button>
          </div>
          <span>{user ? user.fullName : ''}</span>
          <button className="btn-small" onClick={handleLogout}>{t('Log out')}</button>
        </div>
      </header>
      <main className="main">
        <Outlet />
      </main>
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
