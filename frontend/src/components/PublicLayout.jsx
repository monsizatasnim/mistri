import { Outlet } from 'react-router-dom';
import LanguageProvider from '../i18n/LanguageProvider.jsx';
import { useT } from '../i18n/languageContext';

function PublicInner() {
  const { lang, setLang } = useT();
  return (
    <div>
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
  );
}

export default function PublicLayout() {
  return (
    <LanguageProvider>
      <PublicInner />
    </LanguageProvider>
  );
}
