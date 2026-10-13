# Minpro-4-PBO-SistemPendaftaranAcaraSeminar

Sistem untuk mengelola acara **Seminar** dan **Workshop**. Proyek ini adalah pengembangan dari Mini Project 3, dengan perubahan utama: data tidak lagi disimpan di `ArrayList`, melainkan di **database MySQL** lewat **JDBC (PreparedStatement)** dan pola **DAO**.


---

## 1. Deskripsi Singkat Program

Program ini membantu pengelola mencatat dan mengatur acara. Fitur yang tersedia:

- menambah Seminar dan Workshop,
- melihat semua acara,
- mencari acara berdasarkan ID atau kata kunci,
- mengubah data acara,
- menghapus acara (dengan konfirmasi),
- mendaftarkan peserta, lengkap dengan struk biaya pendaftaran.

Semua data tersimpan permanen di database `db_manajemen_acara`. Data awal (3 acara dan 2 peserta).

Perubahan dengan Mini Project 3:

| Aspek | Mini Project 3 | Mini Project 4 |
|---|---|---|
| Penyimpanan data | `ArrayList<Acara>` di memori, hilang saat program ditutup | Database MySQL, data tetap ada |
| Data awal | Di-`add` di constructor `AcaraController` | Di file `.sql` (`INSERT INTO ...`) |
| ID acara | Dihitung manual dengan `nextId` | `AUTO_INCREMENT` di database |
| Akses data | Langsung ke `ArrayList` di controller | Lewat class DAO (`AcaraDAO`, `PendaftaranDAO`) |
| Package | `main`, `model`, `view`, `controller`, `exception`, `util` | Ditambah `config` dan `model.dao` |
| Query | Tidak ada | Semua memakai `PreparedStatement` |
| Penyimpanan peserta | `ArrayList<String>` di dalam objek | Tabel `pendaftaran` |

---

## 2. Struktur Package


<img width="288" height="484" alt="Screenshot 2026-10-13 222821" src="https://github.com/user-attachments/assets/bd0ebda4-0e59-4282-9045-b86a0f3577fb" />


| Package | Isi | Tanggung jawab |
|---|---|---|
| `config` | `Koneksi`, `TesKoneksi` | `Koneksi` membuat dan menyediakan koneksi ke database. `TesKoneksi` dipakai untuk mengecek koneksi berhasil atau tidak |
| `main` | `Main` | Titik masuk program. Mengecek koneksi database, lalu merakit View, input, dan Controller |
| `model` | `Acara`, `Seminar`, `Workshop`, `Pendaftaran` | Menyimpan data dan aturan objek (misalnya cara menghitung biaya dan pengecekan kuota). Tidak ada `Scanner`, `println`, ataupun kode SQL |
| `model.dao` | `AcaraDAO`, `PendaftaranDAO` | Satu-satunya tempat kode SQL. Mengubah objek model menjadi data tabel dan sebaliknya |
| `view` | `MenuView`, `InputHelper` | `MenuView` menampilkan semua teks ke layar. `InputHelper` membaca input dan memvalidasinya seketika |
| `controller` | `AcaraController` | Mengatur jalannya program: menerima pilihan menu, mengambil input, memanggil DAO dan model, lalu meminta View menampilkan hasil |
| `exception` | `KuotaPenuhException` | Exception khusus untuk pendaftaran ke acara yang kuotanya sudah penuh |
| `util` | `InputValidator`, `FormatUtil` | Aturan validasi dan format rupiah |

### 2.1 Database

Database `db_manajemen_acara` terdiri dari 4 tabel. Tabel `seminar` dan `workshop` adalah tabel detail yang terhubung ke `acara` lewat `acara_id` (mengikuti hubungan inheritance di kode: `Seminar` dan `Workshop` adalah turunan `Acara`).

---

## 3. Alur Program

### 3.1 Alur Umum

1. `Main.main()` membuat `InputHelper` (membungkus `Scanner`) dan `MenuView`, lalu menampilkan banner.
2. `Main` memanggil `Koneksi.getConnection()`. Jika hasilnya `null`, program menampilkan pesan error dan berhenti.
3. `AcaraController` dibuat. Constructor-nya membuat `AcaraDAO` dan `PendaftaranDAO`. **Tidak ada lagi data yang diisi lewat kode**.
4. `jalankanMenu()` masuk ke perulangan menu. Setiap putaran: tampilkan menu, baca pilihan 0 sampai 7 lewat `InputHelper`, lalu jalankan method yang sesuai lewat `switch`.
5. Perulangan berhenti saat pengguna memilih `0`. View menampilkan pesan penutup dan `Scanner` ditutup.

### 3.2 Alur Validasi Input (berlaku di semua menu)

Setiap field divalidasi saat itu juga di `InputHelper` dan `InputValidator`, sebelum data dikirim ke DAO. Input yang salah diminta ulang sampai benar.

| Field | Aturan |
|---|---|
| Pilihan menu | Angka 0 sampai 7 |
| ID acara | Bilangan bulat positif |
| Nama acara | 3 sampai 60 karakter; huruf, angka, spasi, dan `. , : & -`; wajib ada huruf |
| Pemateri dan nama peserta | 3 sampai 60 karakter; hanya huruf, spasi, dan `. , ' -` |
| Bidang dan alat | 3 sampai 60 karakter; wajib ada huruf |
| Tanggal | Format `YYYY-MM-DD`, tanggalnya harus benar-benar ada (misal 2026-02-30 ditolak), dan tidak boleh sebelum hari ini |
| Kuota | 1 sampai 500 |
| Durasi workshop | 1 sampai 12 jam |
| Biaya seminar | `0` (gratis) atau Rp10.000 sampai Rp5.000.000 |
| Email (opsional) | Format email yang valid; jika dikosongkan, disimpan sebagai `NULL` di database |
| Konfirmasi hapus | Hanya `y` atau `n` |
| Kuota saat update | Tidak boleh lebih kecil dari jumlah peserta yang sudah mendaftar |
| Nama peserta | Tidak boleh sama dengan peserta lain di acara yang sama |

### 3.3 Alur Tiap Menu

| No | Menu | Proses |
|---|---|---|
| 1 | Tambah Seminar | Input nama, pemateri, tanggal, kuota, bidang, biaya (masing-masing divalidasi langsung) lalu `Seminar` dibuat dan disimpan dengan `nextId`, kemudian `nextId` naik |
| 2 | Tambah Workshop | Sama seperti Seminar, dengan input durasi dan alat |
| 3 | Lihat Daftar Acara | Loop `ArrayList<Acara>`; tiap objek menampilkan `toString()` dan biaya pendaftaran sesuai jenis acaranya |
| 4 | Cari Acara | Pilih cara cari: ID (menampilkan detail dan daftar peserta) atau kata kunci (mencari pada nama dan pemateri, tidak peduli huruf besar atau kecil) |
| 5 | Update Acara | Pilih ID, lalu isi field baru. Tekan Enter untuk melewati. Field yang diisi salah langsung ditolak dan diminta ulang. Data khusus Seminar (bidang, biaya) dan Workshop (durasi, alat) ikut bisa diubah |
| 6 | Hapus Acara | Pilih ID, tampilkan peringatan jika sudah ada peserta, lalu minta konfirmasi `y/n` |
| 7 | Daftar Peserta | Pilih ID, cek kuota (jika penuh langsung ditolak), input nama (cek duplikat), email opsional, lalu tampilkan struk berisi nama, acara, biaya, dan sisa kuota |

Jika tidak ada data acara di database, menu Update, Hapus, dan Daftar Peserta langsung memberi tahu dan kembali ke menu.

### 3.4 Tampilan Output

**Menu utama**

<img width="318" height="226" alt="Screenshot 2026-10-08 201704" src="https://github.com/user-attachments/assets/d276289e-77bf-444a-8ccd-a509e2bef8c7" />

**Lihat daftar acara**

<img width="884" height="255" alt="Screenshot 2026-10-08 201754" src="https://github.com/user-attachments/assets/3cf806f9-296a-4d25-bb50-c5641f00c1c4" />

**Tambah seminar dengan validasi langsung** 

<img width="611" height="260" alt="Screenshot 2026-10-08 202056" src="https://github.com/user-attachments/assets/a1aec6ad-505b-4729-81bf-ff3c7d58c443" />

**Tambah workshop**

<img width="624" height="237" alt="Screenshot 2026-10-08 202210" src="https://github.com/user-attachments/assets/40277671-f473-49fe-beda-bc192dd48276" />

**Cari acara berdasarkan ID**

<img width="881" height="196" alt="Screenshot 2026-10-08 202250" src="https://github.com/user-attachments/assets/52328df2-3d13-44d7-8b85-402a04d79251" />

**Cari acara berdasarkan kata kunci**

<img width="730" height="190" alt="Screenshot 2026-10-08 202401" src="https://github.com/user-attachments/assets/02f2091f-cd95-41f7-9500-dc96f5af47b9" />

**Update acara**

<img width="740" height="214" alt="Screenshot 2026-10-08 202620" src="https://github.com/user-attachments/assets/8b77d936-f684-4a70-b9f7-3bce5a424c0f" />

**Hapus acara dengan konfirmasi**

<img width="648" height="142" alt="Screenshot 2026-10-08 202918" src="https://github.com/user-attachments/assets/11602fe7-6c18-43d8-84b4-f65107a28aa2" />

**Daftar peserta (berhasil dan struk biaya)**

<img width="393" height="141" alt="Screenshot 2026-10-08 202957" src="https://github.com/user-attachments/assets/a4a786ed-e700-4de3-b1e6-7987ec23904e" />

**Daftar peserta ditolak (nama duplikatm)**

<img width="399" height="181" alt="Screenshot 2026-10-08 203053" src="https://github.com/user-attachments/assets/a7bb640f-f835-4acf-bd50-6eacb2dc7e74" />

**Data tersimpan di database setelah operasi CRUD**

<img width="620" height="239" alt="Screenshot 2026-10-13 230802" src="https://github.com/user-attachments/assets/de347faf-1484-45ea-8faf-89d5e813eece" />

<img width="764" height="199" alt="Screenshot 2026-10-13 230812" src="https://github.com/user-attachments/assets/133a85ef-1f7c-4f1e-86ab-2dd6a4f636d6" />

---

## 4. Penerapan Encapsulation dan Inheritance

### 4.1 Encapsulation

Semua atribut pada `Acara`, `Seminar`, dan `Workshop` bersifat `private` dan hanya bisa diakses lewat getter dan setter. Pada versi ini ada tambahan pengamanan:

- **Atribut `final`** untuk nilai yang tidak boleh berubah setelah objek dibuat:

  ```java
  // Acara.java
  private final int id;
  private final ArrayList<String> daftarPeserta;
  ```

- **Getter yang aman.** `getDaftarPeserta()` mengembalikan list read-only (`Collections.unmodifiableList`), sehingga kode di luar tidak bisa menambah atau menghapus peserta tanpa melewati method `tambahPeserta()`.
- **Setter yang menjaga aturan.** `setKuota()` melempar `IllegalArgumentException` jika kuota baru lebih kecil dari jumlah peserta.
- **Method khusus untuk DAO.** Peserta yang sudah tersimpan di database dimasukkan ke objek lewat `muatDaftarPeserta(List<String>)`, bukan dengan membuka akses langsung ke list.
- **Konstanta `static final`** untuk batas validasi di `InputValidator`, serta `BIAYA_MATERI` dan `TARIF_PER_JAM` di `Workshop`.
- **Pembungkusan akses database.** Di `Koneksi`, `URL`, `USER`, `PASS`, dan `conn` bersifat `private static final` / `private static`; kode lain hanya bisa memakai `getConnection()`. Di DAO, `conn` dan `pendaftaranDAO` bersifat `private final`, dan method bantu seperti `petakanKeAcara()` dan `simpanDetail()` bersifat `private`.
- **Referensi `final`** pada `acaraDAO`, `pendaftaranDAO`, `view`, dan `input` di `AcaraController`. `InputValidator` dan `FormatUtil` dibuat `final class` dengan constructor `private` karena hanya berisi method static.

Atribut seperti `nama`, `pemateri`, `tanggal`, dan `kuota` sengaja tidak dibuat `final` karena memang bisa diubah lewat menu Update.

### 4.2 Inheritance

`Seminar` dan `Workshop` memakai `extends Acara` dan memanggil `super(...)` untuk mengisi atribut umum (id, nama, pemateri, tanggal, kuota). Masing-masing hanya menambah atribut miliknya sendiri:

- `Seminar`: `bidang`, `biaya`
- `Workshop`: `durasiJam`, `alat`

Hubungan ini juga tercermin di database: kolom umum ada di tabel `acara`, sedangkan kolom khusus ada di tabel `seminar` dan `workshop`. Karena itu `AcaraDAO` bisa mengembalikan `List<Acara>` yang berisi kedua jenis objek sekaligus.

---

## 5. Penerapan Polymorphism dan Abstraction

### 5.1 Abstraction

`Acara` adalah **abstract class** sehingga tidak bisa dibuat langsung dengan `new Acara(...)`. Acara hanya berfungsi sebagai kerangka umum. Class ini memiliki tiga **abstract method** yang wajib diisi oleh setiap subclass:

```java
public abstract String getJenis();
public abstract String getInfoTambahan();
public abstract double hitungBiayaPendaftaran();
```

Setiap jenis acara dipaksa menjelaskan sendiri jenisnya, informasi khususnya, dan cara menghitung biayanya.

### 5.2 Polymorphism: Overriding

| Method | `Seminar` | `Workshop` |
|---|---|---|
| `getJenis()` | `"Seminar"` | `"Workshop"` |
| `getInfoTambahan()` | `Bidang: ...` | `Durasi: ... jam \| Alat: ...` |
| `hitungBiayaPendaftaran()` | `biaya` (dari database) | `BIAYA_MATERI + (TARIF_PER_JAM × durasiJam)` |
| `toString()` | data umum + info bidang | data umum + info durasi dan alat |

Method di atas dipanggil lewat referensi bertipe `Acara`, yaitu di `MenuView.tampilkanDaftarAcara()`, `tampilkanDetailAcara()`, dan `tampilkanStruk()`.

Polymorphism juga muncul saat membaca database. `AcaraDAO.petakanKeAcara()` membaca satu baris hasil query lalu membuat objek `Seminar` atau `Workshop` sesuai kolom `tipe_acara`, tetapi dikembalikan sebagai tipe `Acara`:

```java
private Acara petakanKeAcara(ResultSet rs) throws SQLException {
    // ...
    if ("Workshop".equals(rs.getString("tipe_acara"))) {
        return new Workshop(id, nama, pemateri, tanggal, kuota,
                rs.getInt("durasi_jam"), rs.getString("alat"));
    }
    return new Seminar(id, nama, pemateri, tanggal, kuota,
            rs.getString("bidang"), rs.getDouble("biaya"));
}
```

Dengan begitu, controller dan view cukup bekerja dengan `List<Acara>` tanpa perlu tahu jenis aslinya. Saat menyimpan, `tipe_acara` diisi dari `acara.getJenis()`.

### 5.3 Polymorphism: Overloading

| Class | Method | Perbedaan |
|---|---|---|
| `AcaraController` | `cariAcara(int id)` | Mengembalikan satu `Acara` berdasarkan ID (memanggil `AcaraDAO.cariById`) |
| `AcaraController` | `cariAcara(String kataKunci)` | Mengembalikan daftar `Acara` yang nama atau pematerinya mengandung kata kunci (memanggil `AcaraDAO.cariByKataKunci`) |
| `Acara` | `tambahPeserta(String nama)` | Mendaftarkan peserta tanpa email |
| `Acara` | `tambahPeserta(String nama, String email)` | Mendaftarkan peserta beserta email |
| `MenuView` | `tampilkanDaftarAcara(List)` | Menampilkan daftar dengan judul bawaan |
| `MenuView` | `tampilkanDaftarAcara(String judul, List)` | Menampilkan daftar dengan judul sendiri (dipakai untuk hasil pencarian) |

---

## 6. Penerapan JDBC

### 6.1 Koneksi (`config/Koneksi.java`)

`Koneksi` memakai `DriverManager.getConnection()` dengan URL `jdbc:mysql://localhost:3306/db_manajemen_acara`. Koneksi disimpan di satu variabel `static`, sehingga hanya dibuat sekali dan dipakai ulang oleh semua DAO. Jika koneksi belum ada atau sudah tertutup, koneksi baru dibuat. Jika gagal, pesan error ditampilkan dan `null` dikembalikan, lalu `Main` menghentikan program dengan pesan yang jelas.

Driver MySQL ditambahkan di `pom.xml`:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>
```

### 6.2 Prepared Statement

Semua query memakai `PreparedStatement` dengan tanda `?` dan `setXxx()`, tidak ada penggabungan string untuk nilai input. Ini mencegah SQL injection dan menangani tipe data dengan benar. Setiap `PreparedStatement` dan `ResultSet` dibuka dengan **try-with-resources** sehingga otomatis ditutup.

```java
String sql = "UPDATE acara SET nama_acara=?, pemateri=?, tanggal=?, kuota=? WHERE id=?";
try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setString(1, acara.getNama());
    pstmt.setString(2, acara.getPemateri());
    pstmt.setDate(3, java.sql.Date.valueOf(acara.getTanggal()));
    pstmt.setInt(4, acara.getKuota());
    pstmt.setInt(5, acara.getId());
    pstmt.executeUpdate();
}
```

### 6.3 DAO (Data Access Object)

| DAO | Method | Operasi | Tabel |
|---|---|---|---|
| `AcaraDAO` | `tambahAcara(Acara)` | CREATE (mengembalikan ID baru, atau -1 jika gagal) | `acara` + `seminar`/`workshop` |
| `AcaraDAO` | `ambilSemuaAcara()` | READ semua | `acara` LEFT JOIN `seminar`, `workshop` |
| `AcaraDAO` | `cariById(int)` | READ satu | sama seperti di atas, `WHERE a.id = ?` |
| `AcaraDAO` | `cariByKataKunci(String)` | READ pencarian | `WHERE LOWER(nama_acara) LIKE ? OR LOWER(pemateri) LIKE ?` |
| `AcaraDAO` | `updateAcara(Acara)` | UPDATE | `acara` + `seminar`/`workshop` |
| `AcaraDAO` | `hapusAcara(int)` | DELETE | `acara` (tabel lain ikut terhapus lewat cascade) |
| `PendaftaranDAO` | `tambahPendaftaran(int, String, String)` | CREATE | `pendaftaran` |
| `PendaftaranDAO` | `ambilPesertaByAcara(int)` | READ | `pendaftaran` |
| `PendaftaranDAO` | `hapusPendaftaran(int, String)` | DELETE | `pendaftaran` |

Hal penting dalam implementasinya:

- **Satu `SELECT` dengan `LEFT JOIN`** mengambil data umum dan data khusus sekaligus. `AcaraDAO` lalu memetakan tiap baris menjadi `Seminar` atau `Workshop` dan memuat pesertanya lewat `PendaftaranDAO`.
- **Konversi tanggal.** Di model tanggal berupa `String` (`yyyy-MM-dd`), sedangkan di database bertipe `DATE`. Saat menyimpan dipakai `java.sql.Date.valueOf(...)`, saat membaca dipakai `rs.getDate(...).toString()`.
- **Email opsional.** `pstmt.setString(3, email)` dengan `email = null` disimpan sebagai `NULL`. Saat dibaca, peserta tanpa email ditampilkan hanya dengan namanya.
- **Penanganan error.** `SQLException` ditangkap di DAO. Method tulis mengembalikan `boolean` (atau ID) sehingga controller bisa menampilkan pesan sukses atau gagal lewat View.

