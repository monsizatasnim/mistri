import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import { formRequest } from '../api/formRequest';
import TextField from '../components/TextField.jsx';
import { useT } from '../i18n/languageContext';

export default function ProviderAvailabilityPage() {
  const { t } = useT();
  const [slots, setSlots] = useState([]);
  const [form, setForm] = useState({ date: '', startTime: '', endTime: '' });
  const [policy, setPolicy] = useState({ windowHours: '', refundPercent: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  async function loadSlots() {
    try {
      setSlots((await apiRequest('/api/service-provider/availability')) || []);
    } catch (err) {
      setError(err.message);
    }
  }

  useEffect(() => { loadSlots(); }, []);

  async function addSlot(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await formRequest('/api/service-provider/availability', 'POST', form);
      setSuccess('Slot added.');
      setForm({ date: '', startTime: '', endTime: '' });
      await loadSlots();
    } catch (err) {
      setError(err.message);
    }
  }

  async function removeSlot(id) {
    setError('');
    setSuccess('');
    try {
      await formRequest('/api/service-provider/availability/' + id, 'DELETE');
      setSuccess('Slot removed.');
      await loadSlots();
    } catch (err) {
      setError(err.message);
    }
  }

  async function savePolicy(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await formRequest('/api/service-provider/cancellation-policy', 'PUT', policy);
      setSuccess('Policy saved.');
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="card card-wide">
      <h1>{t('My availability')}</h1>
      {success && <div className="form-success">{t(success)}</div>}
      {error && <div className="form-error">{error}</div>}
      <form onSubmit={addSlot} className="filters">
        <TextField id="date" label={t('Date')} type="date" value={form.date}
          onChange={(e) => setForm({ ...form, date: e.target.value })} />
        <TextField id="startTime" label={t('Start time')} type="time" value={form.startTime}
          onChange={(e) => setForm({ ...form, startTime: e.target.value })} />
        <TextField id="endTime" label={t('End time')} type="time" value={form.endTime}
          onChange={(e) => setForm({ ...form, endTime: e.target.value })} />
        <div className="filter-buttons">
          <button className="btn" type="submit">{t('Add time slot')}</button>
        </div>
      </form>
      {slots.length === 0 && <p>{t('No time slots yet.')}</p>}
      {slots.length > 0 && (
        <table className="data-table">
          <thead>
            <tr><th>{t('Date')}</th><th>{t('Start time')}</th><th>{t('End time')}</th><th></th></tr>
          </thead>
          <tbody>
            {slots.map((s) => (
              <tr key={s.id}>
                <td>{s.date}</td><td>{s.startTime}</td><td>{s.endTime}</td>
                <td><button className="btn-small-red" onClick={() => removeSlot(s.id)}>{t('Remove')}</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <h2>{t('Cancellation policy')}</h2>
      <form onSubmit={savePolicy} className="filters">
        <TextField id="windowHours" label={t('Hours before the job when cancelling is still free')}
          type="number" min="0" value={policy.windowHours}
          onChange={(e) => setPolicy({ ...policy, windowHours: e.target.value })} />
        <TextField id="refundPercent" label={t('Refund percent for late cancellation')}
          type="number" min="0" max="100" value={policy.refundPercent}
          onChange={(e) => setPolicy({ ...policy, refundPercent: e.target.value })} />
        <div className="filter-buttons">
          <button className="btn" type="submit">{t('Save policy')}</button>
        </div>
      </form>
    </div>
  );
}
