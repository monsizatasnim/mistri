import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/authContext';

export default function PortalPlaceholder({ title }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <div className="page">
      <div className="card">
        <h1>{title}</h1>
        <p>Welcome, {user ? user.fullName : 'guest'}.</p>
        <p>Role: {user ? user.role : 'none'}</p>
        <button className="btn" onClick={handleLogout}>Log out</button>
      </div>
    </div>
  );
}
