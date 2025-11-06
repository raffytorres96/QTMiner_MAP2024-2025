@echo off
setlocal EnableExtensions EnableDelayedExpansion
title QT Miner - Avvio completo
cls

:: === Modalità secondaria: solo wizard client in nuova finestra =================
if /i "%~1"=="_clientwizard" goto :client_wizard
:: =============================================================================

:: === [CONFIGURAZIONE] ========================================================
set "MYSQL_BIN=mysql"
set "MYSQL_HOST=localhost"
set "MYSQL_PORT=3306"
set "MYSQL_USER=root"
set "MYSQL_PWD="

set "SERVER_JAR=%~dp0server\QTServer.jar"
set "CLIENT_JAR=%~dp0client\QTClient.jar"
set "SQL_FILE=%~dp0server\MAPDB.sql"

set "DEFAULT_HOST=127.0.0.1"
set "DEFAULT_PORT=8080"
:: ============================================================================

echo.
echo ==========================================================
echo    QT Miner - Setup DB + Avvio Server e Client
echo ==========================================================
echo.

:: --- Controlli base ---
if not exist "%SQL_FILE%"  (echo ERRORE: "%SQL_FILE%" mancante.& pause & exit /b 1)
if not exist "%SERVER_JAR%" (echo ERRORE: "%SERVER_JAR%" mancante.& pause & exit /b 1)
if not exist "%CLIENT_JAR%" (echo ERRORE: "%CLIENT_JAR%" mancante.& pause & exit /b 1)
"%MYSQL_BIN%" --version >nul 2>&1 || (echo ERRORE: mysql.exe non trovato. Controlla il PATH di sistema.& pause & exit /b 1)
java -version >nul 2>&1 || (echo ERRORE: Java non trovato nel PATH di sistema.& pause & exit /b 1)

:: --- Esegui SQL ---
echo [1/3] Preparo il database (MAPDB)...
set "MYSQL_AUTH=--protocol=tcp -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER%"
if defined MYSQL_PWD (set "MYSQL_AUTH=!MYSQL_AUTH! -p%MYSQL_PWD%") else (set "MYSQL_AUTH=!MYSQL_AUTH! -p")
"%MYSQL_BIN%" !MYSQL_AUTH! < "%SQL_FILE%" >nul 2>&1
if errorlevel 1 (
    echo      ERRORE: Esecuzione MAPDB.sql fallita.
    echo      Controlla che il server MySQL sia in esecuzione e le credenziali siano corrette.
    pause
    exit /b 1
)
echo Database MapDb creato in Mysql.

:: --- Avvio server in una nuova finestra ---
echo [2/3] Avvio QT Server...
start "QT Server" /D "%~dp0server" cmd /k java -jar "QTServer.jar"

:: --- Avvio client in una nuova finestra ---
echo [3/3] Avvio QT Client...
start "QT Client" /D "%~dp0client" cmd /k "%~f0" _clientwizard

echo.
echo ==========================================================
echo  Fatto. Le finestre del server e del client sono aperte.
echo ==========================================================
echo.
echo Il setup e' completato.
echo Premi un tasto per chiudere questa finestra di riepilogo.
pause

endlocal
exit /b 0

:: ======================== WIZARD CLIENT ======================
:client_wizard
setlocal EnableExtensions EnableDelayedExpansion
title QT Client - Configurazione e Avvio
cls

set "DEFAULT_HOST=127.0.0.1"
set "DEFAULT_PORT=8080"

echo.
echo ===== QT Client - Configura destinazione =====
echo   1) Localhost    (%DEFAULT_HOST%)
echo   2) Emulatore AndroidStudio     (10.0.2.2)
echo   3) Personalizzato
choice /c 123 /n /m "Seleziona [1-3]: "
set "_c=%errorlevel%"

if "%_c%"=="1" set "HOST=%DEFAULT_HOST%"
if "%_c%"=="2" set "HOST=10.0.2.2"
if "%_c%"=="3" (
  set "HOST="
  set /p HOST=Inserisci indirizzo IP/host [%DEFAULT_HOST%]: 
  if "%HOST%"=="" set "HOST=%DEFAULT_HOST%"
)

:askport
set "PORT="
set /p PORT=Inserisci porta [%DEFAULT_PORT%]: 
if "%PORT%"=="" set "PORT=%DEFAULT_PORT%"

set "_bad="
for /f "delims=0123456789" %%A in ("%PORT%") do set "_bad=1"
if defined _bad (echo Porta non valida. Riprova.& goto askport)
if %PORT% LSS 1  (echo Porta non valida. Riprova.& goto askport)
if %PORT% GTR 65535 (echo Porta non valida. Riprova.& goto askport)

echo.
echo Attendo che il server sia in ascolto su %HOST%:%PORT% (max ~30s)...
set /a _tries=0
:wait_listen
set /a _tries+=1
>nul 2>&1 (netstat -ano | find ":%PORT% " | find "LISTENING")
if errorlevel 1 (
  if %_tries% GEQ 30 (
    echo AVVISO: Non rilevo la porta in ASCOLTO. Provo a connettermi comunque...
    goto :run_client
  )
  timeout /t 1 >nul
  goto :wait_listen
)
echo OK: Server in ascolto.

:run_client
echo.
echo Avvio client in corso...
if exist "QTClient.jar" (
  java -jar "QTClient.jar" "%HOST%" "%PORT%"
) else (
  echo ERRORE: QTClient.jar non trovato nella cartella corrente.
  pause
)
endlocal
exit /b 0