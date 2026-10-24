import { Link } from 'react-router-dom';
import { useAuth } from '../auth/authContext';

export default function PortalPlaceholder({ title }) {
  const { user } = useAuth();

  return (
    <div className="card">
      <h1>{title}</h1>
      <p>Welcome, {user ? user.fullName : 'guest'}.</p>
      <p>Role: {user ? user.role : 'none'}</p>
      {user && user.role === 'ROLE_CUSTOMER' && (
        <p><Link to="/customer/providers">Find and compare providers</Link></p>
      )}
      {user && user.role === 'ROLE_PROVIDER' && (
        <p><Link to="/provider/shops">Find nearby partner shops</Link></p>
      )}
      {user && user.role === 'ROLE_SHOP_OWNER' && (
        <p><Link to="/shop-owner/settings">Shop settings (hours, online orders, discount)</Link></p>
      )}
      {user && user.role === 'ROLE_SHOP_OWNER' && (
        <p><Link to="/shop-owner/orders">Incoming orders</Link></p>
      )}
      {user && user.role === 'ROLE_SHOP_OWNER' && (
        <p><Link to="/shop-owner/profile">Shop profile and approval status</Link></p>
      )}
      <p>More pages will be added in later stories.</p>
    </div>
  );
}
