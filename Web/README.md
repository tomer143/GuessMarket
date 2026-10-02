# Guess Market – Web Client (Exercise 4, bonus)

A browser client for the Guess Market server from Exercise 3. It uses **exactly the same HTTP API**
as the JavaFX client and requires **no change** to the server or to the way it is deployed.

Built with React 18 + Vite.

## How to run

1. Start the Guess Market server as usual (the Exercise 3 WAR deployed on Tomcat at
   `http://localhost:8080/GuessMarket`).
2. Double-click **`run.bat`** (in this folder). It runs `npm install` and then starts the web client.
   Node.js is the only requirement.
3. Open **http://localhost:5173** in a browser (the bat file also opens it automatically).

To simulate several users, use separate browser profiles / an incognito window (each login is tied
to the browser's session cookie, just like each JavaFX client).

### Why a dev server?
The browser cannot call `localhost:8080` directly from a page served on another port without CORS
headers, which the server does not send. The Vite dev server therefore serves the page **and** proxies
every request under `/GuessMarket/*` to `http://localhost:8080`. From the browser's point of view
everything comes from one origin, so the server's `JSESSIONID` session cookie works unchanged.

## Supported screens

| Screen | Contents |
|---|---|
| **Login** | Username only (no passwords). A name that is already taken shows the server's error message. |
| **Events** | "Create Event", filters (Type / Status / Fee method), the events table (Name, Status, Type, Fee, Account Balance), and a detail pane for the selected event with Open / Buy Shares / Submit Order / Close actions and your participation in it. |
| **Users** | Other users (username, balance, blocked, market maker?), your own balance with "Deposit Funds", a balance-over-time chart, your account activity (one row per ledger entry, newest first), your events and their details. |

Not supported (by design, per the exercise): uploading event files. Exercise 3 bonuses are not supported.

Data refreshes automatically by polling the server every second (only for the visible tab),
and immediately after every action you perform.

## Code structure

| File | Role |
|---|---|
| `vite.config.js` | Dev server on port 5173 + proxy of `/GuessMarket` to Tomcat. |
| `src/api.js` | One function per server endpoint; turns non-2xx responses into errors carrying the server's text. |
| `src/usePolling.js` | React hook that runs a fetch immediately and then every second. |
| `src/format.js` | Same display formatting as the JavaFX client's `Format.java`. |
| `src/App.jsx` | Login state (kept in `sessionStorage` so a page reload keeps you logged in), header and tabs. |
| `src/components/EventsView.jsx`, `EventDetail.jsx`, `FilterRow.jsx` | The Events screen. |
| `src/components/UsersView.jsx`, `BalanceChart.jsx` | The Users screen. |
| `src/components/dialogs/*` | Create / Open / Buy / Submit Order / Close / Deposit dialogs. |

## Working with AI tools

> Fill in / adjust the personal answers below before submitting.

**Which tool and why? Did you try others?**
Claude Code (Anthropic's coding agent), used from the Claude desktop app directly on the project
repository. It could read the existing server servlets and JavaFX screens, so it derived the API and
the screen layouts from the real code instead of from a description. _(Other tools tried: …)_

**Where did the AI not "deliver the goods" and needed my intervention?**
_(…)_

**Was there a bug or requirement the AI could not solve?**
_(…)_

**How much of the client was written by the AI alone vs. with my involvement?**
_(…% AI-only; describe where you stepped in.)_

**How long did it take? How long would it have taken without AI?**
_(…)_

**Prior frontend experience? Did it help?**
_(…)_

**Compared to the rest of the course exercises – was AI-driven development fun?**
_(…)_
