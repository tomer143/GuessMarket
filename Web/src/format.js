// Mirrors Client/src/Client/Format.java so both clients display values the same way.

export const decimal = (value) => (value === undefined || value === null ? '-' : Number(value).toFixed(2));

export const feeCollection = (fc) => (fc === 'OnPurchase' ? 'on purchase' : 'on close');

export const phase = (p) => ({ NOT_ACTIVE: 'Not active', ACTIVE: 'Active', CLOSED: 'Closed' })[p] ?? p;

export const method = (m) => (m === 'LMSR' ? 'LMSR' : 'Order Book');

export const fee = (event) => `${event.feePercent}% (${feeCollection(event.feeCollection)})`;
