import { useState } from 'react';
import { api } from '../api.js';
import { decimal, phase } from '../format.js';
import { usePolling } from '../usePolling.js';
import { Avatar, Logo } from './ui.jsx';

const PHASE_COLORS = { ACTIVE: 'var(--positive)', NOT_ACTIVE: 'var(--amber)', CLOSED: '#a3a9cf' };

export default function Header({ username, onLogout }) {
  return (
    <>
      <header className="header glass">
        <div className="brand">
          <Logo />
          <span className="gradient-text">Guess Market</span>
        </div>
        <div className="header-spacer" />
        <div className="user-chip">
          <Avatar name={username} />
          <span className="who">
            <small>Logged in as:</small>
            {username}
          </span>
        </div>
        <button className="danger" onClick={onLogout}>
          Log Out
        </button>
      </header>
      <Ticker />
    </>
  );
}

// Scrolling market tape with every event on the server.
function Ticker() {
  const [events, setEvents] = useState([]);

  usePolling(async (isCancelled) => {
    try {
      const data = await api.getEvents();
      if (!isCancelled()) setEvents(data ?? []);
    } catch {
      // keep the last known tape
    }
  }, []);

  if (events.length === 0) {
    return <div className="ticker ticker-empty">MARKET TAPE · waiting for events…</div>;
  }

  const items = (prefix) =>
    events.map((e) => (
    <span key={`${prefix}${e.id}`} className="ticker-item" aria-hidden={prefix ? 'true' : undefined}>
      <span className="dot" style={{ background: PHASE_COLORS[e.phase] }} />
      <b>{e.name}</b>
      <span className="muted">{phase(e.phase)}</span>
      <span>{decimal(e.accountBalance)}</span>
    </span>
    ));

  return (
    <div className="ticker" style={{ '--ticker-duration': `${Math.max(20, events.length * 6)}s` }}>
      {/* Content is duplicated so the -50% scroll loops seamlessly. */}
      <div className="ticker-track">
        {items('')}
        {items('dup-')}
      </div>
    </div>
  );
}
