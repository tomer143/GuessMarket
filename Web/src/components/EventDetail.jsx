import { useState } from 'react';
import { api } from '../api.js';
import { decimal, feeCollection } from '../format.js';
import { usePolling } from '../usePolling.js';
import OpenEventDialog from './dialogs/OpenEventDialog.jsx';
import BuySharesDialog from './dialogs/BuySharesDialog.jsx';
import SubmitOrderDialog from './dialogs/SubmitOrderDialog.jsx';
import CloseEventDialog from './dialogs/CloseEventDialog.jsx';
import { Icons, PhaseBadge, TypeBadge } from './ui.jsx';

// Details of one event plus the logged-in user's participation in it (same content as the JFX EventDetailPane).
export default function EventDetail({ event, username, refreshKey, onChanged, showMessage }) {
  const [participation, setParticipation] = useState(null);
  const [loadError, setLoadError] = useState('');
  const [dialog, setDialog] = useState(null);

  usePolling(
    async (isCancelled) => {
      try {
        const data =
          event.method === 'LMSR'
            ? await api.getLmsrParticipation(event.id, username)
            : await api.getOrderBookParticipation(event.id, username);
        if (!isCancelled()) {
          setParticipation(data);
          setLoadError('');
        }
      } catch (err) {
        if (!isCancelled()) setLoadError(err.message);
      }
    },
    [event.id, event.method, username, refreshKey]
  );

  const onDone = (title, text) => {
    setDialog(null);
    showMessage(title, text);
    onChanged();
  };
  const dialogProps = { event, onClose: () => setDialog(null), onDone };

  return (
    <div className="event-detail">
      <div className="detail-hero">
        <div className="detail-title">
          {event.name} <span className="muted">(id {event.id})</span>
        </div>
        {event.description && <div className="detail-desc">{event.description}</div>}
        <div className="info-line">
          <TypeBadge value={event.method} />
          <PhaseBadge value={event.phase} />
          <span className="meta">
            Fee <b>{event.feePercent}%</b> {feeCollection(event.feeCollection)}
          </span>
          <span className="meta">
            Market maker <b>{event.mmUsername}</b>
          </span>
          <span className="meta">
            Account <b className="mono">{decimal(event.accountBalance)}</b>
          </span>
        </div>
      </div>

      <div className="button-row">
        {event.phase === 'NOT_ACTIVE' && (
          <button className="primary" onClick={() => setDialog('open')}>
            Open Event
          </button>
        )}
        {event.phase === 'ACTIVE' && (
          <>
            {event.method === 'LMSR' ? (
              <button className="primary" onClick={() => setDialog('buy')}>
                Buy Shares
              </button>
            ) : (
              <button className="primary" onClick={() => setDialog('order')}>
                Submit Order
              </button>
            )}
            <button className="danger" onClick={() => setDialog('close')}>
              Close Event
            </button>
          </>
        )}
      </div>

      {loadError && <div className="error-text">{loadError}</div>}
      {participation &&
        (event.method === 'LMSR' ? (
          <LmsrParticipation data={participation} />
        ) : (
          <OrderBookParticipation data={participation} />
        ))}

      {dialog === 'open' && <OpenEventDialog {...dialogProps} />}
      {dialog === 'buy' && <BuySharesDialog {...dialogProps} />}
      {dialog === 'order' && <SubmitOrderDialog {...dialogProps} />}
      {dialog === 'close' && <CloseEventDialog {...dialogProps} />}
    </div>
  );
}

function Winner({ name }) {
  return (
    <div className="winner">
      {Icons.trophy}
      Winning option: {name}
    </div>
  );
}

function LmsrParticipation({ data }) {
  const history = [...(data.history ?? [])].reverse();
  return (
    <div className="participation">
      {data.winningOptionName && <Winner name={data.winningOptionName} />}
      <div className="option-cards">
        {(data.optionStatuses ?? []).map((option) => (
          <div key={option.name} className={option.name === data.winningOptionName ? 'option-card winner-card' : 'option-card'}>
            <div className="option-name">{option.name}</div>
            <div className="option-chance" style={{ color: chanceColor(option.chance) }}>
              {decimal(option.chance)}
            </div>
            <ChanceBar chance={option.chance} />
            <div className="option-sub">chance · total shares bought {option.totalSharesBought}</div>
          </div>
        ))}
      </div>
      <div className="stat-row">
        <div className="stat">
          <div className="stat-label">Total fee paid</div>
          <div className="stat-value">{decimal(data.totalFeePaid)}</div>
        </div>
        <div className="stat">
          <div className="stat-label">Your trades</div>
          <div className="stat-value">{history.length}</div>
        </div>
      </div>
      <div className="section-title">Your trade history (most recent first)</div>
      {history.length === 0 ? (
        <div className="muted">(no trades yet)</div>
      ) : (
        <ul className="feed">
          {history.map((trade, i) => (
            <li key={history.length - i}>
              <span className="feed-icon">+</span>
              <span>
                Bought <b>{trade.amount}</b> share(s) of "{trade.optionName}"
              </span>
              <span className="feed-amount">for {decimal(trade.pricePaid)}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

// Red (0) -> yellow (0.5) -> green (1), like the JFX LMSR view.
const clamp01 = (v) => Math.max(0, Math.min(1, v));
const chanceColor = (chance) => `hsl(${clamp01(chance) * 140}, 85%, 60%)`;

function ChanceBar({ chance }) {
  const color = chanceColor(chance);
  return (
    <span className="chance-bar" aria-hidden="true">
      <span style={{ width: `${clamp01(chance) * 100}%`, background: color, color }} />
    </span>
  );
}

function OrderBookParticipation({ data }) {
  const holdings = data.holdings ?? [];
  const hasPnl = data.profitLoss !== undefined && data.profitLoss !== null;
  return (
    <div className="participation">
      <div className="section-title">Your holdings</div>
      {holdings.length === 0 ? (
        <div className="muted">(no holdings)</div>
      ) : (
        <div className="option-cards">
          {holdings.map((h) => (
            <div key={h.optionName} className="option-card">
              <div className="option-name">{h.optionName}</div>
              <div className="option-chance">{h.quantity}</div>
              <div className="option-sub">quantity · amount paid {decimal(h.amountPaid)}</div>
            </div>
          ))}
        </div>
      )}
      <div className="stat-row">
        <div className="stat">
          <div className="stat-label">Total fee paid</div>
          <div className="stat-value">{decimal(data.totalFeePaid)}</div>
        </div>
        {hasPnl && (
          <div className="stat">
            <div className="stat-label">Profit/Loss</div>
            <div className={`stat-value ${data.profitLoss >= 0 ? 'positive' : 'negative'}`}>
              {data.profitLoss >= 0 ? '+' : ''}
              {decimal(data.profitLoss)}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
