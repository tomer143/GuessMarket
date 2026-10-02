// Thin wrapper around the Guess Market server endpoints (the same ones the JavaFX client uses).
// All parameters go in the query string; errors come back as plain text with a non-2xx status.

const BASE = '/GuessMarket';

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}

async function request(method, path, params = {}) {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null) query.append(key, String(value));
  }
  const qs = query.toString();
  let res;
  try {
    res = await fetch(`${BASE}${path}${qs ? `?${qs}` : ''}`, { method, credentials: 'same-origin' });
  } catch (e) {
    throw new ApiError(`Could not reach the server: ${e.message}`, 0);
  }
  const text = await res.text();
  if (!res.ok) {
    throw new ApiError(text || `Request failed (${res.status})`, res.status);
  }
  return text ? JSON.parse(text) : null;
}

const get = (path, params) => request('GET', path, params);
const post = (path, params) => request('POST', path, params);

export const api = {
  login: (username) => post('/login', { username }),
  logout: () => post('/logout'),

  getEvents: () => get('/events'),
  createLmsrEvent: (common, liquidityB) => post('/events', { method: 'LMSR', ...common, liquidityB }),
  createOrderBookEvent: (common, baseValue, initialAmount, allowMint) =>
    post('/events', { method: 'ORDER_BOOK', ...common, baseValue, initialAmount, allowMint }),
  openEvent: (eventId) => post('/event/open', { eventId }),
  buyShares: (eventId, optionIndex, amount) => post('/event/buy', { eventId, optionIndex, amount }),
  submitOrder: (eventId, optionIndex, side, quantity, price) =>
    post('/event/orders', { eventId, optionIndex, side, quantity, price }),
  closeEvent: (eventId, winningOptionIndex) => post('/event/close', { eventId, winningOptionIndex }),
  getLmsrParticipation: (eventId, username) => get('/event/participation/lmsr', { eventId, username }),
  getOrderBookParticipation: (eventId, username) =>
    get('/event/participation/orderbook', { eventId, username }),

  getUsers: () => get('/users'),
  getUser: (username) => get('/user', { username }),
  getBalanceHistory: (username) => get('/user/balance-history', { username }),
  getLedger: (username) => get('/user/ledger', { username }),
  deposit: (username, amount) => post('/user/deposit', { username, amount }),
};
