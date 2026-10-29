import { Outlet } from 'react-router-dom';
import LanguageProvider from '../i18n/LanguageProvider.jsx';
import { useT } from '../i18n/languageContext';
import Icon from './Icon.jsx';

function PublicInner() {
  const { lang, setLang, t } = useT();
  return (
    <div className="auth-shell">
      <section className="auth-hero">
        <div className="orb orb-a" />
        <div className="orb orb-b" />
        <div className="orb orb-c" />
        <div className="grid-fx" />
        <div className="auth-hero-inner">
          <div className="auth-brand">
            <span className="logo-mark">M</span>
            <strong>Mistri</strong>
          </div>
          <h2 className="auth-title">
            {t('Trusted local services,')} <span className="grad">{t('one tap away.')}</span>
          </h2>
          <p className="auth-sub">
            {t('Book verified providers, order tools from partner shops and track everything in one place.')}
          </p>
          <ul className="feat-list">
            <li className="feat" style={{ animationDelay: '0.35s' }}>
              <span className="feat-ico"><Icon name="shield" /></span>{t('Verified providers')}
            </li>
            <li className="feat" style={{ animationDelay: '0.5s' }}>
              <span className="feat-ico"><Icon name="calendar" /></span>{t('Easy booking')}
            </li>
            <li className="feat" style={{ animationDelay: '0.65s' }}>
              <span className="feat-ico"><Icon name="store" /></span>{t('Partner shops')}
            </li>
          </ul>
          <div className="float-card">
            <span className="float-ico"><Icon name="check" /></span>
            <span>{t('Booking confirmed')}</span>
          </div>
        </div>
      </section>
      <div className="auth-form-side">
        <div className="public-lang">
          <div className="lang-switch">
            <button type="button" id="lang-en"
              className={lang === 'en' ? 'lang-btn lang-on public-lang-btn' : 'lang-btn public-lang-btn'}
              onClick={() => setLang('en')}>EN</button>
            <button type="button" id="lang-bn"
              className={lang === 'bn' ? 'lang-btn lang-on public-lang-btn' : 'lang-btn public-lang-btn'}
              onClick={() => setLang('bn')}>{'\u09AC\u09BE\u0982\u09B2\u09BE'}</button>
          </div>
        </div>
        <Outlet />
      </div>
    </div>
  );
}

export default function PublicLayout() {
  return (
    <LanguageProvider>
      <PublicInner />
    </LanguageProvider>
  );
}
