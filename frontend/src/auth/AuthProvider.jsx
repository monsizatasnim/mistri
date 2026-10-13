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

  function logout() {
    clearAuth();
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
