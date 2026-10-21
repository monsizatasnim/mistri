import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/authContext';

export default function ProviderLoginPage() {
  const { loginProvider } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      await loginProvider(email.trim(), password);
      navigate('/provider/price', { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <div className="card">
        <h1>Provider log in</h1>
        {error && <div className="form-error" id="login-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="email">Email</label>
            <input id="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <div className="field">
            <label htmlFor="password">Password</label>
            <input id="password" type="password" value={password}
                   onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <button className="btn" id="login-submit" type="submit" disabled={loading}>
            {loading ? 'Please wait...' : 'Log in'}
          </button>
        </form>
      </div>
    </div>
  );
}