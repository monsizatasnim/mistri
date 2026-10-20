import { titleCase } from './format';

export function Loading({ text = 'Loading...' }) {
  return <div className="adm-loading">{text}</div>;
}

export function ErrorBox({ message, onRetry }) {
  return (
    <div className="adm-notice adm-notice-error" role="alert">
      <span>{message}</span>
      {onRetry && (
        <button type="button" className="adm-btn adm-btn-small" onClick={onRetry}>
          Try again
        </button>
      )}
    </div>
  );
}

// kind: success | error | info
export function Notice({ kind = 'info', children, onClose }) {
  return (
    <div className={'adm-notice adm-notice-' + kind} role={kind === 'error' ? 'alert' : 'status'}>
      <span>{children}</span>
      {onClose && (
        <button type="button" className="adm-link-button" onClick={onClose} aria-label="Dismiss">
          Dismiss
        </button>
      )}
    </div>
  );
}

function toneOf(value) {
  const text = String(value || '').toUpperCase();
  if (['ACTIVE', 'VERIFIED', 'APPROVED', 'RESOLVED', 'SENT'].includes(text)) return 'good';
  if (['SUSPENDED', 'REJECTED', 'CLOSED', 'CANCELLED'].includes(text)) return 'bad';
  if (['PENDING', 'SUBMITTED', 'UNDER_REVIEW'].includes(text)) return 'warn';
  return 'neutral';
}

export function Badge({ value }) {
  return <span className={'adm-badge adm-badge-' + toneOf(value)}>{titleCase(value)}</span>;
}

export function PageHeader({ title, subtitle, children }) {
  return (
    <div className="adm-page-header">
      <div>
        <h1>{title}</h1>
        {subtitle && <p className="adm-muted">{subtitle}</p>}
      </div>
      {children && <div className="adm-page-actions">{children}</div>}
    </div>
  );
}

export function Empty({ children }) {
  return <div className="adm-empty">{children}</div>;
}

export function Tabs({ tabs, value, onChange }) {
  return (
    <div className="adm-tabs" role="tablist">
      {tabs.map(([key, label]) => (
        <button
          key={key}
          type="button"
          role="tab"
          aria-selected={value === key}
          className={'adm-tab' + (value === key ? ' adm-tab-active' : '')}
          onClick={() => onChange(key)}
        >
          {label}
        </button>
      ))}
    </div>
  );
}

// label, value rows
export function Facts({ rows }) {
  return (
    <dl className="adm-facts">
      {rows.map(([label, value]) => (
        <div key={label} className="adm-fact">
          <dt>{label}</dt>
          <dd>{value === null || value === undefined || value === '' ? '-' : value}</dd>
        </div>
      ))}
    </dl>
  );
}
