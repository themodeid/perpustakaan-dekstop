@echo off
echo ==========================================
echo    Menjalankan SIPerpus Desktop (Java)
echo ==========================================

set "JAVA_BIN=java"
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "C:\Program Files\Java\jdk-22\bin\java.exe" (
        set "JAVA_BIN=C:\Program Files\Java\jdk-22\bin\java.exe"
    )
)

if not exist bin\com\perpustakaan\Main.class (
    echo Menjalankan kompilasi terlebih dahulu...
    call compile.bat
)

"%JAVA_BIN%" -cp "bin;lib/*" com.perpustakaan.Main
