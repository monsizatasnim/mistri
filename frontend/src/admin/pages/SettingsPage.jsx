import { useState } from 'react';
import { adminApi } from '../adminApi';
import { errorMessage, formatDateTime } from '../format';
import { useLoad } from '../hooks';
import { ErrorBox, Loading, Notice, PageHeader } from '../ui.jsx';

const WEIGHTS = [
  ['weightRating', 'Rating'],
  ['weightDistance', 'Distance'],
  ['weightPrice', 'Price'],
  ['weightAvailability', 'Availability'],
];
const KEYS = [
  ...WEIGHTS.map(([key]) => key),
  'bookingFeeMin',
  'bookingFeeMax',
  'minCancellationWindowHours',
  'minRefundPercentage',
];

const toNumber = (text) => (String(text).trim() === '' ? NaN : Number(text));
const hasMoreDecimals = (value, places) => {
  const scaled = value * Math.pow(10, places);
  return Math.abs(scaled - Math.round(scaled)) > 1e-6;
};

// The same rules the backend enforces, so mistakes show up before saving.
function weightTotal(form) {
  return WEIGHTS.reduce((sum, [key]) => sum + Math.round(toNumber(form[key]) * 10000), 0) / 10000;
}

function validate(form) {
  const problems = [];

  WEIGHTS.forEach(([key, label]) => {
    const value = toNumber(form[key]);
    if (Number.isNaN(value)) problems.push(label + ' weight is required.');
    else if (value < 0 || value > 1) problems.push(label + ' weight must be between 0 and 1.');
    else if (hasMoreDecimals(value, 4)) problems.push(label + ' weight can have at most 4 decimal places.');
  });
  if (problems.length === 0 && Math.abs(weightTotal(form) - 1) > 1e-9) {
    problems.push('The four weights must add up to 1 (they add up to ' + weightTotal(form) + ').');
  }

  const feeMin = toNumber(form.bookingFeeMin);
  const feeMax = toNumber(form.bookingFeeMax);
  [
    [feeMin, 'Minimum booking fee'],
    [feeMax, 'Maximum booking fee'],
  ].forEach(([value, label]) => {
    if (Number.isNaN(value)) problems.push(label + ' is required.');
    else if (value < 0) problems.push(label + ' cannot be negative.');
    else if (hasMoreDecimals(value, 2)) problems.push(label + ' can have at most 2 decimal places.');
  });
  if (!Number.isNaN(feeMin) && !Number.isNaN(feeMax) && feeMin > feeMax) {
    problems.push('The minimum booking fee cannot be higher than the maximum.');
  }

  const hours = toNumber(form.minCancellationWindowHours);
  if (Number.isNaN(hours) || !Number.isInteger(hours) || hours < 0 || hours > 720) {
    problems.push('The cancellation window must be a whole number of hours from 0 to 720.');
  }
  const refund = toNumber(form.minRefundPercentage);
  if (Number.isNaN(refund) || !Number.isInteger(refund) || refund < 0 || refund > 100) {
    problems.push('The minimum refund must be a whole number from 0 to 100.');
  }
  return problems;
}

function Field({ id, label, hint, value, onChange, step = 'any' }) {
  return (
    <div className="adm-field">
      <label htmlFor={id}>{label}</label>
      <input id={id} type="number" step={step} value={value} onChange={(event) => onChange(event.target.value)} />
      {hint && <small>{hint}</small>}
    </div>
  );
}

function SettingsForm({ settings, onSaved, onNotice }) {
  const [edits, setEdits] = useState({});
  const [busy, setBusy] = useState(false);
  const [serverErrors, setServerErrors] = useState([]);

  const form = {};
  KEYS.forEach((key) => {
    form[key] = key in edits ? edits[key] : String(settings[key] ?? '');
  });
  const dirty = KEYS.some((key) => key in edits && edits[key] !== String(settings[key] ?? ''));
  const problems = validate(form);
  const total = weightTotal(form);
  const totalOk = !Number.isNaN(total) && Math.abs(total - 1) <= 1e-9;

  const set = (key) => (value) => setEdits({ ...edits, [key]: value });

  async function save(event) {
    event.preventDefault();
    if (problems.length > 0 || !dirty) return;
    setBusy(true);
    setServerErrors([]);
    const body = {};
    KEYS.forEach((key) => {
      body[key] = Number(form[key]);
    });
    try {
      const saved = await adminApi.saveSettings(body);
      setEdits({});
      onNotice({
        kind: 'success',
        text: 'Settings saved' + (saved && saved.updatedBy ? ' by ' + saved.updatedBy : '') + '.',
      });
      onSaved();
    } catch (err) {
      const list = err.data && Array.isArray(err.data.errors) ? err.data.errors : [errorMessage(err)];
      setServerErrors(list);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form onSubmit={save} noValidate>
      {serverErrors.length > 0 && (
        <div className="adm-notice adm-notice-error" role="alert">
          <div>
            The server did not accept these settings:
            <ul className="adm-errors">
              {serverErrors.map((message) => (
                <li key={message}>{message}</li>
              ))}
            </ul>
          </div>
        </div>
      )}

      <div className="adm-card">
        <h2>Ranking weights</h2>
        <p className="adm-muted" style={{ marginBottom: '0.75rem' }}>
          How much each factor counts when providers are ranked for customers. The four must add up to 1.
        </p>
        <div className="adm-form-grid">
          {WEIGHTS.map(([key, label]) => (
            <Field key={key} id={'set-' + key} label={label} value={form[key]} onChange={set(key)} hint="0 to 1" />
          ))}
        </div>
        <p style={{ marginTop: '0.75rem' }} className={totalOk ? 'adm-sum-good' : 'adm-sum-bad'}>
          Total: {Number.isNaN(total) ? '-' : total} {totalOk ? '(correct)' : '(must be 1)'}
        </p>
      </div>

      <div className="adm-card">
        <h2>Booking fee range</h2>
        <div className="adm-form-grid">
          <Field id="set-bookingFeeMin" label="Lowest fee (BDT)" value={form.bookingFeeMin} onChange={set('bookingFeeMin')} />
          <Field id="set-bookingFeeMax" label="Highest fee (BDT)" value={form.bookingFeeMax} onChange={set('bookingFeeMax')} />
        </div>
      </div>

      <div className="adm-card">
        <h2>Cancellation rules providers must follow</h2>
        <div className="adm-form-grid">
          <Field
            id="set-minCancellationWindowHours"
            label="Shortest cancellation window (hours)"
            value={form.minCancellationWindowHours}
            onChange={set('minCancellationWindowHours')}
            step="1"
            hint="0 to 720"
          />
          <Field
            id="set-minRefundPercentage"
            label="Lowest refund (%)"
            value={form.minRefundPercentage}
            onChange={set('minRefundPercentage')}
            step="1"
            hint="0 to 100"
          />
        </div>
      </div>

      {dirty && problems.length > 0 && (
        <div className="adm-notice adm-notice-error" role="alert">
          <ul className="adm-errors">
            {problems.map((message) => (
              <li key={message}>{message}</li>
            ))}
          </ul>
        </div>
      )}
      <div className="adm-actions">
        <button type="submit" className="adm-btn adm-btn-primary" disabled={busy || !dirty || problems.length > 0}>
          {busy ? 'Saving...' : 'Save settings'}
        </button>
        {dirty && (
          <button type="button" className="adm-btn" disabled={busy} onClick={() => setEdits({})}>
            Undo changes
          </button>
        )}
        {settings.updatedAt && (
          <span className="adm-muted">
            Last changed {formatDateTime(settings.updatedAt)}
            {settings.updatedBy ? ' by ' + settings.updatedBy : ''}
          </span>
        )}
      </div>
    </form>
  );
}

export default function SettingsPage() {
  const settings = useLoad(adminApi.settings);
  const log = useLoad(adminApi.settingsAudit);
  const [notice, setNotice] = useState(null);

  function saved() {
    settings.reload();
    log.reload();
  }

  const changes = (log.data || []).slice(0, 50);

  return (
    <>
      <PageHeader title="Platform settings" subtitle="Rules the platform follows. Changing them needs no code change." />
      {notice && (
        <Notice kind={notice.kind} onClose={() => setNotice(null)}>
          {notice.text}
        </Notice>
      )}
      {settings.error && <ErrorBox message={settings.error} onRetry={settings.reload} />}
      {settings.loading && !settings.data && <Loading />}
      {settings.data && <SettingsForm settings={settings.data} onSaved={saved} onNotice={setNotice} />}

      <div className="adm-card" style={{ marginTop: '1.5rem' }}>
        <h2>Change history</h2>
        {log.error && <ErrorBox message={log.error} onRetry={log.reload} />}
        {log.loading && !log.data && <Loading />}
        {log.data && changes.length === 0 && <p className="adm-muted">No changes yet.</p>}
        {changes.length > 0 && (
          <div className="adm-table-wrap">
            <table>
              <thead>
                <tr>
                  <th>When</th>
                  <th>Admin</th>
                  <th>Setting</th>
                  <th>Was</th>
                  <th>Now</th>
                </tr>
              </thead>
              <tbody>
                {changes.map((change, index) => (
                  <tr key={index}>
                    <td>{formatDateTime(change.changedAt)}</td>
                    <td>{change.changedBy}</td>
                    <td>{change.fieldName}</td>
                    <td>{change.oldValue ?? '-'}</td>
                    <td>{change.newValue ?? '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  );
}
