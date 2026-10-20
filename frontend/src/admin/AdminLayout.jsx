import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/authContext';
import './admin.css';

const LINKS = [
  ['/admin', 'Dashboard', true],
  ['/admin/verification', 'Verification', false],
  ['/admin/accounts', 'Accounts', false],
  ['/admin/disputes', 'Disputes', false],
  ['/admin/settings', 'Settings', false],
  ['/admin/reports', 'Reports', false],
];

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function signOut() {
    logout();
    navigate('/admin/login');
  }

  return (
    <div className="adm-root">
      <aside className="adm-sidebar">
        <div className="adm-brand">
          Mistri
          <small>Admin portal</small>
        </div>
        <nav className="adm-nav" aria-label="Admin">
          {LINKS.map(([to, label, end]) => (
            <NavLink key={to} to={to} end={end}>
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="adm-sidebar-footer">
          <strong>{user && user.fullName ? user.fullName : 'Admin'}</strong>
          <button type="button" className="adm-btn adm-btn-small" onClick={signOut}>
            Log out
          </button>
        </div>
      </aside>
      <main className="adm-main">
        <Outlet />
      </main>
    </div>
  );
}
