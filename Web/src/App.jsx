import { useCallback, useEffect, useState } from 'react';
import { api } from './api.js';
import Login from './components/Login.jsx';
import Header from './components/Header.jsx';
import EventsView from './components/EventsView.jsx';
import UsersView from './components/UsersView.jsx';
import MessageBox from './components/MessageBox.jsx';
import { Icons } from './components/ui.jsx';

const STORAGE_KEY = 'guessmarket.username';

function readStoredUser() {
  try {
    return sessionStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
}

function storeUser(username) {
  try {
    if (username) sessionStorage.setItem(STORAGE_KEY, username);
    else sessionStorage.removeItem(STORAGE_KEY);
  } catch {
    // storage unavailable - the session simply won't survive a page reload
  }
}

export default function App() {
  const [username, setUsername] = useState(null);
  const [checkingSession, setCheckingSession] = useState(true);
  const [tab, setTab] = useState('events');
  // Bumped after every successful action so the visible views refresh immediately.
  const [refreshKey, setRefreshKey] = useState(0);
  const [message, setMessage] = useState(null);

  // After a page reload, keep the user logged in as long as the server still knows them.
  useEffect(() => {
    const stored = readStoredUser();
    if (!stored) {
      setCheckingSession(false);
      return;
    }
    api
      .getUser(stored)
      .then(() => setUsername(stored))
      .catch(() => storeUser(null))
      .finally(() => setCheckingSession(false));
  }, []);

  const handleLoggedIn = (name) => {
    storeUser(name);
    setUsername(name);
    setTab('events');
  };

  const handleSessionLost = useCallback(() => {
    storeUser(null);
    setUsername(null);
  }, []);

  const handleLogout = async () => {
    try {
      await api.logout();
    } catch {
      // ignore - we are leaving anyway
    }
    handleSessionLost();
  };

  const onChanged = useCallback(() => setRefreshKey((k) => k + 1), []);
  const showMessage = useCallback((title, text) => setMessage({ title, text }), []);

  if (checkingSession) return null;

  if (!username) return <Login onLoggedIn={handleLoggedIn} />;

  const viewProps = { username, refreshKey, onChanged, showMessage, onSessionLost: handleSessionLost };

  return (
    <div className="app">
      <Header username={username} onLogout={handleLogout} />
      <div className="tabs glass" role="tablist">
        <button role="tab" aria-selected={tab === 'events'} className={tab === 'events' ? 'tab active' : 'tab'} onClick={() => setTab('events')}>
          {Icons.events}
          Events
        </button>
        <button role="tab" aria-selected={tab === 'users'} className={tab === 'users' ? 'tab active' : 'tab'} onClick={() => setTab('users')}>
          {Icons.users}
          Users
        </button>
      </div>
      <div className="tab-content" key={tab}>
        {tab === 'events' ? <EventsView {...viewProps} /> : <UsersView {...viewProps} />}
      </div>
      {message && <MessageBox title={message.title} text={message.text} onClose={() => setMessage(null)} />}
    </div>
  );
}
