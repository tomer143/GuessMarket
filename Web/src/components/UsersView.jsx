import { useState } from 'react';
import { api } from '../api.js';
import { decimal } from '../format.js';
import { usePolling } from '../usePolling.js';
import BalanceChart from './BalanceChart.jsx';
import EventDetail from './EventDetail.jsx';
import DepositDialog from './dialogs/DepositDialog.jsx';
import { Avatar, Icons, TypeBadge, YesNo } from './ui.jsx';

export default function UsersView({ username, refreshKey, onChanged, showMessage, onSessionLost }) {
  const [users, setUsers] = useState([]);
  const [events, setEvents] = useState([]);
  const [me, setMe] = useState(null);
  const [history, setHistory] = useState([]);
  const [ledger, setLedger] = useState([]);
  const [selectedEventId, setSelectedEventId] = useState(null);
  const [depositing, setDepositing] = useState(false);

  usePolling(
    async (isCancelled) => {
      try {
        const [u, e, details, h, l] = await Promise.all([
          api.getUsers(),
          api.getEvents(),
          api.getUser(username),
          api.getBalanceHistory(username),
          api.getLedger(username),
        ]);
        if (isCancelled()) return;
        setUsers(u ?? []);
        setEvents(e ?? []);
        setMe(details);
        setHistory(h ?? []);
        setLedger(l ?? []);
      } catch (err) {
        // The server no longer knows us (e.g. it was restarted) - back to the login screen.
        if (!isCancelled() && err.status === 404) onSessionLost();
      }
    },
    [username, refreshKey]
  );

  const marketMakers = new Set(events.map((e) => e.mmUsername));
  const others = users.filter((u) => u.username !== username);
  const participations = me?.participations ?? [];
  const selectedEvent = events.find((e) => e.id === selectedEventId);
  const ledgerNewestFirst = [...ledger].reverse();

  return (
    <div className="split split-users">
      <div className="split-left glass">
        <div className="panel-head">
          <h2>
            Other users<span className="count">{others.length}</span>
          </h2>
        </div>
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Username</th>
                <th className="num">Balance</th>
                <th>Blocked</th>
                <th>Market Maker?</th>
              </tr>
            </thead>
            <tbody>
              {others.length === 0 && (
                <tr>
                  <td colSpan={4} className="muted empty">
                    No other users are logged in.
                  </td>
                </tr>
              )}
              {others.map((u) => (
                <tr key={u.username}>
                  <td>
                    <span className="cell-user">
                      <Avatar name={u.username} />
                      {u.username}
                    </span>
                  </td>
                  <td className="num">{decimal(u.balance)}</td>
                  <td>{u.blocked ? <span className="badge plain blocked">Yes</span> : <YesNo value={false} />}</td>
                  <td><YesNo value={marketMakers.has(u.username)} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="split-right glass">
        {me && (
          <div className="own-detail">
            <div className="profile">
              <Avatar name={me.username} large />
              <div>
                <div className="profile-name">{me.username}</div>
                {me.blocked && <span className="badge plain blocked">BLOCKED</span>}
              </div>
              <div className="spacer" />
              <div>
                <div className="profile-label">Account balance</div>
                <div className="profile-balance gradient-text">{decimal(me.balance)}</div>
              </div>
              <button className="primary" onClick={() => setDepositing(true)}>
                + Deposit Funds
              </button>
            </div>

            <BalanceChart points={history} />

            <div className="section-title">Account activity (most recent first)</div>
            <div className="table-wrap ledger">
              <table className="table">
                <thead>
                  <tr>
                    <th>Description</th>
                    <th className="num">Amount</th>
                    <th className="num">Balance</th>
                  </tr>
                </thead>
                <tbody>
                  {ledgerNewestFirst.length === 0 && (
                    <tr>
                      <td colSpan={3} className="muted empty">
                        No account activity yet.
                      </td>
                    </tr>
                  )}
                  {ledgerNewestFirst.map((row, i) => (
                    <tr key={ledger.length - i}>
                      <td>{row.description}</td>
                      <td className={`num ${row.amount < 0 ? 'negative' : 'positive'}`}>{decimal(row.amount)}</td>
                      <td className="num">{decimal(row.resultingBalance)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="section-title">Your events</div>
            <div className="table-wrap participations">
              <table className="table selectable">
                <thead>
                  <tr>
                    <th>Event</th>
                    <th>Type</th>
                    <th>Role</th>
                  </tr>
                </thead>
                <tbody>
                  {participations.length === 0 && (
                    <tr>
                      <td colSpan={3} className="muted empty">
                        You are not part of any event yet.
                      </td>
                    </tr>
                  )}
                  {participations.map((p) => (
                    <tr
                      key={p.eventId}
                      className={p.eventId === selectedEventId ? 'selected' : ''}
                      onClick={() => setSelectedEventId(p.eventId)}
                      tabIndex={0}
                      onKeyDown={(k) => k.key === 'Enter' && setSelectedEventId(p.eventId)}
                    >
                      <td><b>{p.eventName}</b></td>
                      <td><TypeBadge value={p.method} /></td>
                      <td>
                        <span className={`badge plain ${p.isMm ? 'role-mm' : 'role-p'}`}>
                          {p.isMm ? 'Market maker' : 'Participant'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="nested-detail">
              {selectedEvent ? (
                <EventDetail
                  key={selectedEvent.id}
                  event={selectedEvent}
                  username={username}
                  refreshKey={refreshKey}
                  onChanged={onChanged}
                  showMessage={showMessage}
                />
              ) : (
                <div className="placeholder">
                  {Icons.pointer}
                  Select an event above to see its details.
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {depositing && (
        <DepositDialog
          username={username}
          onClose={() => setDepositing(false)}
          onDone={(title, text) => {
            setDepositing(false);
            showMessage(title, text);
            onChanged();
          }}
        />
      )}
    </div>
  );
}
