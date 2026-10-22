-- Inisialisasi Database Sistem Perpustakaan
CREATE DATABASE IF NOT EXISTS perpustakaan_db;
USE perpustakaan_db;

-- 1. Tabel Users (Petugas & Admin) dengan Soft Delete
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nama_lengkap VARCHAR(100) NOT NULL,
    role ENUM('admin', 'petugas') NOT NULL DEFAULT 'petugas',
    no_telp VARCHAR(20),
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Tabel Rak Buku dengan Soft Delete
CREATE TABLE IF NOT EXISTS rak (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kode_rak VARCHAR(20) NOT NULL UNIQUE,
    nama_rak VARCHAR(100) NOT NULL,
    lokasi VARCHAR(100) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Tabel Kategori Buku
CREATE TABLE IF NOT EXISTS kategori (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nama_kategori VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Tabel Buku dengan Soft Delete
CREATE TABLE IF NOT EXISTS buku (
    id INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    judul VARCHAR(200) NOT NULL,
    pengarang VARCHAR(100) NOT NULL,
    penerbit VARCHAR(100) NOT NULL,
    tahun_terbit INT NOT NULL,
    kategori_id INT NOT NULL,
    rak_id INT NOT NULL DEFAULT 1,
    stok_total INT NOT NULL DEFAULT 1,
    stok_tersedia INT NOT NULL DEFAULT 1,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (kategori_id) REFERENCES kategori(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (rak_id) REFERENCES rak(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Tabel Anggota dengan Soft Delete
CREATE TABLE IF NOT EXISTS anggota (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nim VARCHAR(30) NOT NULL UNIQUE,
    nama VARCHAR(100) NOT NULL,
    jurusan VARCHAR(100) NOT NULL,
    no_telp VARCHAR(20),
    email VARCHAR(100),
    status ENUM('aktif', 'nonaktif') NOT NULL DEFAULT 'aktif',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Tabel Transaksi Peminjaman
CREATE TABLE IF NOT EXISTS peminjaman (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kode_transaksi VARCHAR(50) NOT NULL UNIQUE,
    anggota_id INT NOT NULL,
    user_id INT NOT NULL,
    tanggal_pinjam DATE NOT NULL,
    tanggal_jatuh_tempo DATE NOT NULL,
    tanggal_kembali DATE NULL,
    status ENUM('dipinjam', 'kembali', 'terlambat') NOT NULL DEFAULT 'dipinjam',
    total_denda DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    catatan TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (anggota_id) REFERENCES anggota(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Tabel Detail Peminjaman
CREATE TABLE IF NOT EXISTS detail_peminjaman (
    id INT AUTO_INCREMENT PRIMARY KEY,
    peminjaman_id INT NOT NULL,
    buku_id INT NOT NULL,
    denda_per_buku DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (peminjaman_id) REFERENCES peminjaman(id) ON DELETE CASCADE,
    FOREIGN KEY (buku_id) REFERENCES buku(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- SEED DATA AWAL (Sample Data)
-- ============================================================

INSERT INTO users (username, password, nama_lengkap, role, no_telp, is_deleted) VALUES
('admin', 'admin123', 'Administrator Utama', 'admin', '081234567890', 0),
('petugas', 'petugas123', 'Budi Santoso (Petugas)', 'petugas', '089876543210', 0)
ON DUPLICATE KEY UPDATE nama_lengkap=VALUES(nama_lengkap);

INSERT INTO rak (id, kode_rak, nama_rak, lokasi, is_deleted) VALUES
(1, 'RAK-01', 'Rak Ilmu Komputer & Pemrograman', 'Lantai 1 - Sayap Kiri', 0),
(2, 'RAK-02', 'Rak Matematika & Sains', 'Lantai 1 - Sayap Kanan', 0),
(3, 'RAK-03', 'Rak Sastra & Novel', 'Lantai 2 - Zona Santai', 0),
(4, 'RAK-04', 'Rak Manajemen & Ekonomi', 'Lantai 2 - Sayap Tengah', 0)
ON DUPLICATE KEY UPDATE nama_rak=VALUES(nama_rak);

INSERT INTO kategori (id, nama_kategori) VALUES
(1, 'Teknologi Informasi & Komputer'),
(2, 'Sains & Matematika'),
(3, 'Sastra & Bahasa'),
(4, 'Ekonomi & Bisnis'),
(5, 'Sejarah & Filsafat')
ON DUPLICATE KEY UPDATE nama_kategori=VALUES(nama_kategori);

INSERT INTO buku (isbn, judul, pengarang, penerbit, tahun_terbit, kategori_id, rak_id, stok_total, stok_tersedia, is_deleted) VALUES
('978-602-02-1234-1', 'Pemrograman Java Berorientasi Objek', 'Erico Darmawan', 'Informatika', 2023, 1, 1, 5, 5, 0),
('978-602-02-5678-2', 'Algoritma & Struktur Data dengan Java', 'Rinaldi Munir', 'Informatika', 2022, 1, 1, 4, 4, 0),
('978-602-02-9988-3', 'Basis Data Relasional & MySQL', 'Abdul Kadir', 'Andi Offset', 2021, 1, 1, 6, 6, 0),
('978-602-04-1122-4', 'Matematika Diskrit Edisi Revisi', 'Rinaldi Munir', 'Informatika', 2020, 2, 2, 3, 3, 0),
('978-979-30-4455-6', 'Laskar Pelangi', 'Andrea Hirata', 'Bentang Pustaka', 2018, 3, 3, 8, 8, 0),
('978-979-22-3344-7', 'Bumi Manusia', 'Pramoedya Ananta Toer', 'Hasta Mitra', 2019, 3, 3, 5, 5, 0),
('978-602-03-7788-9', 'Manajemen Keuangan Modern', 'Bambang Riyanto', 'BPFE', 2022, 4, 4, 4, 4, 0)
ON DUPLICATE KEY UPDATE judul=VALUES(judul);

INSERT INTO anggota (nim, nama, jurusan, no_telp, email, status, is_deleted) VALUES
('2024001', 'Ahmad Fadillah', 'Teknik Informatika', '081111222333', 'ahmad@campus.ac.id', 'aktif', 0),
('2024002', 'Siti Rahmawati', 'Sistem Informasi', '082222333444', 'siti@campus.ac.id', 'aktif', 0),
('2024003', 'Dimas Pratama', 'Manajemen', '083333444555', 'dimas@campus.ac.id', 'aktif', 0),
('2024004', 'Putri Ayu', 'Akuntansi', '084444555666', 'putri@campus.ac.id', 'aktif', 0)
ON DUPLICATE KEY UPDATE nama=VALUES(nama);
