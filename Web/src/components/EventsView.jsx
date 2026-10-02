import { useState } from 'react';
import { api } from '../api.js';
import { decimal, fee } from '../format.js';
import { usePolling } from '../usePolling.js';
import FilterRow, { matches } from './FilterRow.jsx';
import EventDetail from './EventDetail.jsx';
import CreateEventDialog from './dialogs/CreateEventDialog.jsx';
import { Icons, PhaseBadge, TypeBadge } from './ui.jsx';

const TYPE_OPTIONS = [
  { value: 'LMSR', label: 'LMSR' },
  { value: 'ORDER_BOOK', label: 'Order Book' },
];
const STATUS_OPTIONS = [
  { value: 'NOT_ACTIVE', label: 'Not active' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'CLOSED', label: 'Closed' },
];
const FEE_OPTIONS = [
  { value: 'OnPurchase', label: 'on purchase' },
  { value: 'OnClose', label: 'on close' },
];
const all = (options) => options.map((o) => o.value);

export default function EventsView({ username, refreshKey, onChanged, showMessage }) {
  const [events, setEvents] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [types, setTypes] = useState(all(TYPE_OPTIONS));
  const [statuses, setStatuses] = useState(all(STATUS_OPTIONS));
  const [fees, setFees] = useState(all(FEE_OPTIONS));
  const [creating, setCreating] = useState(false);

  usePolling(
    async (isCancelled) => {
      try {
        const data = await api.getEvents();
        if (!isCancelled()) setEvents(data ?? []);
      } catch {
        // keep showing the last known data; the next poll will retry
      }
    },
    [refreshKey]
  );

  const visible = events.filter(
    (e) => matches(types, e.method) && matches(statuses, e.phase) && matches(fees, e.feeCollection)
  );
  const selected = events.find((e) => e.id === selectedId);

  return (
    <div className="split split-events">
      <div className="split-left glass">
        <div className="panel-head">
          <h2>
            Events<span className="count">{visible.length}/{events.length}</span>
          </h2>
          <button className="primary" onClick={() => setCreating(true)}>
            + Create Event
          </button>
        </div>
        <div className="filters">
          <FilterRow label="Type:" options={TYPE_OPTIONS} selected={types} onChange={setTypes} />
          <FilterRow label="Status:" options={STATUS_OPTIONS} selected={statuses} onChange={setStatuses} />
          <FilterRow label="Fee method:" options={FEE_OPTIONS} selected={fees} onChange={setFees} />
        </div>
        <div className="table-wrap">
          <table className="table selectable">
            <thead>
              <tr>
                <th>Name</th>
                <th>Status</th>
                <th>Type</th>
                <th>Fee</th>
                <th className="num">Account Balance</th>
              </tr>
            </thead>
            <tbody>
              {visible.length === 0 && (
                <tr>
                  <td colSpan={5} className="muted empty">
                    {events.length === 0 ? 'No events yet. Events uploaded by any user will appear here.' : 'No events match the filters.'}
                  </td>
                </tr>
              )}
              {visible.map((e) => (
                <tr
                  key={e.id}
                  className={e.id === selectedId ? 'selected' : ''}
                  onClick={() => setSelectedId(e.id)}
                  tabIndex={0}
                  onKeyDown={(k) => k.key === 'Enter' && setSelectedId(e.id)}
                >
                  <td><b>{e.name}</b></td>
                  <td><PhaseBadge value={e.phase} /></td>
                  <td><TypeBadge value={e.method} /></td>
                  <td>{fee(e)}</td>
                  <td className="num">{decimal(e.accountBalance)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      <div className="split-right glass">
        {selected ? (
          <EventDetail
            key={selected.id}
            event={selected}
            username={username}
            refreshKey={refreshKey}
            onChanged={onChanged}
            showMessage={showMessage}
          />
        ) : (
          <div className="placeholder">
            {Icons.pointer}
            Select an event to see its details.
          </div>
        )}
      </div>
      {creating && (
        <CreateEventDialog
          onClose={() => setCreating(false)}
          onDone={(title, text) => {
            setCreating(false);
            showMessage(title, text);
            onChanged();
          }}
        />
      )}
    </div>
  );
}
