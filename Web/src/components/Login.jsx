import { useState } from 'react';
import { api } from '../api.js';
import { Logo } from './ui.jsx';

export default function Login({ onLoggedIn }) {
  const [name, setName] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    const username = name.trim();
    setError('');
    setBusy(true);
    try {
      await api.login(username);
      onLoggedIn(username);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login-page">
      <span className="orb orb-1" />
      <span className="orb orb-2" />
      <span className="orb orb-3" />
      <form className="login-box glass" onSubmit={submit}>
        <Logo big />
        <h1 className="gradient-text">Guess Market</h1>
        <p className="login-tagline">Predict the future. Trade on it.</p>
        <input
          className="login-input"
          placeholder="Username"
          aria-label="Username"
          value={name}
          autoFocus
          onChange={(e) => setName(e.target.value)}
        />
        <button type="submit" className="primary" disabled={busy}>
          {busy ? 'Logging in…' : 'Log In'}
        </button>
        <div className="error-text" role="alert">{error}</div>
        <div className="login-ticks" aria-hidden="true">
          <span /><span /><span /><span /><span />
        </div>
      </form>
    </div>
  );
}
