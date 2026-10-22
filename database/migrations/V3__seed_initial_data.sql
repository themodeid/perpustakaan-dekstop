-- Migrasi V3: Seed Data Awal (Users, Rak, Kategori, Buku, Anggota)
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

INSERT INTO buku (judul, pengarang, penerbit, tahun_terbit, kategori_id, rak_id, stok_total, stok_tersedia, is_deleted) VALUES
('Pemrograman Java Berorientasi Objek', 'Erico Darmawan', 'Informatika', 2023, 1, 1, 5, 5, 0),
('Algoritma & Struktur Data dengan Java', 'Rinaldi Munir', 'Informatika', 2022, 1, 1, 4, 4, 0),
('Basis Data Relasional & MySQL', 'Abdul Kadir', 'Andi Offset', 2021, 1, 1, 6, 6, 0),
('Matematika Diskrit Edisi Revisi', 'Rinaldi Munir', 'Informatika', 2020, 2, 2, 3, 3, 0),
('Laskar Pelangi', 'Andrea Hirata', 'Bentang Pustaka', 2018, 3, 3, 8, 8, 0),
('Bumi Manusia', 'Pramoedya Ananta Toer', 'Hasta Mitra', 2019, 3, 3, 5, 5, 0),
('Manajemen Keuangan Modern', 'Bambang Riyanto', 'BPFE', 2022, 4, 4, 4, 4, 0)
ON DUPLICATE KEY UPDATE judul=VALUES(judul);

INSERT INTO anggota (nim, nama, jurusan, no_telp, email, status, is_deleted) VALUES
('2024001', 'Ahmad Fadillah', 'Teknik Informatika', '081111222333', 'ahmad@campus.ac.id', 'aktif', 0),
('2024002', 'Siti Rahmawati', 'Sistem Informasi', '082222333444', 'siti@campus.ac.id', 'aktif', 0),
('2024003', 'Dimas Pratama', 'Manajemen', '083333444555', 'dimas@campus.ac.id', 'aktif', 0),
('2024004', 'Putri Ayu', 'Akuntansi', '084444555666', 'putri@campus.ac.id', 'aktif', 0)
ON DUPLICATE KEY UPDATE nama=VALUES(nama);
