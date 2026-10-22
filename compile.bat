@echo off
echo ==========================================
echo    Mengompilasi SIPerpus Desktop (Java)
echo ==========================================

if not exist bin (
    mkdir bin
)

dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% equ 0 (
    echo [SUKSES] Kompilasi berhasil! File .class tersimpan di folder bin/
) else (
    echo [GAGAL] Terjadi kesalahan saat kompilasi!
)
pause
