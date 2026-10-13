import { Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/authContext';

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <div className="layout">
      <header className="topbar">
        <strong className="brand">Mistri</strong>
        <div className="topbar-right">
          <span>{user ? user.fullName : ''}</span>
          <button className="btn-small" onClick={handleLogout}>Log out</button>
        </div>
      </header>
      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
