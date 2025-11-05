@echo off
SETLOCAL
echo ============================================
echo  Avvio Progetto MAP - QTMINER
echo ============================================
echo.

REM --- Richiesta Password Nascosta ---
echo Richiesta password di root MySQL...
FOR /F "usebackq delims=" %%i IN (`powershell -Command "Write-Host -NoNewLine 'Inserisci la password di root di MySQL: '; $pass = Read-Host -AsSecureString; $BSTR=[System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($pass); [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)"`) DO SET "MYSQL_PASS=%%i"
echo.
echo.
echo Password acquisita.
echo.

REM --- Esecuzione Script SQL ---
echo Esecuzione script SQL (server\MAPDB.sql)...

REM =========== MODIFICA CHIAVE QUI SOTTO ===========
REM Salva il percorso di questo script in una variabile
SET "SCRIPT_PATH=%~dp0"

REM Spostati temporaneamente nella cartella del server
pushd "%SCRIPT_PATH%server"

REM Esegui il comando. Ora "MAPDB.sql" e' un file locale, non serve il percorso
mysql -u root -p%MYSQL_PASS% -e "source MAPDB.sql" 2>nul || GOTO :ERRORE_SCRIPT

REM Torna alla cartella originale
popd
GOTO :SUCCESS_SCRIPT

:ERRORE_SCRIPT
REM Torna alla cartella originale ANCHE SE FALLISCE
popd
GOTO :ERRORE
REM =========== FINE MODIFICA ===========

:SUCCESS_SCRIPT
echo.
echo Script SQL eseguito. Database 'MapDb' creato/verificato.
echo.
GOTO :AVVIO

REM --- Sezione di Errore Unificata ---
:ERRORE
echo.
echo ================== ERRORE ==================
echo ERRORE: Impossibile eseguire lo script SQL.
echo.
echo Cause possibili:
echo 1. La password di root di MySQL e' ERRATA.
echo 2. Il servizio MySQL non e' in esecuzione.
echo 3. Il file '%SCRIPT_PATH%server\MAPDB.sql' non e' stato trovato.
echo 4. Il comando 'mysql' o 'java' non e' nel PATH di sistema.
echo ============================================
echo.
pause
ENDLOCAL
exit /b

REM --- Sezione di Avvio ---
:AVVIO
echo Avvio del Server...
start "QTMINER Server" cmd /k "java -jar ""%SCRIPT_PATH%server\server.jar"""

REM Pausa per dare tempo al server di avviarsi
timeout /t 3 /nobreak > nul

echo Avvio del Client...
start "QTMINER Client" cmd /k "java -jar ""%SCRIPT_PATH%client\client.jar"""

echo.
echo Avvio completato.
ENDLOCAL