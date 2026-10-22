@echo off
echo ==========================================
echo    Menjalankan SIPerpus Desktop (Java)
echo ==========================================

if not exist bin\com\perpustakaan\Main.class (
    echo Menjalankan kompilasi terlebih dahulu...
    call compile.bat
)

java -cp "bin;lib/*" com.perpustakaan.Main
