import { createContext, useContext } from 'react';

export const LanguageContext = createContext({
  lang: 'en',
  setLang: () => {},
  t: (text) => text,
});

export function useT() {
  return useContext(LanguageContext);
}
