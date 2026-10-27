import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiRequest } from '../api/client';
import TextField from '../components/TextField.jsx';
import { useT } from '../i18n/languageContext';

const TEXT_FIELDS = [
  ['fullName', 'Full name'],
  ['phone', 'Phone number'],
  ['email', 'Email'],
  ['password', 'Password'],
  ['nidNumber', 'NID number'],
  ['tradeLicenseNumber', 'Trade license number'],
];

const FILE_FIELDS = [
  ['nidDocument', 'NID document'],
  ['tradeLicenseDocument', 'Trade license document'],
  ['experienceCertificate', 'Experience certificate'],
];

export default function RegisterProviderPage() {
  const { t } = useT();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '',
    phone: '',
    email: '',
    password: '',
    nidNumber: '',
    tradeLicenseNumber: '',
  });
  const [files, setFiles] = useState({
    nidDocument: null,
    tradeLicenseDocument: null,
    experienceCertificate: null,
  });
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [loading, setLoading] = useState(false);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  function handleFile(event) {
    const file = event.target.files[0] || null;
    setFiles({ ...files, [event.target.name]: file });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');

    // Check everything here first, so the messages are clear.
    const problems = {};
    TEXT_FIELDS.forEach(([name, label]) => {
      if (!form[name].trim()) problems[name] = t(label) + ' ' + t('is required');
    });
    if (form.password && form.password.length < 6) {
      problems.password = t('Password must be at least 6 characters');
    }
    FILE_FIELDS.forEach(([name, label]) => {
      if (!files[name]) problems[name] = t(label) + ' ' + t('is required');
    });
    setFieldErrors(problems);
    if (Object.keys(problems).length > 0) {
      setError(t('Please fix the highlighted fields.'));
      return;
    }

    const data = new FormData();
    TEXT_FIELDS.forEach(([name]) => {
      data.append(name, name === 'password' ? form[name] : form[name].trim());
    });
    FILE_FIELDS.forEach(([name]) => {
      data.append(name, files[name]);
    });

    setLoading(true);
    try {
      await apiRequest('/api/service-provider/register', { method: 'POST', body: data });
      navigate('/login', {
        replace: true,
        state: { message: 'Registered. An admin will verify your documents. Please log in.' },
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
        <h1>{t('Register as a service provider')}</h1>
        {error && <div className="form-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          {TEXT_FIELDS.map(([name, label]) => (
            <TextField
              key={name}
              id={name}
              label={t(label)}
              type={name === 'password' ? 'password' : name === 'email' ? 'email' : 'text'}
              value={form[name]}
              onChange={handleChange}
              error={fieldErrors[name]}
            />
          ))}
          {FILE_FIELDS.map(([name, label]) => (
            <div className="field" key={name}>
              <label htmlFor={name}>{t(label)}</label>
              <input
                id={name}
                name={name}
                type="file"
                accept=".pdf,.jpg,.jpeg,.png"
                onChange={handleFile}
              />
              {fieldErrors[name] && <div className="field-error">{fieldErrors[name]}</div>}
            </div>
          ))}
          <button className="btn" type="submit" disabled={loading}>
            {loading ? t('Please wait...') : t('Register')}
          </button>
        </form>
        <div className="links">
          <Link to="/login">{t('I already have an account')}</Link>
        </div>
      </div>
    </div>
  );
}
