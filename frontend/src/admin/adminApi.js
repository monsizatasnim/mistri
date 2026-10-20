import { apiRequest } from '../api/client';

// ?a=1&b=2 from an object, leaving out empty values
function query(params) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  const text = search.toString();
  return text ? '?' + text : '';
}

// Every call the admin screens make to the backend.
export const adminApi = {
  // verification
  pendingProviders: () => apiRequest('/api/admin/verifications/pending'),
  providerDocuments: (id) => apiRequest('/api/admin/providers/' + id + '/documents'),
  approveProvider: (id) => apiRequest('/api/admin/verifications/' + id + '/approve', { method: 'POST' }),
  rejectProvider: (id, reason) =>
    apiRequest('/api/admin/verifications/' + id + '/reject', { method: 'POST', body: { reason } }),
  pendingShops: () => apiRequest('/api/admin/shop-owner-verifications/pending'),
  approveShop: (id) =>
    apiRequest('/api/admin/shop-owner-verifications/' + id + '/approve', { method: 'POST' }),
  rejectShop: (id, reason) =>
    apiRequest('/api/admin/shop-owner-verifications/' + id + '/reject', { method: 'POST', body: { reason } }),

  // accounts: kind is 'customers' or 'providers', action is 'suspend' or 'reactivate'
  searchAccounts: (kind, search, status) =>
    apiRequest('/api/admin/accounts/' + kind + query({ search, status })),
  changeAccountStatus: (kind, id, action) =>
    apiRequest('/api/admin/accounts/' + kind + '/' + id + '/' + action, { method: 'POST' }),

  // disputes
  complaintOptions: () => apiRequest('/api/admin/complaint-options'),
  complaints: (status) => apiRequest('/api/admin/complaints' + query({ status })),
  complaint: (id) => apiRequest('/api/admin/complaints/' + id),
  startReview: (id) => apiRequest('/api/admin/complaints/' + id + '/start-review', { method: 'PUT' }),
  resolveComplaint: (id, body) =>
    apiRequest('/api/admin/complaints/' + id + '/resolve', { method: 'POST', body }),

  // platform settings
  settings: () => apiRequest('/api/admin/settings'),
  saveSettings: (body) => apiRequest('/api/admin/settings', { method: 'PUT', body }),
  settingsAudit: () => apiRequest('/api/admin/settings/audit'),

  // reports
  summary: (from, to) => apiRequest('/api/admin/reports/summary' + query({ from, to })),
  adminActions: (from, to, category) =>
    apiRequest('/api/admin/reports/admin-actions' + query({ from, to, category })),
};
