// One radio button per option name (works for binary and multi-outcome events).
export default function OptionChoice({ name, options, value, onChange }) {
  return (
    <div className="radio-group">
      {options.map((option, index) => (
        <label key={index} className="radio">
          <input type="radio" name={name} checked={value === index} onChange={() => onChange(index)} />
          {option}
        </label>
      ))}
    </div>
  );
}
