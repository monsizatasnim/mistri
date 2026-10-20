import { useCallback, useState } from 'react';
import { adminApi } from '../adminApi';
import { errorMessage, formatDateTime } from '../format';
import { useLoad } from '../hooks';
import { Badge, Empty, ErrorBox, Loading, Notice, PageHeader, Tabs } from '../ui.jsx';

function AccountList({ kind, onNotice }) {
  const [draft, setDraft] = useState({ search: '', status: '' });
  const [applied, setApplied] = useState({ search: '', status: '' });
  const [busyId, setBusyId] = useState(null);

  const load = useCallback(
    () => adminApi.searchAccounts(kind, applied.search.trim(), applied.status),
    [kind, applied]
  );
  const { data, loading, error, reload } = useLoad(load);
  const isProvider = kind === 'providers';
  const accounts = data || [];

  function search(event) {
    event.preventDefault();
    setApplied({ ...draft });
  }

  async function change(account, action) {
    const verb = action === 'suspend' ? 'Suspend' : 'Reactivate';
    if (!window.confirm(verb + ' ' + account.fullName + '?')) return;
    setBusyId(account.id);
    try {
      await adminApi.changeAccountStatus(kind, account.id, action);
      onNotice({
        kind: 'success',
        text: account.fullName + (action === 'suspend' ? ' was suspended.' : ' was reactivated.'),
      });
      reload();
    } catch (err) {
      onNotice({ kind: 'error', text: errorMessage(err) });
    } finally {
      setBusyId(null);
    }
  }

  return (
    <>
      <form className="adm-inline" onSubmit={search}>
        <div className="adm-field adm-field-wide">
          <label htmlFor="acc-search">Search by name, email or phone</label>
          <input
            id="acc-search"
            type="search"
            value={draft.search}
            onChange={(event) => setDraft({ ...draft, search: event.target.value })}
          />
        </div>
        <div className="adm-field">
          <label htmlFor="acc-status">Status</label>
          <select
            id="acc-status"
            value={draft.status}
            onChange={(event) => setDraft({ ...draft, status: event.target.value })}
          >
            <option value="">All</option>
            <option value="ACTIVE">Active</option>
            <option value="SUSPENDED">Suspended</option>
          </select>
        </div>
        <button type="submit" className="adm-btn adm-btn-primary">
          Search
        </button>
      </form>

      {error && <ErrorBox message={error} onRetry={reload} />}
      {loading && !data && <Loading />}
      {data && accounts.length === 0 && <Empty>No accounts match.</Empty>}
      {accounts.length > 0 && (
        <div className="adm-table-wrap">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                {isProvider && <th>Verification</th>}
                <th>Status</th>
                <th>Joined</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {accounts.map((account) => (
                <tr key={account.id}>
                  <td>
                    {account.fullName}
                    <div className="adm-muted">
                      {isProvider
                        ? account.tradeLicenseNumber && 'License ' + account.tradeLicenseNumber
                        : account.defaultLocation}
                    </div>
                  </td>
                  <td>{account.email}</td>
                  <td>{account.phone}</td>
                  {isProvider && (
                    <td>
                      <Badge value={account.verificationStatus} />
                    </td>
                  )}
                  <td>
                    <Badge value={account.accountStatus} />
                  </td>
                  <td>{formatDateTime(account.createdAt)}</td>
                  <td>
                    {account.accountStatus === 'SUSPENDED' ? (
                      <button
                        type="button"
                        className="adm-btn adm-btn-small adm-btn-good"
                        disabled={busyId === account.id}
                        onClick={() => change(account, 'reactivate')}
                      >
                        Reactivate
                      </button>
                    ) : (
                      <button
                        type="button"
                        className="adm-btn adm-btn-small adm-btn-danger"
                        disabled={busyId === account.id}
                        onClick={() => change(account, 'suspend')}
                      >
                        Suspend
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}

export default function AccountsPage() {
  const [kind, setKind] = useState('customers');
  const [notice, setNotice] = useState(null);

  return (
    <>
      <PageHeader title="Accounts" subtitle="Find an account, then suspend or reactivate it." />
      <Tabs
        tabs={[
          ['customers', 'Customers'],
          ['providers', 'Service providers'],
        ]}
        value={kind}
        onChange={(next) => {
          setKind(next);
          setNotice(null);
        }}
      />
      {notice && (
        <Notice kind={notice.kind} onClose={() => setNotice(null)}>
          {notice.text}
        </Notice>
      )}
      <AccountList key={kind} kind={kind} onNotice={setNotice} />
    </>
  );
}
