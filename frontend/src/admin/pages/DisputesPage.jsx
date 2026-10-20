import { useCallback, useState } from 'react';
import { adminApi } from '../adminApi';
import { errorMessage, formatDateTime, formatMoney, titleCase } from '../format';
import { useLoad } from '../hooks';
import { Badge, Empty, ErrorBox, Facts, Loading, Notice, PageHeader } from '../ui.jsx';

const FILTERS = [
  ['', 'All'],
  ['SUBMITTED', 'New'],
  ['UNDER_REVIEW', 'Under review'],
  ['RESOLVED', 'Resolved'],
  ['CLOSED', 'Closed'],
];

// The decision that pays money back. The backend asks for an amount only with this one.
const REFUND_DECISION = 'REFUND_ISSUED';

function ResolveForm({ complaintId, decisions, onResolved }) {
  const [decision, setDecision] = useState('');
  const [note, setNote] = useState('');
  const [refund, setRefund] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const needsRefund = decision === REFUND_DECISION;
  const refundNumber = Number(refund);
  const missing = [];
  if (!decision) missing.push('Pick a decision.');
  if (!note.trim()) missing.push('Write a resolution note.');
  if (needsRefund && !(refund !== '' && refundNumber > 0)) missing.push('Enter a refund amount above zero.');

  async function submit(event) {
    event.preventDefault();
    if (missing.length > 0) return;
    if (!window.confirm('Record this resolution? It cannot be changed afterwards.')) return;
    setBusy(true);
    setError('');
    try {
      await adminApi.resolveComplaint(complaintId, {
        decision,
        note: note.trim(),
        refundAmount: needsRefund ? refundNumber : undefined,
      });
      await onResolved();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className="adm-card" onSubmit={submit}>
      <h2>Record a resolution</h2>
      {error && (
        <div className="adm-notice adm-notice-error" role="alert">
          {error}
        </div>
      )}
      <div className="adm-form">
        <div className="adm-field">
          <label htmlFor="res-decision">Decision</label>
          <select id="res-decision" value={decision} onChange={(event) => setDecision(event.target.value)}>
            <option value="">Choose...</option>
            {decisions.map((item) => (
              <option key={item} value={item}>
                {titleCase(item)}
              </option>
            ))}
          </select>
        </div>
        {needsRefund && (
          <div className="adm-field">
            <label htmlFor="res-refund">Refund amount (BDT)</label>
            <input
              id="res-refund"
              type="number"
              min="0"
              step="any"
              value={refund}
              onChange={(event) => setRefund(event.target.value)}
            />
          </div>
        )}
        <div className="adm-field">
          <label htmlFor="res-note">Resolution note</label>
          <textarea id="res-note" value={note} onChange={(event) => setNote(event.target.value)} maxLength={2000} />
          <small>The customer sees this note.</small>
        </div>
        {missing.length > 0 && (decision || note) && <small className="adm-muted">{missing.join(' ')}</small>}
        <div className="adm-actions">
          <button type="submit" className="adm-btn adm-btn-primary" disabled={busy || missing.length > 0}>
            {busy ? 'Saving...' : 'Save resolution'}
          </button>
        </div>
      </div>
    </form>
  );
}

function ComplaintDetail({ id, decisions, onChanged, onNotice }) {
  const load = useCallback(() => adminApi.complaint(id), [id]);
  const { data, loading, error, reload } = useLoad(load);
  const [busy, setBusy] = useState(false);

  if (loading && !data) return <Loading />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;
  if (!data) return null;

  const { complaint, booking, review, resolvedBy } = data;
  const open = complaint.status === 'SUBMITTED' || complaint.status === 'UNDER_REVIEW';

  async function startReview() {
    setBusy(true);
    try {
      await adminApi.startReview(id);
      onNotice({ kind: 'success', text: 'Complaint #' + id + ' is now under review.' });
      reload();
      onChanged();
    } catch (err) {
      onNotice({ kind: 'error', text: errorMessage(err) });
    } finally {
      setBusy(false);
    }
  }

  async function resolved() {
    onNotice({ kind: 'success', text: 'Complaint #' + id + ' was resolved.' });
    reload();
    onChanged();
  }

  return (
    <div>
      <div className="adm-card">
        <h2>
          Complaint #{complaint.id} <Badge value={complaint.status} />
        </h2>
        <Facts
          rows={[
            ['Category', titleCase(complaint.category)],
            ['Submitted', formatDateTime(complaint.createdAt)],
            ['Last update', formatDateTime(complaint.updatedAt)],
            ['Customer id', complaint.customerId],
            ['Booking id', complaint.bookingId],
          ]}
        />
        <h3>What the customer said</h3>
        <p className="adm-quote">{complaint.description}</p>
        {complaint.status === 'SUBMITTED' && (
          <div className="adm-actions">
            <button type="button" className="adm-btn adm-btn-primary" disabled={busy} onClick={startReview}>
              Start review
            </button>
          </div>
        )}
      </div>

      <div className="adm-card">
        <h2>The booking</h2>
        {booking ? (
          <Facts
            rows={[
              ['Booking id', booking.bookingId],
              ['Status', titleCase(booking.status)],
              ['Provider id', booking.providerId],
              ['Appointment', booking.appointmentTime ? formatDateTime(booking.appointmentTime) : null],
            ]}
          />
        ) : (
          <p className="adm-muted">Booking details are not available for this complaint.</p>
        )}
        <h3>Review for this booking</h3>
        {review ? (
          <>
            <Facts rows={[['Rating', review.rating + ' / 5'], ['Written by', review.customerName]]} />
            {review.comment && <p className="adm-quote">{review.comment}</p>}
          </>
        ) : (
          <p className="adm-muted">The customer has not reviewed this booking.</p>
        )}
      </div>

      {open ? (
        <ResolveForm complaintId={complaint.id} decisions={decisions} onResolved={resolved} />
      ) : (
        <div className="adm-card">
          <h2>Resolution</h2>
          <Facts
            rows={[
              ['Decision', titleCase(complaint.resolutionDecision)],
              ['Refund', complaint.refundAmount ? formatMoney(complaint.refundAmount) : null],
              ['Resolved by', resolvedBy],
              ['Resolved at', formatDateTime(complaint.resolvedAt)],
            ]}
          />
          <h3>Note</h3>
          <p className="adm-quote">{complaint.resolutionNote || '-'}</p>
        </div>
      )}
    </div>
  );
}

export default function DisputesPage() {
  const [filter, setFilter] = useState('');
  const [selectedId, setSelectedId] = useState(null);
  const [notice, setNotice] = useState(null);

  const loadList = useCallback(() => adminApi.complaints(filter), [filter]);
  const loadOptions = useCallback(() => adminApi.complaintOptions(), []);
  const list = useLoad(loadList);
  const options = useLoad(loadOptions);

  const complaints = list.data || [];
  const decisions = options.data && options.data.decisions ? options.data.decisions : [];

  return (
    <>
      <PageHeader title="Disputes" subtitle="Review complaints from customers and record how each one was settled." />
      <div className="adm-inline">
        <div className="adm-field">
          <label htmlFor="dis-filter">Show</label>
          <select
            id="dis-filter"
            value={filter}
            onChange={(event) => {
              setFilter(event.target.value);
              setSelectedId(null);
            }}
          >
            {FILTERS.map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {notice && (
        <Notice kind={notice.kind} onClose={() => setNotice(null)}>
          {notice.text}
        </Notice>
      )}
      {options.error && <ErrorBox message={'Could not load the list of decisions. ' + options.error} onRetry={options.reload} />}
      {list.error && <ErrorBox message={list.error} onRetry={list.reload} />}
      {list.loading && !list.data && <Loading />}
      {list.data && complaints.length === 0 && <Empty>No complaints here.</Empty>}

      {complaints.length > 0 && (
        <div className="adm-two-col">
          <ul className="adm-list" aria-label="Complaints">
            {complaints.map((item) => (
              <li key={item.id}>
                <button
                  type="button"
                  className={'adm-list-item' + (item.id === selectedId ? ' adm-list-item-active' : '')}
                  onClick={() => setSelectedId(item.id)}
                >
                  <div className="adm-list-item-title">
                    <span>
                      #{item.id} {titleCase(item.category)}
                    </span>
                    <Badge value={item.status} />
                  </div>
                  <div className="adm-list-item-sub">{item.description}</div>
                  <div className="adm-list-item-sub">{formatDateTime(item.createdAt)}</div>
                </button>
              </li>
            ))}
          </ul>
          {selectedId ? (
            <ComplaintDetail
              key={selectedId}
              id={selectedId}
              decisions={decisions}
              onChanged={list.reload}
              onNotice={setNotice}
            />
          ) : (
            <Empty>Pick a complaint to read it.</Empty>
          )}
        </div>
      )}
    </>
  );
}
