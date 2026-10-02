import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The Guess Market server (Tomcat) runs on localhost:8080 under the /GuessMarket context path.
// Proxying it through the dev server keeps the browser on a single origin, so the session
// cookie (JSESSIONID) works and the server needs no CORS support.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      '/GuessMarket': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
