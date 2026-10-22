-- Migrasi V4: Hapus kolom isbn dari tabel buku secara permanen
-- Mengecek keberadaan kolom secara dinamis sebelum drop

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'buku' 
               AND COLUMN_NAME = 'isbn');

SET @query := IF(@exist > 0, 'ALTER TABLE buku DROP COLUMN isbn', 'SELECT 1');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
