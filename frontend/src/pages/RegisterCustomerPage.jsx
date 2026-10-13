import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiRequest } from '../api/client';
import TextField from '../components/TextField.jsx';

export default function RegisterCustomerPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '',
    phone: '',
    email: '',
    password: '',
    defaultLocation: '',
  });
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [loading, setLoading] = useState(false);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setFieldErrors({});
    setLoading(true);

    // Email is optional, so only send it when it is filled in.
    const body = {
      fullName: form.fullName.trim(),
      phone: form.phone.trim(),
      password: form.password,
      defaultLocation: form.defaultLocation.trim(),
    };
    if (form.email.trim()) {
      body.email = form.email.trim();
    }

    try {
      await apiRequest('/api/auth/register', { method: 'POST', body });
      navigate('/login', {
        replace: true,
        state: { message: 'Account created. Please log in.' },
      });
    } catch (err) {
      setError(err.message);
      setFieldErrors(err.fieldErrors || {});
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <div className="card">
        <h1>Create customer account</h1>
        {error && <div className="form-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <TextField id="fullName" label="Full name" value={form.fullName}
            onChange={handleChange} error={fieldErrors.fullName} />
          <TextField id="phone" label="Phone number" value={form.phone}
            onChange={handleChange} error={fieldErrors.phone} />
          <TextField id="email" label="Email (optional)" type="email" value={form.email}
            onChange={handleChange} error={fieldErrors.email} />
          <TextField id="password" label="Password" type="password" value={form.password}
            onChange={handleChange} error={fieldErrors.password} />
          <TextField id="defaultLocation" label="Your area (for example Dhaka)" value={form.defaultLocation}
            onChange={handleChange} error={fieldErrors.defaultLocation} />
          <button className="btn" type="submit" disabled={loading}>
            {loading ? 'Please wait...' : 'Create account'}
          </button>
        </form>
        <div className="links">
          <Link to="/login">I already have an account</Link>
        </div>
      </div>
    </div>
  );
}
