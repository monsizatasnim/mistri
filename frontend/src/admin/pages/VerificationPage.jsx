import { useCallback, useState } from 'react';
import { adminApi } from '../adminApi';
import { errorMessage, formatDateTime, formatMoney } from '../format';
import { useLoad } from '../hooks';
import { Badge, Empty, ErrorBox, Facts, Loading, Notice, PageHeader, Tabs } from '../ui.jsx';

// Approve, or reject with a reason. `onApprove` and `onReject` return promises.
function DecisionPanel({ name, onApprove, onReject }) {
  const [rejecting, setRejecting] = useState(false);
  const [reason, setReason] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  async function run(action) {
    setBusy(true);
    setError('');
    try {
      await action();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  function approve() {
    if (!window.confirm('Approve ' + name + '?')) return;
    run(onApprove);
  }

  return (
    <div className="adm-card">
      <h2>Decision</h2>
      {error && (
        <div className="adm-notice adm-notice-error" role="alert">
          {error}
        </div>
      )}
      {!rejecting ? (
        <div className="adm-actions">
          <button type="button" className="adm-btn adm-btn-good" disabled={busy} onClick={approve}>
            Approve
          </button>
          <button type="button" className="adm-btn adm-btn-danger" disabled={busy} onClick={() => setRejecting(true)}>
            Reject...
          </button>
        </div>
      ) : (
        <div className="adm-form">
          <div className="adm-field">
            <label htmlFor="reject-reason">Reason for rejecting (the applicant will see it)</label>
            <textarea id="reject-reason" value={reason} onChange={(event) => setReason(event.target.value)} />
          </div>
          <div className="adm-actions">
            <button
              type="button"
              className="adm-btn adm-btn-danger"
              disabled={busy || !reason.trim()}
              onClick={() => run(() => onReject(reason.trim()))}
            >
              Confirm rejection
            </button>
            <button type="button" className="adm-btn" disabled={busy} onClick={() => setRejecting(false)}>
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

function ProviderDocuments({ id }) {
  const load = useCallback(() => adminApi.providerDocuments(id), [id]);
  const { data, loading, error, reload } = useLoad(load);

  const documents = data
    ? [
        ['NID', data.nidDocumentUrl],
        ['Trade license', data.tradeLicenseDocumentUrl],
        ['Experience certificate', data.experienceCertificateUrl],
      ]
    : [];

  return (
    <div className="adm-card">
      <h2>Documents</h2>
      {loading && !data && <Loading text="Getting secure links..." />}
      {error && <ErrorBox message={error} onRetry={reload} />}
      {data && (
        <>
          <ul className="adm-list">
            {documents.map(([label, url]) => (
              <li key={label} className="adm-inline" style={{ margin: 0 }}>
                <span style={{ minWidth: 190 }}>{label}</span>
                {url ? (
                  <a href={url} target="_blank" rel="noreferrer">
                    Open document
                  </a>
                ) : (
                  <span className="adm-muted">Not uploaded</span>
                )}
              </li>
            ))}
          </ul>
          <p className="adm-muted">
            These links stop working after 10 minutes.{' '}
            <button type="button" className="adm-link-button" onClick={reload}>
              Get new links
            </button>
          </p>
        </>
      )}
    </div>
  );
}

function ProviderQueue({ onNotice }) {
  const load = useCallback(() => adminApi.pendingProviders(), []);
  const { data, loading, error, reload } = useLoad(load);
  const [selectedId, setSelectedId] = useState(null);

  const items = data || [];
  const selected = items.find((item) => item.id === selectedId) || null;

  async function decide(promise, text) {
    await promise;
    onNotice({ kind: 'success', text });
    setSelectedId(null);
    reload();
  }

  return (
    <>
      {error && <ErrorBox message={error} onRetry={reload} />}
      {loading && !data && <Loading />}
      {data && items.length === 0 && <Empty>No providers are waiting for verification.</Empty>}
      {items.length > 0 && (
        <div className="adm-two-col">
          <ul className="adm-list" aria-label="Providers waiting for verification">
            {items.map((item) => (
              <li key={item.id}>
                <button
                  type="button"
                  className={'adm-list-item' + (item.id === selectedId ? ' adm-list-item-active' : '')}
                  onClick={() => setSelectedId(item.id)}
                >
                  <div className="adm-list-item-title">
                    <span>{item.fullName}</span>
                    <Badge value={item.verificationStatus} />
                  </div>
                  <div className="adm-list-item-sub">
                    {item.serviceCategory || 'No category yet'} - applied {formatDateTime(item.createdAt)}
                  </div>
                </button>
              </li>
            ))}
          </ul>

          {selected ? (
            <div key={selected.id}>
              <div className="adm-card">
                <h2>{selected.fullName}</h2>
                <Facts
                  rows={[
                    ['Email', selected.email],
                    ['Phone', selected.phone],
                    ['NID number', selected.nidNumber],
                    ['Trade license number', selected.tradeLicenseNumber],
                    ['Service category', selected.serviceCategory],
                    [
                      'Price range',
                      selected.priceMin != null || selected.priceMax != null
                        ? formatMoney(selected.priceMin) + ' to ' + formatMoney(selected.priceMax)
                        : null,
                    ],
                    ['Applied', formatDateTime(selected.createdAt)],
                  ]}
                />
                {selected.serviceDescription && <p className="adm-quote">{selected.serviceDescription}</p>}
              </div>
              <ProviderDocuments id={selected.id} />
              <DecisionPanel
                name={selected.fullName}
                onApprove={() =>
                  decide(adminApi.approveProvider(selected.id), selected.fullName + ' was approved.')
                }
                onReject={(reason) =>
                  decide(adminApi.rejectProvider(selected.id, reason), selected.fullName + ' was rejected.')
                }
              />
            </div>
          ) : (
            <Empty>Pick a provider to review.</Empty>
          )}
        </div>
      )}
    </>
  );
}

function ShopQueue({ onNotice }) {
  const load = useCallback(() => adminApi.pendingShops(), []);
  const { data, loading, error, reload } = useLoad(load);
  const [selectedId, setSelectedId] = useState(null);

  const items = data || [];
  const selected = items.find((item) => item.id === selectedId) || null;

  async function decide(promise, text) {
    await promise;
    onNotice({ kind: 'success', text });
    setSelectedId(null);
    reload();
  }

  return (
    <>
      {error && <ErrorBox message={error} onRetry={reload} />}
      {loading && !data && <Loading />}
      {data && items.length === 0 && <Empty>No shops are waiting for verification.</Empty>}
      {items.length > 0 && (
        <div className="adm-two-col">
          <ul className="adm-list" aria-label="Shops waiting for verification">
            {items.map((item) => (
              <li key={item.id}>
                <button
                  type="button"
                  className={'adm-list-item' + (item.id === selectedId ? ' adm-list-item-active' : '')}
                  onClick={() => setSelectedId(item.id)}
                >
                  <div className="adm-list-item-title">
                    <span>{item.shopName}</span>
                    <Badge value={item.verificationStatus} />
                  </div>
                  <div className="adm-list-item-sub">
                    {item.ownerName} - {item.location}
                  </div>
                </button>
              </li>
            ))}
          </ul>

          {selected ? (
            <div key={selected.id}>
              <div className="adm-card">
                <h2>{selected.shopName}</h2>
                <Facts
                  rows={[
                    ['Owner', selected.ownerName],
                    ['Email', selected.email],
                    ['Phone', selected.phone],
                    ['Location', selected.location],
                    ['Contact', selected.contactInfo],
                    ['Account', selected.accountStatus],
                  ]}
                />
                <p className="adm-muted">Shops do not upload documents, so there is nothing to open here.</p>
              </div>
              <DecisionPanel
                name={selected.shopName}
                onApprove={() => decide(adminApi.approveShop(selected.id), selected.shopName + ' was approved.')}
                onReject={(reason) =>
                  decide(adminApi.rejectShop(selected.id, reason), selected.shopName + ' was rejected.')
                }
              />
            </div>
          ) : (
            <Empty>Pick a shop to review.</Empty>
          )}
        </div>
      )}
    </>
  );
}

export default function VerificationPage() {
  const [tab, setTab] = useState('providers');
  const [notice, setNotice] = useState(null);

  return (
    <>
      <PageHeader title="Verification queue" subtitle="Check new providers and shops, then approve or reject them." />
      <Tabs
        tabs={[
          ['providers', 'Service providers'],
          ['shops', 'Partner shops'],
        ]}
        value={tab}
        onChange={(next) => {
          setTab(next);
          setNotice(null);
        }}
      />
      {notice && (
        <Notice kind={notice.kind} onClose={() => setNotice(null)}>
          {notice.text}
        </Notice>
      )}
      {tab === 'providers' ? <ProviderQueue onNotice={setNotice} /> : <ShopQueue onNotice={setNotice} />}
    </>
  );
}
