import { useAuth } from '../auth/authContext';

export default function PortalPlaceholder({ title }) {
  const { user } = useAuth();

  return (
    <div className="card">
      <h1>{title}</h1>
      <p>Welcome, {user ? user.fullName : 'guest'}.</p>
      <p>Role: {user ? user.role : 'none'}</p>
      <p>This page will be built in a later story.</p>
    </div>
  );
}
