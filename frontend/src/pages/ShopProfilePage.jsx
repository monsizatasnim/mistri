import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiRequest } from '../api/client';
import { useAuth } from '../auth/authContext';
import TextField from '../components/TextField.jsx';

const STATUS_TEXT = {
  PENDING: 'Waiting for admin approval. You can edit your details meanwhile.',
  VERIFIED: 'Approved. Providers can see your shop.',
  REJECTED: 'Rejected by the admin. Please check your details and contact support.',
};

export default function ShopProfilePage() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    ownerName: '', email: '', phone: '', shopName: '', location: '', contactInfo: '',
  });
  const [original, setOriginal] = useState(null);
  const [status, setStatus] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  function fill(data) {
    setOriginal(data);
    setStatus(data.verificationStatus);
    setForm({
      ownerName: data.ownerName || '',
      email: data.email || '',
      phone: data.phone || '',
      shopName: data.shopName || '',
      location: data.location || '',
      contactInfo: data.contactInfo || '',
    });
  }

  useEffect(() => {
    apiRequest('/api/shop-owner/profile')
      .then(fill)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSuccess('');
    setFieldErrors({});
    setSaving(true);
    const body = {
      ownerName: form.ownerName.trim(),
      email: form.email.trim(),
      phone: form.phone.trim() === '' ? null : form.phone.trim(),
      shopName: form.shopName.trim(),
      location: form.location.trim(),
      contactInfo: form.contactInfo.trim(),
    };
    try {
      const saved = await apiRequest('/api/shop-owner/profile', { method: 'PUT', body });
      const emailChanged = original && original.email !== saved.email;
      if (emailChanged) {
        logout();
        navigate('/login', {
          replace: true,
          state: { message: 'Email changed. Please log in with your new email.' },
        });
        return;
      }
      fill(saved);
      setSuccess('Profile saved.');
    } catch (err) {
      setError(err.message);
      setFieldErrors(err.fieldErrors || {});
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <div className="card">Loading...</div>;
  }

  return (
    <div className="card">
      <h1>Shop profile</h1>
      {status && (
        <div className="status-line">
          <span className={'badge badge-' + status}>{status}</span>
          <span className="small">{STATUS_TEXT[status]}</span>
        </div>
      )}
      {success && <div className="form-success">{success}</div>}
      {error && <div className="form-error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <TextField id="ownerName" label="Owner name" value={form.ownerName}
          onChange={handleChange} error={fieldErrors.ownerName} />
        <TextField id="email" label="Email (used to log in)" type="email" value={form.email}
          onChange={handleChange} error={fieldErrors.email} />
        <TextField id="phone" label="Phone number (optional)" value={form.phone}
          onChange={handleChange} error={fieldErrors.phone} />
        <TextField id="shopName" label="Shop name" value={form.shopName}
          onChange={handleChange} error={fieldErrors.shopName} />
        <TextField id="location" label="Shop location" value={form.location}
          onChange={handleChange} error={fieldErrors.location} />
        <TextField id="contactInfo" label="Shop contact info" value={form.contactInfo}
          onChange={handleChange} error={fieldErrors.contactInfo} />
        <button className="btn" type="submit" disabled={saving}>
          {saving ? 'Please wait...' : 'Save profile'}
        </button>
      </form>
    </div>
  );
}
