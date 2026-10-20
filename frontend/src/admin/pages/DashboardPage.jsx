import { Link } from 'react-router-dom';
import { adminApi } from '../adminApi';
import { errorMessage, formatMoney } from '../format';
import { useLoad } from '../hooks';
import { ErrorBox, Loading, PageHeader } from '../ui.jsx';

const KEYS = ['summary', 'providers', 'shops', 'submitted', 'underReview'];

// Each box loads on its own, so one failing report does not blank the whole dashboard.
async function loadDashboard() {
  const results = await Promise.allSettled([
    adminApi.summary(),
    adminApi.pendingProviders(),
    adminApi.pendingShops(),
    adminApi.complaints('SUBMITTED'),
    adminApi.complaints('UNDER_REVIEW'),
  ]);

  const loginProblem = results.find(
    (result) =>
      result.status === 'rejected' &&
      result.reason &&
      (result.reason.status === 401 || result.reason.status === 403)
  );
  if (loginProblem) throw loginProblem.reason;

  const out = {};
  results.forEach((result, index) => {
    out[KEYS[index]] =
      result.status === 'fulfilled' ? { value: result.value } : { error: errorMessage(result.reason) };
  });
  return out;
}

function Stat({ label, box, show, to, note }) {
  const failed = box && box.error;
  const content = (
    <>
      <div className="adm-stat-label">{label}</div>
      <div className="adm-stat-value" title={failed ? box.error : undefined}>
        {failed ? 'n/a' : box ? show(box.value) : '-'}
      </div>
      {note && <div className="adm-stat-note">{failed ? 'Could not load' : note}</div>}
    </>
  );
  return to ? (
    <Link className="adm-stat" to={to}>
      {content}
    </Link>
  ) : (
    <div className="adm-stat">{content}</div>
  );
}

export default function DashboardPage() {
  const { data, loading, error, reload } = useLoad(loadDashboard);

  if (loading && !data) return <Loading />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;

  const count = (value) => (Array.isArray(value) ? value.length : 0);
  const summary = data.summary && data.summary.value;

  return (
    <>
      <PageHeader title="Dashboard" subtitle="What needs attention, and how the platform did in the last 30 days.">
        <button type="button" className="adm-btn" onClick={reload} disabled={loading}>
          {loading ? 'Refreshing...' : 'Refresh'}
        </button>
      </PageHeader>

      <h2>Waiting for you</h2>
      <div className="adm-grid">
        <Stat label="Providers to verify" box={data.providers} show={count} to="/admin/verification" note="Open the verification queue" />
        <Stat label="Shops to verify" box={data.shops} show={count} to="/admin/verification" note="Open the verification queue" />
        <Stat label="New complaints" box={data.submitted} show={count} to="/admin/disputes" note="Not reviewed yet" />
        <Stat label="Complaints under review" box={data.underReview} show={count} to="/admin/disputes" note="Waiting for a decision" />
      </div>

      <h2>Last 30 days</h2>
      <div className="adm-grid">
        <Stat label="Bookings" box={data.summary} show={(s) => s.bookings.total} to="/admin/reports" />
        <Stat
          label="Cancellation rate"
          box={data.summary}
          show={(s) => s.bookings.cancellationRatePercent + '%'}
          to="/admin/reports"
          note={summary ? summary.bookings.cancelled + ' cancelled' : undefined}
        />
        <Stat
          label="Average provider rating"
          box={data.summary}
          show={(s) => (s.ratings.averageRating === null ? 'No reviews' : s.ratings.averageRating + ' / 5')}
          to="/admin/reports"
          note={summary ? summary.ratings.reviewCount + ' reviews' : undefined}
        />
        <Stat
          label="Refunds issued"
          box={data.summary}
          show={(s) => formatMoney(s.disputes.totalRefundAmount)}
          to="/admin/reports"
          note={summary ? summary.disputes.total + ' complaints' : undefined}
        />
      </div>
    </>
  );
}
