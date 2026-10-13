export default function TextField({ id, label, error, ...inputProps }) {
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      <input id={id} name={id} {...inputProps} />
      {error && <div className="field-error">{error}</div>}
    </div>
  );
}
