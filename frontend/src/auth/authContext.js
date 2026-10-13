import { createContext, useContext } from 'react';

export const AuthContext = createContext(null);

export function useAuth() {
  return useContext(AuthContext);
}

// Which portal each role should land on after login.
export function homePathForRole(role) {
  switch (role) {
    case 'ROLE_CUSTOMER':
      return '/customer';
    case 'ROLE_PROVIDER':
      return '/provider';
    case 'ROLE_SHOP_OWNER':
      return '/shop-owner';
    case 'ROLE_ADMIN':
      return '/admin';
    default:
      return '/login';
  }
}
