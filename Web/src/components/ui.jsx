import { method, phase } from '../format.js';

export function Logo({ big }) {
  return (
    <span className={big ? 'logo big' : 'logo'} aria-hidden="true">
      <svg viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round">
        <path d="M3 17l6-6 4 4 8-8" />
        <path d="M15 7h6v6" />
      </svg>
    </span>
  );
}

// A stable gradient per username so every user gets their own colour.
function hue(name) {
  let h = 0;
  for (const ch of name) h = (h * 31 + ch.charCodeAt(0)) % 360;
  return h;
}

export function Avatar({ name, large }) {
  const h = hue(name || '?');
  return (
    <span
      className={large ? 'avatar lg' : 'avatar'}
      style={{ background: `linear-gradient(135deg, hsl(${h} 85% 60%), hsl(${(h + 60) % 360} 85% 45%))` }}
      aria-hidden="true"
    >
      {(name || '?').charAt(0)}
    </span>
  );
}

export const PhaseBadge = ({ value }) => <span className={`badge phase-${value}`}>{phase(value)}</span>;

export const TypeBadge = ({ value }) => <span className={`badge plain type-${value}`}>{method(value)}</span>;

export const YesNo = ({ value }) => <span className={`badge plain ${value ? 'yes' : 'no'}`}>{value ? 'Yes' : 'No'}</span>;

export const Icons = {
  events: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 3v18h18" />
      <path d="M7 15l4-4 3 3 5-6" />
    </svg>
  ),
  users: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="9" cy="8" r="4" />
      <path d="M2 21c0-4 3-6 7-6s7 2 7 6" />
      <path d="M16 4a4 4 0 0 1 0 8M22 21c0-3-1.5-5-4-5.7" />
    </svg>
  ),
  pointer: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="4" width="18" height="16" rx="3" />
      <path d="M3 9h18M8 14h5M8 17h8" />
    </svg>
  ),
  trophy: (
    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M8 21h8M12 17v4M7 4h10v5a5 5 0 0 1-10 0V4z" />
      <path d="M17 5h3v2a3 3 0 0 1-3 3M7 5H4v2a3 3 0 0 0 3 3" />
    </svg>
  ),
};
