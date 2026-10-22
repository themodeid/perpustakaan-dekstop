# 📚 SIPerpus Desktop (Java Swing Modern & MySQL Docker)

Aplikasi Desktop Sistem Informasi Manajemen Perpustakaan berbasis **Java Swing (Pure Code)** dengan tema modern **FlatLaf** dan database **MySQL 8** yang dijalankan melalui **Docker Compose**.

Aplikasi ini dirancang bersih (tanpa *auto-generated drag-and-drop code* NetBeans yang rumit) sehingga mudah dibaca, dimodifikasi, dan dipelajari konsep Object-Oriented Programming (OOP) serta arsitektur berlapis (MVC/DAO).

---

## 📁 Struktur Direktori & Arsitektur

```text
perpustakaan-desktop/
├── bin/                          # File bytecode .class hasil kompilasi
├── database/
│   └── init.sql                  # Skema database (DDL) & seeder data awal
├── lib/                          # Library JAR (tanpa perlu install Maven)
│   ├── flatlaf-3.5.4.jar         # Modern Look and Feel UI
│   └── mysql-connector-j-8.4.0.jar # Driver JDBC MySQL
├── src/com/perpustakaan/
│   ├── config/
│   │   └── DatabaseConfig.java   # Konfigurasi koneksi JDBC MySQL
│   ├── dao/                      # Data Access Object (Query Database)
│   │   ├── AnggotaDAO.java
│   │   ├── BukuDAO.java
│   │   ├── KategoriDAO.java
│   │   ├── PeminjamanDAO.java
│   │   └── UserDAO.java
│   ├── model/                    # Entitas Data (POJO)
│   │   ├── Anggota.java
│   │   ├── Buku.java
│   │   ├── DetailPeminjaman.java
│   │   ├── Kategori.java
│   │   ├── Peminjaman.java
│   │   └── User.java
│   ├── service/                  # Business Logic Layer
│   │   ├── AuthService.java      # Manajemen login & session
│   │   ├── SirkulasiService.java # Aturan pinjam, kembali & denda
│   │   └── LaporanService.java   # Rekap statistik & ekspor CSV
│   ├── ui/                       # Antarmuka Java Swing (Pure Code)
│   │   ├── UIConstants.java      # Skema warna, font, styling helper
│   │   ├── LoginForm.java        # Form login modern
│   │   ├── MainFrame.java        # Jendela utama + sidebar navigation
│   │   ├── DashboardPanel.java   # Kartu statistik & tabel transaksi terkini
│   │   ├── BukuPanel.java        # CRUD Buku & filter pencarian
│   │   ├── KategoriPanel.java    # CRUD Kategori buku
│   │   ├── AnggotaPanel.java     # CRUD Data anggota
│   │   ├── KaryawanPanel.java    # CRUD Petugas & Admin (khusus Admin)
│   │   ├── PeminjamanPanel.java  # Transaksi peminjaman buku
│   │   ├── PengembalianPanel.java# Verifikasi kembali & kalkulasi denda
│   │   └── LaporanPanel.java     # Rekap riwayat transaksi & ekspor CSV
│   └── Main.java                 # Entry point aplikasi
├── compile.bat                   # Skrip kompilasi otomatis
├── docker-compose.yml            # Konfigurasi container MySQL 8
├── pom.xml                       # Konfigurasi standar Maven (opsional)
└── run.bat                       # Skrip untuk menjalankan aplikasi
```

---

## 🚀 Cara Menjalankan Aplikasi

### Langkah 1: Pastikan Docker Desktop Menyala & Jalankan MySQL
1. Buka aplikasi **Docker Desktop** di Windows Anda (dari Start Menu).
2. Buka terminal (PowerShell atau Command Prompt) di folder `perpustakaan-desktop`:
   ```powershell
   cd c:\A_main_code\all-my-projek\perpustakaan\perpustakaan-desktop
   docker compose up -d
   ```
   *Database `perpustakaan_db` beserta tabel dan data sample otomatis dibuat dari file `database/init.sql`.*

### Langkah 2: Menjalankan Aplikasi Java
Cukup jalankan file **`run.bat`**:
- Dobel klik file `run.bat` di File Explorer, **atau**
- Dari terminal PowerShell:
  ```powershell
  .\run.bat
  ```

---

## 🔑 Akun Login Bawaan (Default Credentials)

| Role | Username | Password | Hak Akses |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | Akses penuh (Termasuk menu kelola Petugas/Admin) |
| **Petugas Perpustakaan** | `petugas` | `petugas123` | Akses operasional sirkulasi, buku, anggota, dan laporan |

---

## ✨ Menu & Fitur Lengkap (Sesuai Silabus Pemrograman Visual)

### 📁 FORM MASTER
1. **Data Buku**: Input, edit, hapus, dan pencarian judul, ISBN, pengarang, kategori, serta pemilihan lokasi Rak Buku.
2. **Data Anggota**: Pendaftaran anggota, NIM, nama, jurusan, no. telepon, email, dan status aktif/nonaktif.
3. **Data Petugas**: Manajemen akun admin & petugas perpustakaan (khusus role Admin).
4. **Rak Buku**: Master penataan lokasi fisik buku (Kode Rak, Nama Rak, Lokasi / Lantai).

### 💼 FORM TRANSAKSI
1. **Peminjaman Buku**: Kasir peminjaman (pilih anggota aktif, pilih buku, durasi pinjam 7 hari, generate kode transaksi otomatis, pemotongan stok aman).
2. **Pengembalian Buku & Denda**: Pencarian transaksi aktif, kalkulasi otomatis hari keterlambatan, kalkulasi denda Rp 2.000/hari telat per buku, serta pemulihan stok buku ke rak.

### 📑 REPORT (LAPORAN)
1. **Laporan Buku**: Cetak / Ekspor data seluruh koleksi buku dan ketersediaan stok fisik ke format CSV/Excel.
2. **Laporan Anggota**: Cetak / Ekspor data seluruh anggota terdaftar dan status keaktifan ke format CSV/Excel.
3. **Laporan Peminjaman**: Rekap data transaksi peminjaman buku berdasarkan filter rentang tanggal.
4. **Laporan Pengembalian**: Rekap transaksi pengembalian buku beserta rincian histori denda keterlambatan yang terkumpul.

---

## 🔄 Sistem Database Migration Otomatis

Aplikasi ini dilengkapi fitur **Database Migration Otomatis** layaknya framework modern:
- Seluruh perubahan skema dan data awal disimpan dalam folder:
  `perpustakaan-desktop/database/migrations/`
  - `V1__init_schema.sql` (Struktur dasar tabel)
  - `V2__add_rak_table_and_soft_delete.sql` (Tabel rak & penambahan kolom soft delete)
  - `V3__seed_initial_data.sql` (Data awal buku, anggota, petugas, rak, kategori)
- Saat aplikasi dijalankan, sistem secara otomatis mengecek tabel `schema_migrations` di MySQL. File migrasi yang baru akan langsung diaplikasikan tanpa menghapus data yang sudah ada!
- Jika ingin menjalankan migrasi secara mandiri, cukup dobel-klik file **`migrate.bat`**.
