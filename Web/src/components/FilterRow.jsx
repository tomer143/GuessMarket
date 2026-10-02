// A row of toggle buttons with an "All" toggle, matching the JFX FilterButtonGroup:
// toggling "All" sets every option; turning off any option turns "All" off;
// an empty selection matches everything.
export default function FilterRow({ label, options, selected, onChange }) {
  const allOn = options.every((o) => selected.includes(o.value));

  const toggle = (value) =>
    onChange(selected.includes(value) ? selected.filter((v) => v !== value) : [...selected, value]);

  return (
    <div className="filter-row">
      <span className="filter-label">{label}</span>
      <button
        type="button"
        className={allOn ? 'toggle on' : 'toggle'}
        aria-pressed={allOn}
        onClick={() => onChange(allOn ? [] : options.map((o) => o.value))}
      >
        All
      </button>
      {options.map((o) => (
        <button
          key={o.value}
          type="button"
          className={selected.includes(o.value) ? 'toggle on' : 'toggle'}
          aria-pressed={selected.includes(o.value)}
          onClick={() => toggle(o.value)}
        >
          {o.label}
        </button>
      ))}
    </div>
  );
}

export const matches = (selected, value) => selected.length === 0 || selected.includes(value);
