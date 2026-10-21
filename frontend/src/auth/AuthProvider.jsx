import { useState } from 'react';
import { AuthContext } from './authContext';
import { apiRequest } from '../api/client';
import { saveAuth, getAuth, clearAuth } from './authStorage';

export default function AuthProvider({ children }) {
  const [user, setUser] = useState(() => getAuth());

  async function login(identifier, password) {
    const data = await apiRequest('/api/auth/login', {
      method: 'POST',
      body: { identifier, password },
    });
    saveAuth(data);
    setUser(data);
    return data;
  }

  // Admins have their own login endpoint. Its answer has no role, so ROLE_ADMIN is added here.
  async function loginAdmin(identifier, password) {
    const data = await apiRequest('/api/auth/admin/login', {
      method: 'POST',
      body: { identifier, password },
    });
    const auth = {
      token: data.token,
      userId: data.adminId,
      fullName: data.fullName,
      role: 'ROLE_ADMIN',
    };
    saveAuth(auth);
    setUser(auth);
    return auth;
  }

  // Provider login: backend returns token + providerId, so role is added here.
  async function loginProvider(email, password) {
    const data = await apiRequest(
      '/api/service-provider/login?email=' + encodeURIComponent(email) +
      '&password=' + encodeURIComponent(password),
      { method: 'POST' }
    );
    const auth = {
      token: data.token,
      userId: data.providerId,
      fullName: data.fullName,
      role: 'ROLE_PROVIDER',
    };
    saveAuth(auth);
    setUser(auth);
    return auth;
  }

  function logout() {
    clearAuth();
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, loginAdmin, loginProvider, logout }}>
      {children}
    </AuthContext.Provider>
  );
}