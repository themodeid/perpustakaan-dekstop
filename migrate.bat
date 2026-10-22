@echo off
echo ==========================================
echo    Menjalankan Migrasi Database SIPerpus
echo ==========================================

if not exist bin\com\perpustakaan\config\DatabaseMigration.class (
    echo Mengompilasi project terlebih dahulu...
    call compile.bat
)

java -cp "bin;lib/*" -e "com.perpustakaan.config.DatabaseMigration.runMigrations()" 2>nul
if %ERRORLEVEL% neq 0 (
    java -cp "bin;lib/*" com.perpustakaan.config.DatabaseMigration
)

pause
