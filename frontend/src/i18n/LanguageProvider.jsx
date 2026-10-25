import { useEffect, useState } from 'react';
import { LanguageContext } from './languageContext';
import { bn } from './translations';

const KEY = 'mistri_lang';

function readLang() {
  try {
    return localStorage.getItem(KEY) === 'bn' ? 'bn' : 'en';
  } catch {
    return 'en';
  }
}

export default function LanguageProvider({ children }) {
  const [lang, setLangState] = useState(readLang);

  useEffect(() => {
    document.documentElement.lang = lang;
  }, [lang]);

  function setLang(next) {
    setLangState(next);
    try {
      localStorage.setItem(KEY, next);
    } catch {
      // saving is optional
    }
  }

  function t(text) {
    if (lang === 'bn' && bn[text]) return bn[text];
    return text;
  }

  return (
    <LanguageContext.Provider value={{ lang, setLang, t }}>
      {children}
    </LanguageContext.Provider>
  );
}
