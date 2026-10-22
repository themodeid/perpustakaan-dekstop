@echo off
echo ==========================================
echo    Mengompilasi SIPerpus Desktop (Java)
echo ==========================================

if not exist bin (
    mkdir bin
)

set "JAVAC_BIN=javac"
where javac >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "C:\Program Files\Java\jdk-22\bin\javac.exe" (
        set "JAVAC_BIN=C:\Program Files\Java\jdk-22\bin\javac.exe"
    )
)

dir /s /b src\*.java > sources.txt
"%JAVAC_BIN%" -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% equ 0 (
    echo [SUKSES] Kompilasi berhasil! File .class tersimpan di folder bin/
) else (
    echo [GAGAL] Terjadi kesalahan saat kompilasi!
)
pause
