// Small helpers for showing values on the admin screens.

export function formatDateTime(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString('en-GB', { dateStyle: 'medium', timeStyle: 'short' });
}

export function formatMoney(value) {
  if (value === null || value === undefined || value === '') return '-';
  const number = Number(value);
  if (Number.isNaN(number)) return String(value);
  return 'BDT ' + number.toLocaleString('en-US', { maximumFractionDigits: 2 });
}

// REFUND_ISSUED -> Refund Issued
export function titleCase(value) {
  if (!value) return '-';
  return String(value)
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

export function errorMessage(error) {
  return error && error.message ? error.message : 'Something went wrong. Please try again.';
}

// 2026-10-31 in the browser's own time zone (the format the report endpoints expect)
export function isoDate(date) {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return date.getFullYear() + '-' + month + '-' + day;
}

export function lastDays(days) {
  const to = new Date();
  const from = new Date();
  from.setDate(to.getDate() - (days - 1));
  return { from: isoDate(from), to: isoDate(to) };
}
