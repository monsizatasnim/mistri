import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiRequest } from '../api/client';
import TextField from '../components/TextField.jsx';

export default function RegisterShopOwnerPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    ownerName: '',
    email: '',
    phone: '',
    password: '',
    shopName: '',
    location: '',
    contactInfo: '',
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

    const body = {
      ownerName: form.ownerName.trim(),
      email: form.email.trim(),
      phone: form.phone.trim(),
      password: form.password,
      shopName: form.shopName.trim(),
      location: form.location.trim(),
      contactInfo: form.contactInfo.trim(),
    };

    try {
      await apiRequest('/api/shop-owner/register', { method: 'POST', body });
      navigate('/login', {
        replace: true,
        state: { message: 'Shop registered. An admin will verify it. Please log in.' },
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
        <h1>Register your shop</h1>
        {error && <div className="form-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <TextField id="ownerName" label="Owner name" value={form.ownerName}
            onChange={handleChange} error={fieldErrors.ownerName} />
          <TextField id="email" label="Email" type="email" value={form.email}
            onChange={handleChange} error={fieldErrors.email} />
          <TextField id="phone" label="Phone number" value={form.phone}
            onChange={handleChange} error={fieldErrors.phone} />
          <TextField id="password" label="Password" type="password" value={form.password}
            onChange={handleChange} error={fieldErrors.password} />
          <TextField id="shopName" label="Shop name" value={form.shopName}
            onChange={handleChange} error={fieldErrors.shopName} />
          <TextField id="location" label="Shop location" value={form.location}
            onChange={handleChange} error={fieldErrors.location} />
          <TextField id="contactInfo" label="Shop contact info" value={form.contactInfo}
            onChange={handleChange} error={fieldErrors.contactInfo} />
          <button className="btn" type="submit" disabled={loading}>
            {loading ? 'Please wait...' : 'Register shop'}
          </button>
        </form>
        <div className="links">
          <Link to="/login">I already have an account</Link>
        </div>
      </div>
    </div>
  );
}
