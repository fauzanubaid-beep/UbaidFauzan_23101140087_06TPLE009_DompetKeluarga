
==================================================
  DompetKeluarga - Android Java
  SPK Pengelolaan Keuangan Rumah Tangga
==================================================

CARA BUKA DI ANDROID STUDIO:
1. Buka Android Studio
2. File -> Open -> pilih folder dompet_keluarga_java ini
3. Tunggu Gradle sync selesai (perlu internet pertama kali)
4. Aktifkan USB Debugging di Redmi Note 13
5. Run -> Run 'app'

STRUKTUR PROJECT:
app/src/main/java/com/dompetkeluarga/app/
├── activity/
│   ├── SplashActivity.java       <- Router awal (login/onboarding)
│   ├── LoginActivity.java        <- PIN keypad 4 digit
│   ├── OnboardingActivity.java   <- Setup profil & gaji
│   ├── MainActivity.java         <- Bottom nav (Beranda/Pos/Laporan)
│   ├── TambahTransaksiActivity   <- Catat pengeluaran
│   ├── TambahPemasukanActivity   <- Pemasukan tak terduga
│   ├── KelolaPosActivity.java    <- Edit/tambah/hapus pos
│   └── PrioritasPosActivity.java <- Atur urutan drag & drop
├── model/
│   ├── UserProfile.java
│   ├── PosKeuangan.java
│   ├── Transaksi.java
│   └── PosSummary.java
├── database/
│   ├── DatabaseHelper.java       <- SQLite onCreate/upgrade
│   ├── UserDao.java              <- CRUD users
│   ├── PosDao.java               <- CRUD pos keuangan
│   └── TransaksiDao.java         <- CRUD transaksi
├── service/
│   ├── SpkService.java           <- 3 metode SPK
│   ├── AppSession.java           <- State management singleton
│   └── PdfService.java           <- Export PDF (Android built-in)
└── fragment/
    ├── BerandaFragment.java      <- Dashboard
    ├── PosFragment.java          <- List pos
    └── LaporanFragment.java      <- Laporan + grafik

DEPENDENCIES (build.gradle):
- MPAndroidChart v3.1.0 (grafik)
- Material Design 1.12.0 (UI)
- Navigation Component (fragment nav)
PDF menggunakan Android PdfDocument bawaan (tidak perlu library tambahan)

IZIN KHUSUS REDMI NOTE 13:
Settings -> Apps -> DompetKeluarga -> Other Permissions -> Autostart: ON
(untuk notifikasi reminder bisa jalan di background)
