-- Migrasi V2: Tabel Rak Buku dan Fitur Soft Delete
CREATE TABLE IF NOT EXISTS rak (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kode_rak VARCHAR(20) NOT NULL UNIQUE,
    nama_rak VARCHAR(100) NOT NULL,
    lokasi VARCHAR(100) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO rak (id, kode_rak, nama_rak, lokasi, is_deleted) VALUES
(1, 'RAK-01', 'Rak Ilmu Komputer & Pemrograman', 'Lantai 1 - Sayap Kiri', 0),
(2, 'RAK-02', 'Rak Matematika & Sains', 'Lantai 1 - Sayap Kanan', 0),
(3, 'RAK-03', 'Rak Sastra & Novel', 'Lantai 2 - Zona Santai', 0),
(4, 'RAK-04', 'Rak Manajemen & Ekonomi', 'Lantai 2 - Sayap Tengah', 0)
ON DUPLICATE KEY UPDATE nama_rak=VALUES(nama_rak);
