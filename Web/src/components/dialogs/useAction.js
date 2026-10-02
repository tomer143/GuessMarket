import { useState } from 'react';

// Shared submit handling for dialogs: tracks busy state and shows the server's error message.
export function useAction() {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const run = async (fn) => {
    setError('');
    setBusy(true);
    try {
      await fn();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return { busy, error, setError, run };
}
