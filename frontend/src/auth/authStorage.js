const KEY = 'mistri_auth';

export function saveAuth(data) {
  try {
    localStorage.setItem(KEY, JSON.stringify(data));
  } catch {
    console.error('Could not save login');
  }
}

export function getAuth() {
  try {
    const text = localStorage.getItem(KEY);
    return text ? JSON.parse(text) : null;
  } catch {
    return null;
  }
}

export function getToken() {
  const auth = getAuth();
  return auth ? auth.token : null;
}

export function clearAuth() {
  try {
    localStorage.removeItem(KEY);
  } catch {
    console.error('Could not clear login');
  }
}
