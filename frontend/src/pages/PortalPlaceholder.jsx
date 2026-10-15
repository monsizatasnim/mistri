import { Link } from 'react-router-dom';
import { useAuth } from '../auth/authContext';

export default function PortalPlaceholder({ title }) {
  const { user } = useAuth();

  return (
    <div className="card">
      <h1>{title}</h1>
      <p>Welcome, {user ? user.fullName : 'guest'}.</p>
      <p>Role: {user ? user.role : 'none'}</p>
      {user && user.role === 'ROLE_SHOP_OWNER' && (
        <p><Link to="/shop-owner/settings">Shop settings (hours, online orders, discount)</Link></p>
      )}
      <p>More pages will be added in later stories.</p>
    </div>
  );
}

