@echo off
cd /d "%~dp0"
echo Installing dependencies...
call npm install
if errorlevel 1 (
  echo npm install failed.
  pause
  exit /b 1
)
echo Starting the Guess Market web client on http://localhost:5173 ...
rem Open the browser a few seconds from now, once the dev server is up.
start "" /b cmd /c "timeout /t 4 /nobreak >nul & start http://localhost:5173"
call npm run dev
pause
