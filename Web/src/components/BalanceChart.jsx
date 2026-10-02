// Line chart of the account balance over time (Step vs Balance), drawn as inline SVG.
const WIDTH = 560;
const HEIGHT = 210;
const PAD = { left: 56, right: 18, top: 14, bottom: 32 };

function ticks(min, max, count = 4) {
  if (min === max) return [min];
  const step = (max - min) / count;
  return Array.from({ length: count + 1 }, (_, i) => min + step * i);
}

export default function BalanceChart({ points }) {
  const data = points ?? [];
  const maxStep = Math.max(1, data.length ? data[data.length - 1].step : 1);
  const balances = data.map((p) => p.balance);
  const minY = Math.min(0, ...balances);
  let maxY = Math.max(1, ...balances);
  if (minY === maxY) maxY = minY + 1;

  const plotW = WIDTH - PAD.left - PAD.right;
  const plotH = HEIGHT - PAD.top - PAD.bottom;
  const x = (step) => PAD.left + (step / maxStep) * plotW;
  const y = (bal) => PAD.top + plotH - ((bal - minY) / (maxY - minY)) * plotH;
  const baseline = y(Math.max(minY, 0));

  const line = data.map((p, i) => `${i === 0 ? 'M' : 'L'}${x(p.step).toFixed(1)},${y(p.balance).toFixed(1)}`).join(' ');
  const area =
    data.length > 0
      ? `${line} L${x(data[data.length - 1].step).toFixed(1)},${baseline.toFixed(1)} L${x(data[0].step).toFixed(1)},${baseline.toFixed(1)} Z`
      : '';
  const last = data[data.length - 1];
  const xTicks = [...new Set(ticks(0, maxStep, Math.min(maxStep, 6)).map(Math.round))];

  return (
    <div className="chart">
      <div className="chart-title">Account balance over time</div>
      <svg viewBox={`0 0 ${WIDTH} ${HEIGHT}`} role="img" aria-label="Account balance over time">
        <defs>
          <linearGradient id="lineGrad" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0" stopColor="#7c5cff" />
            <stop offset="0.5" stopColor="#4f8bff" />
            <stop offset="1" stopColor="#00e5c3" />
          </linearGradient>
          <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0" stopColor="#7c5cff" stopOpacity="0.45" />
            <stop offset="1" stopColor="#7c5cff" stopOpacity="0" />
          </linearGradient>
        </defs>
        {ticks(minY, maxY).map((t) => (
          <g key={`y${t}`}>
            <line className="grid" x1={PAD.left} x2={WIDTH - PAD.right} y1={y(t)} y2={y(t)} />
            <text className="tick" x={PAD.left - 8} y={y(t) + 4} textAnchor="end">
              {t.toFixed(0)}
            </text>
          </g>
        ))}
        {xTicks.map((t) => (
          <text key={`x${t}`} className="tick" x={x(t)} y={HEIGHT - PAD.bottom + 16} textAnchor="middle">
            {t}
          </text>
        ))}
        <line className="axis" x1={PAD.left} x2={WIDTH - PAD.right} y1={PAD.top + plotH} y2={PAD.top + plotH} />
        {data.length > 0 && <path className="area" d={area} />}
        {data.length > 0 && <path className="series" d={line} />}
        {last && (
          <>
            <circle className="last-ring" cx={x(last.step)} cy={y(last.balance)} r="4" />
            <circle className="last-dot" cx={x(last.step)} cy={y(last.balance)} r="4" />
          </>
        )}
        <text className="axis-label" x={PAD.left + plotW / 2} y={HEIGHT - 2} textAnchor="middle">
          Step
        </text>
        <text className="axis-label" transform={`translate(12 ${PAD.top + plotH / 2}) rotate(-90)`} textAnchor="middle">
          Balance
        </text>
      </svg>
    </div>
  );
}
