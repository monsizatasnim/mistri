import { useCallback, useState } from 'react';
import { adminApi } from '../adminApi';
import { formatDateTime, formatMoney, lastDays, titleCase } from '../format';
import { useLoad } from '../hooks';
import { Badge, Empty, ErrorBox, Loading, PageHeader } from '../ui.jsx';

// A label, a count and a bar, for status and decision breakdowns.
function Breakdown({ counts }) {
  const entries = Object.entries(counts || {});
  if (entries.length === 0) return <p className="adm-muted">Nothing in this period.</p>;
  const biggest = Math.max(...entries.map(([, count]) => count));
  return (
    <div>
      {entries.map(([label, count]) => (
        <div className="adm-bar-row" key={label}>
          <span>{titleCase(label)}</span>
          <div className="adm-bar">
            <span style={{ width: (biggest ? (count / biggest) * 100 : 0) + '%' }} />
          </div>
          <strong>{count}</strong>
        </div>
      ))}
    </div>
  );
}

function SummaryReport({ range }) {
  const load = useCallback(() => adminApi.summary(range.from, range.to), [range]);
  const { data, loading, error, reload } = useLoad(load);

  if (loading && !data) return <Loading />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;
  if (!data) return null;

  const { bookings, ratings, disputes } = data;
  return (
    <>
      <div className="adm-grid">
        <div className="adm-stat">
          <div className="adm-stat-label">Bookings</div>
          <div className="adm-stat-value">{bookings.total}</div>
        </div>
        <div className="adm-stat">
          <div className="adm-stat-label">Cancellation rate</div>
          <div className="adm-stat-value">{bookings.cancellationRatePercent}%</div>
          <div className="adm-stat-note">{bookings.cancelled} cancelled</div>
        </div>
        <div className="adm-stat">
          <div className="adm-stat-label">Average provider rating</div>
          <div className="adm-stat-value">{ratings.averageRating === null ? 'No reviews' : ratings.averageRating + ' / 5'}</div>
          <div className="adm-stat-note">{ratings.reviewCount} reviews</div>
        </div>
        <div className="adm-stat">
          <div className="adm-stat-label">Complaints</div>
          <div className="adm-stat-value">{disputes.total}</div>
          <div className="adm-stat-note">Refunds {formatMoney(disputes.totalRefundAmount)}</div>
        </div>
      </div>

      <div className="adm-card">
        <h2>Bookings by status</h2>
        <Breakdown counts={bookings.byStatus} />
      </div>

      <div className="adm-card">
        <h2>Provider ratings</h2>
        {ratings.byProvider.length === 0 ? (
          <p className="adm-muted">No reviews in this period.</p>
        ) : (
          <div className="adm-table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Provider</th>
                  <th>Reviews</th>
                  <th>Average rating</th>
                </tr>
              </thead>
              <tbody>
                {ratings.byProvider.map((row) => (
                  <tr key={row.providerId}>
                    <td>{row.providerName}</td>
                    <td>{row.reviewCount}</td>
                    <td>{row.averageRating} / 5</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="adm-two-col" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))' }}>
        <div className="adm-card">
          <h2>Complaints by status</h2>
          <Breakdown counts={disputes.byStatus} />
        </div>
        <div className="adm-card">
          <h2>How complaints were settled</h2>
          <Breakdown counts={disputes.byDecision} />
        </div>
      </div>
    </>
  );
}

function ActionLog({ range }) {
  const [category, setCategory] = useState('');
  const load = useCallback(() => adminApi.adminActions(range.from, range.to, category), [range, category]);
  const { data, loading, error, reload } = useLoad(load);
  const rows = data || [];

  return (
    <div className="adm-card">
      <h2>Admin actions</h2>
      <div className="adm-inline">
        <div className="adm-field">
          <label htmlFor="log-category">Kind of action</label>
          <select id="log-category" value={category} onChange={(event) => setCategory(event.target.value)}>
            <option value="">All</option>
            <option value="VERIFICATION">Verification</option>
            <option value="SUSPENSION">Suspension</option>
            <option value="SETTINGS">Settings</option>
          </select>
        </div>
      </div>
      {error && <ErrorBox message={error} onRetry={reload} />}
      {loading && !data && <Loading />}
      {data && rows.length === 0 && <Empty>No admin actions in this period.</Empty>}
      {rows.length > 0 && (
        <div className="adm-table-wrap">
          <table>
            <thead>
              <tr>
                <th>When</th>
                <th>Admin</th>
                <th>Kind</th>
                <th>Action</th>
                <th>On</th>
                <th>Details</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row, index) => (
                <tr key={index}>
                  <td>{formatDateTime(row.performedAt)}</td>
                  <td>{row.adminName}</td>
                  <td>
                    <Badge value={row.category} />
                  </td>
                  <td>{titleCase(row.action)}</td>
                  <td>{row.targetId ? titleCase(row.targetType) + ' #' + row.targetId : titleCase(row.targetType)}</td>
                  <td>{row.detail || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default function ReportsPage() {
  const [draft, setDraft] = useState(() => lastDays(30));
  const [range, setRange] = useState(draft);
  const [problem, setProblem] = useState('');

  function apply(event) {
    event.preventDefault();
    if (!draft.from || !draft.to) {
      setProblem('Choose both dates.');
    } else if (draft.from > draft.to) {
      setProblem('The first date cannot be after the last date.');
    } else {
      setProblem('');
      setRange({ ...draft });
    }
  }

  return (
    <>
      <PageHeader title="Reports" subtitle="How the platform performed in the dates you choose." />
      <form className="adm-inline" onSubmit={apply}>
        <div className="adm-field">
          <label htmlFor="rep-from">From</label>
          <input id="rep-from" type="date" value={draft.from} onChange={(event) => setDraft({ ...draft, from: event.target.value })} />
        </div>
        <div className="adm-field">
          <label htmlFor="rep-to">To</label>
          <input id="rep-to" type="date" value={draft.to} onChange={(event) => setDraft({ ...draft, to: event.target.value })} />
        </div>
        <button type="submit" className="adm-btn adm-btn-primary">
          Show report
        </button>
        <button type="button" className="adm-btn" onClick={() => { const days = lastDays(30); setDraft(days); setRange(days); setProblem(''); }}>
          Last 30 days
        </button>
      </form>
      {problem && (
        <div className="adm-notice adm-notice-error" role="alert">
          {problem}
        </div>
      )}
      <SummaryReport range={range} />
      <ActionLog range={range} />
    </>
  );
}
