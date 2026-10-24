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
(1, 'RAK-DEF-01', 'Rak Doktrin & Strategi Pertahanan', 'Lantai 1 - Sayap Kanan (Pustaka Utama)', 0),
(2, 'RAK-CYBER-02', 'Rak Siber, Sandi & Komlek Pertahanan', 'Lantai 1 - Sayap Kiri (Pustaka TIK)', 0),
(3, 'RAK-HUKUM-03', 'Rak Hukum & Kebijakan Internasional', 'Lantai 2 - Sayap Tengah', 0),
(4, 'RAK-LOG-04', 'Rak Alutsista & Rantai Pasok Pertahanan', 'Lantai 2 - Ruang Referensi', 0)
ON DUPLICATE KEY UPDATE nama_rak=VALUES(nama_rak), lokasi=VALUES(lokasi);

INSERT INTO kategori (id, nama_kategori) VALUES
(1, 'Strategi & Doktrin Pertahanan Semesta'),
(2, 'Keamanan Siber & Kriptografi Intelijen'),
(3, 'Hukum Militer & Hukum Humaniter Internasional'),
(4, 'Manajemen Logistik & Alutsista Pertahanan'),
(5, 'Geopolitik & Sejarah Kepemimpinan Militer')
ON DUPLICATE KEY UPDATE nama_kategori=VALUES(nama_kategori);

INSERT INTO buku (id, judul, pengarang, penerbit, tahun_terbit, kategori_id, rak_id, stok_total, stok_tersedia, is_deleted) VALUES
(1, 'Doktrin Pertahanan Semesta (Bela Negara)', 'Tim Pokja Renhan Kemhan', 'Kementerian Pertahanan RI', 2023, 1, 1, 6, 6, 0),
(2, 'Keamanan Siber Nasional & Perang Informasi Asimetris', 'Kolonel Inf. Dr. Suryanto', 'Pusdatin Kemhan', 2022, 2, 2, 5, 5, 0),
(3, 'Geopolitik & Ketahanan Nasional Kawasan Maritim Pasifik', 'Lemhannas RI', 'Lemhannas Press', 2021, 5, 1, 4, 4, 0),
(4, 'Hukum Humaniter Internasional dalam Operasi Militer Modern', 'Ditjen Strahan Kemhan', 'Balitbang Kemhan', 2020, 3, 3, 5, 5, 0),
(5, 'Manajemen Logistik & Pemeliharaan Alutsista Pertahanan', 'Marsma TNI Dr. Hendra', 'Pustaka Pertahanan', 2022, 4, 4, 4, 4, 0),
(6, 'Kepemimpinan Strategis Perwira di Era Disrupsi Teknologi', 'Letjen TNI (Purn) Bambang S.', 'Pustaka Militer', 2023, 5, 1, 5, 5, 0),
(7, 'Komunikasi Taktis C4ISR & Integrasi Sistem Pertahanan', 'Tim Ahli Pusdatin Kemhan', 'Kemhan RI Press', 2024, 2, 2, 4, 4, 0)
ON DUPLICATE KEY UPDATE judul=VALUES(judul);

INSERT INTO anggota (id, nim, nama, jurusan, no_telp, email, status, is_deleted) VALUES
(1, '198205122008011001', 'Kolonel Inf. Ahmad Fadillah, M.Han.', 'Pusdatin Kemhan', '081111222333', 'ahmad.fadillah@kemhan.go.id', 'aktif', 0),
(2, '198811202014022002', 'Pembina IV/a Siti Rahmawati, S.Kom., M.T.I.', 'Biro Humas Setjen Kemhan', '082222333444', 'siti.rahmawati@kemhan.go.id', 'aktif', 0),
(3, '532109', 'Mayor Chb Dimas Pratama, S.T.', 'Ditjen Pothan Kemhan', '083333444555', 'dimas.pratama@kemhan.go.id', 'aktif', 0),
(4, '199403152019012003', 'Penata Muda III/a Putri Ayu, S.E.', 'Ditjen Renhan Kemhan', '084444555666', 'putri.ayu@kemhan.go.id', 'aktif', 0),
(5, '197904252006041005', 'Letkol Czi Budi Hartono, S.Sos.', 'Balitbang Kemhan', '085555666777', 'budi.hartono@kemhan.go.id', 'aktif', 0)
ON DUPLICATE KEY UPDATE nama=VALUES(nama);
