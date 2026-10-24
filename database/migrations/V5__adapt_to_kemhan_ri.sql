-- Migrasi V5: Penyesuaian Identitas Resmi & Data Master Perpustakaan Kementerian Pertahanan RI (Kemhan RI)

-- 1. Pembaruan Kategori Buku Kedinasan & Pertahanan
INSERT INTO kategori (id, nama_kategori) VALUES
(1, 'Strategi & Doktrin Pertahanan Semesta'),
(2, 'Keamanan Siber & Kriptografi Intelijen'),
(3, 'Hukum Militer & Hukum Humaniter Internasional'),
(4, 'Manajemen Logistik & Alutsista Pertahanan'),
(5, 'Geopolitik & Sejarah Kepemimpinan Militer')
ON DUPLICATE KEY UPDATE nama_kategori=VALUES(nama_kategori);

-- 2. Pembaruan Rak Penyimpanan Buku Kedinasan
INSERT INTO rak (id, kode_rak, nama_rak, lokasi, is_deleted) VALUES
(1, 'RAK-DEF-01', 'Rak Doktrin & Strategi Pertahanan', 'Lantai 1 - Sayap Kanan (Pustaka Utama)', 0),
(2, 'RAK-CYBER-02', 'Rak Siber, Sandi & Komlek Pertahanan', 'Lantai 1 - Sayap Kiri (Pustaka TIK)', 0),
(3, 'RAK-HUKUM-03', 'Rak Hukum & Kebijakan Internasional', 'Lantai 2 - Sayap Tengah', 0),
(4, 'RAK-LOG-04', 'Rak Alutsista & Rantai Pasok Pertahanan', 'Lantai 2 - Ruang Referensi', 0)
ON DUPLICATE KEY UPDATE nama_rak=VALUES(nama_rak), lokasi=VALUES(lokasi);

-- 3. Pembaruan Koleksi Pustaka Resmi Kementerian Pertahanan RI
INSERT INTO buku (id, judul, pengarang, penerbit, tahun_terbit, kategori_id, rak_id, stok_total, stok_tersedia, is_deleted) VALUES
(1, 'Doktrin Pertahanan Semesta (Bela Negara)', 'Tim Pokja Renhan Kemhan', 'Kementerian Pertahanan RI', 2023, 1, 1, 6, 6, 0),
(2, 'Keamanan Siber Nasional & Perang Informasi Asimetris', 'Kolonel Inf. Dr. Suryanto', 'Pusdatin Kemhan', 2022, 2, 2, 5, 5, 0),
(3, 'Geopolitik & Ketahanan Nasional Kawasan Maritim Pasifik', 'Lemhannas RI', 'Lemhannas Press', 2021, 5, 1, 4, 4, 0),
(4, 'Hukum Humaniter Internasional dalam Operasi Militer Modern', 'Ditjen Strahan Kemhan', 'Balitbang Kemhan', 2020, 3, 3, 5, 5, 0),
(5, 'Manajemen Logistik & Pemeliharaan Alutsista Pertahanan', 'Marsma TNI Dr. Hendra', 'Pustaka Pertahanan', 2022, 4, 4, 4, 4, 0),
(6, 'Kepemimpinan Strategis Perwira di Era Disrupsi Teknologi', 'Letjen TNI (Purn) Bambang S.', 'Pustaka Militer', 2023, 5, 1, 5, 5, 0),
(7, 'Komunikasi Taktis C4ISR & Integrasi Sistem Pertahanan', 'Tim Ahli Pusdatin Kemhan', 'Kemhan RI Press', 2024, 2, 2, 4, 4, 0)
ON DUPLICATE KEY UPDATE judul=VALUES(judul), pengarang=VALUES(pengarang), penerbit=VALUES(penerbit), kategori_id=VALUES(kategori_id), rak_id=VALUES(rak_id);

-- 4. Pembaruan Data Personel / Anggota Perpustakaan Kemhan RI (NIP/NRP & Satker Resmi)
INSERT INTO anggota (id, nim, nama, jurusan, no_telp, email, status, is_deleted) VALUES
(1, '198205122008011001', 'Kolonel Inf. Ahmad Fadillah, M.Han.', 'Pusdatin Kemhan', '081111222333', 'ahmad.fadillah@kemhan.go.id', 'aktif', 0),
(2, '198811202014022002', 'Pembina IV/a Siti Rahmawati, S.Kom., M.T.I.', 'Biro Humas Setjen Kemhan', '082222333444', 'siti.rahmawati@kemhan.go.id', 'aktif', 0),
(3, '532109', 'Mayor Chb Dimas Pratama, S.T.', 'Ditjen Pothan Kemhan', '083333444555', 'dimas.pratama@kemhan.go.id', 'aktif', 0),
(4, '199403152019012003', 'Penata Muda III/a Putri Ayu, S.E.', 'Ditjen Renhan Kemhan', '084444555666', 'putri.ayu@kemhan.go.id', 'aktif', 0),
(5, '197904252006041005', 'Letkol Czi Budi Hartono, S.Sos.', 'Balitbang Kemhan', '085555666777', 'budi.hartono@kemhan.go.id', 'aktif', 0)
ON DUPLICATE KEY UPDATE nim=VALUES(nim), nama=VALUES(nama), jurusan=VALUES(jurusan), no_telp=VALUES(no_telp), email=VALUES(email);
