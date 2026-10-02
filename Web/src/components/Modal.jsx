import { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';

// Generic OK/Cancel dialog. `onOk` may be async; errors are shown by the caller via `error`.
export default function Modal({ title, header, children, onOk, onCancel, okLabel = 'OK', busy, error, hideCancel }) {
  const formRef = useRef(null);

  // Focus the dialog so Enter/Escape work right away (unless a field already took focus).
  useEffect(() => {
    const form = formRef.current;
    if (form && !form.contains(document.activeElement)) form.querySelector('button[type="submit"]')?.focus();
  }, []);

  useEffect(() => {
    const onKey = (e) => e.key === 'Escape' && onCancel();
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onCancel]);

  const submit = (e) => {
    e.preventDefault();
    if (!busy) onOk();
  };

  // Rendered into <body> so the fixed backdrop is not clipped by the blurred (backdrop-filter) panels.
  return createPortal(
    <div className="modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && onCancel()}>
      <form ref={formRef} className="modal" onSubmit={submit} role="dialog" aria-label={title}>
        <div className="modal-title">{title}</div>
        {header && <div className="modal-header">{header}</div>}
        <div className="modal-body">{children}</div>
        {error && <div className="error-text modal-error">{error}</div>}
        <div className="modal-buttons">
          <button type="submit" className="primary" disabled={busy}>
            {okLabel}
          </button>
          {!hideCancel && (
            <button type="button" onClick={onCancel}>
              Cancel
            </button>
          )}
        </div>
      </form>
    </div>,
    document.body
  );
}
