import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/authContext';

// Lets only a logged-in admin through; everyone else goes to the admin login.
export default function AdminGuard({ children }) {
  const { user } = useAuth();
  const location = useLocation();

  if (!user || user.role !== 'ROLE_ADMIN') {
    return <Navigate to="/admin/login" replace state={{ from: location }} />;
  }
  return children;
}
