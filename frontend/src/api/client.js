import { getToken } from '../auth/authStorage';

export class ApiError extends Error {
  constructor(status, message, fieldErrors, data) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors || {};
    // the full error body, for screens that need more than the message
    this.data = data ?? null;
  }
}

async function readBody(response) {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export async function apiRequest(path, options = {}) {
  const headers = { ...(options.headers || {}) };

  // Public login/register routes do not need a token.
  const token = getToken();
  if (token && !path.startsWith('/api/auth/') && !path.endsWith('/register')) {
    headers.Authorization = 'Bearer ' + token;
  }

  let body = options.body;
  if (body && !(body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
    body = JSON.stringify(body);
  }

  let response;
  try {
    response = await fetch(path, {
      method: options.method || 'GET',
      headers,
      body,
    });
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Please try again.');
  }

  const data = await readBody(response);
  if (response.ok) return data;

  let message = 'Something went wrong. Please try again.';
  let fieldErrors = {};
  if (data && typeof data === 'object') {
    if (typeof data.message === 'string') {
      message = data.message;
    } else if (data.error) {
      message = data.error;
    } else {
      fieldErrors = data;
      message = 'Please fix the highlighted fields.';
    }
  }
  throw new ApiError(response.status, message, fieldErrors, data);
}


