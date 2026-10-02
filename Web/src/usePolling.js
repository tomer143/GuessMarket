import { useEffect, useRef } from 'react';

export const POLL_INTERVAL_MILLIS = 1000;

// Runs `fn` immediately and then every POLL_INTERVAL_MILLIS until unmounted or `deps` change.
// A new tick is skipped while the previous one is still in flight.
export function usePolling(fn, deps) {
  const fnRef = useRef(fn);
  fnRef.current = fn;

  useEffect(() => {
    let cancelled = false;
    let running = false;
    const tick = async () => {
      if (running || cancelled) return;
      running = true;
      try {
        await fnRef.current(() => cancelled);
      } finally {
        running = false;
      }
    };
    tick();
    const id = setInterval(tick, POLL_INTERVAL_MILLIS);
    return () => {
      cancelled = true;
      clearInterval(id);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);
}
