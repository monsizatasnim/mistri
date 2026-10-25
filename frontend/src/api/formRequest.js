import { getToken } from '../auth/authStorage';

// Sends form fields (the provider endpoints use form parameters, not JSON).
export async function formRequest(path, method, fields) {
  const params = new URLSearchParams();
  Object.entries(fields || {}).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') params.set(k, v);
  });
  let response;
  try {
    response = await fetch(path, {
      method,
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        Authorization: 'Bearer ' + getToken(),
      },
      body: method === 'DELETE' ? undefined : params.toString(),
    });
  } catch {
    throw new Error('Cannot reach the server. Please try again.');
  }
  const text = await response.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  if (response.ok) return data;
  const msg = data && typeof data === 'object' ? (data.message || data.error) : null;
  throw new Error(msg || 'Something went wrong. Please try again.');
}
