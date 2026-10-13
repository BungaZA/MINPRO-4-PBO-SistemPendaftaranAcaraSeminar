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

Perubahan utama dibanding Mini Project 2:

| Aspek | Mini Project 2 | Mini Project 3 |
|---|---|---|
| Struktur | Satu package `seminar` | MVC: `model`, `view`, `controller`, ditambah `exception` dan `util` |
| `Acara` | Class biasa | `abstract class` dengan 3 abstract method |
| Overriding | `getJenis()` dan `getInfoTambahan()` di-override tetapi tidak berdampak apa-apa | Semua method override dipakai nyata (tampilan daftar, detail, dan struk biaya) |
| Overloading | Tidak ada | `cariAcara`, `tambahPeserta`, `tampilkanDaftarAcara` |
| Atribut tetap | Semua bisa diubah | Atribut yang tidak berubah dibuat `final` |
| Validasi | Dicek setelah semua input terisi | Dicek langsung per field, diulang sampai benar |
| Aturan biaya | Biaya Rp1 lolos | Gratis (0) atau Rp10.000 sampai Rp5.000.000 |
| Nilai tambah | Polymorphism | Interface dan custom exception |

---

## 2. Struktur Package



<img width="340" height="412" alt="Screenshot 2026-10-08 200017" src="https://github.com/user-attachments/assets/65c48346-b470-4193-8b24-4901aa94bdb3" />


| Package | Isi | Tanggung jawab |
|---|---|---|
| `main` | `Main` | Titik masuk program. Merakit View, input, dan Controller, lalu menjalankan menu |
| `model` | `Acara`, `Seminar`, `Workshop`, `Pendaftaran` | Menyimpan data dan aturan objek (misalnya cara menghitung biaya). Tidak ada `Scanner` ataupun `println` |
| `view` | `MenuView`, `InputHelper` | `MenuView` menampilkan semua teks ke layar. `InputHelper` membaca input dan memvalidasinya seketika |
| `controller` | `AcaraController` | Mengatur jalannya program: menerima pilihan menu, mengambil input, mengolah data lewat model, lalu meminta View menampilkan hasil |
| `exception` | `KuotaPenuhException` | Exception khusus untuk pendaftaran ke acara yang kuotanya sudah penuh |
| `util` | `InputValidator`, `FormatUtil` | Aturan validasi dan format rupiah |


Catatan: `InputHelper` berada di package `view` karena tugasnya berurusan langsung dengan pengguna. Controller memanggilnya untuk mengambil data dan tidak mencetak apa pun sendiri.

---

## 3. Alur Program

### 3.1 Alur Umum

1. `Main.main()` membuat `InputHelper` (membungkus `Scanner`), `MenuView`, lalu `AcaraController`.
2. `MenuView` menampilkan banner program.
3. Constructor `AcaraController` menyiapkan `ArrayList<Acara>` dan mengisi 3 data awal.
4. `jalankanMenu()` masuk ke perulangan menu. Setiap putaran: tampilkan menu, baca pilihan 0 sampai 7 lewat `InputHelper`, lalu jalankan method yang sesuai lewat `switch`.
5. Perulangan berhenti saat pengguna memilih `0`. View menampilkan pesan penutup dan `Scanner` ditutup.

### 3.2 Alur Validasi Input (berlaku di semua menu)

Setiap field divalidasi saat itu juga, tidak menunggu semua field terisi:

Aturan validasi:

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
| Biaya seminar | `0` (gratis) atau Rp10.000 sampai Rp5.000.000. Nilai seperti Rp1 ditolak |
| Email (opsional) | Format email yang valid |
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

Jika data acara kosong, menu Update, Hapus, dan Daftar Peserta langsung memberi tahu dan kembali ke menu.

### 3.4 Tampilan Output

**Menu utama**

<img width="318" height="226" alt="Screenshot 2026-10-08 201704" src="https://github.com/user-attachments/assets/d276289e-77bf-444a-8ccd-a509e2bef8c7" />

**Lihat daftar acara** (biaya tiap jenis acara dihitung dengan cara berbeda)

<img width="884" height="255" alt="Screenshot 2026-10-08 201754" src="https://github.com/user-attachments/assets/3cf806f9-296a-4d25-bb50-c5641f00c1c4" />

**Tambah seminar dengan validasi langsung** (tanggal lampau, kuota di luar rentang, dan biaya Rp1 ditolak seketika)

<img width="611" height="260" alt="Screenshot 2026-10-08 202056" src="https://github.com/user-attachments/assets/a1aec6ad-505b-4729-81bf-ff3c7d58c443" />

**Tambah workshop**

<img width="624" height="237" alt="Screenshot 2026-10-08 202210" src="https://github.com/user-attachments/assets/40277671-f473-49fe-beda-bc192dd48276" />

**Cari acara berdasarkan ID**

<img width="881" height="196" alt="Screenshot 2026-10-08 202250" src="https://github.com/user-attachments/assets/52328df2-3d13-44d7-8b85-402a04d79251" />

**Cari acara berdasarkan kata kunci**

<img width="730" height="190" alt="Screenshot 2026-10-08 202401" src="https://github.com/user-attachments/assets/02f2091f-cd95-41f7-9500-dc96f5af47b9" />

**Update acara dengan input salah lalu diperbaiki**

<img width="740" height="214" alt="Screenshot 2026-10-08 202620" src="https://github.com/user-attachments/assets/8b77d936-f684-4a70-b9f7-3bce5a424c0f" />

**Hapus acara dengan konfirmasi**

<img width="648" height="142" alt="Screenshot 2026-10-08 202918" src="https://github.com/user-attachments/assets/11602fe7-6c18-43d8-84b4-f65107a28aa2" />

**Daftar peserta (berhasil dan struk biaya)**

<img width="393" height="141" alt="Screenshot 2026-10-08 202957" src="https://github.com/user-attachments/assets/a4a786ed-e700-4de3-b1e6-7987ec23904e" />

**Daftar peserta ditolak (nama duplikatm)**

<img width="399" height="181" alt="Screenshot 2026-10-08 203053" src="https://github.com/user-attachments/assets/a7bb640f-f835-4acf-bd50-6eacb2dc7e74" />

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

  `setId()` dan `setDaftarPeserta()` dihapus karena tidak lagi dibutuhkan.

- **Getter yang aman.** `getDaftarPeserta()` mengembalikan list read-only (`Collections.unmodifiableList`), sehingga kode di luar tidak bisa menambah atau menghapus peserta tanpa melewati method `tambahPeserta()`.
- **Setter yang menjaga aturan.** `setKuota()` melempar `IllegalArgumentException` jika kuota baru lebih kecil dari jumlah peserta.
- **Konstanta `static final`** untuk batas validasi (`KUOTA_MAKSIMAL`, `BIAYA_MINIMAL`, dll.) di `InputValidator`, serta `BIAYA_MATERI` dan `TARIF_PER_JAM` di `Workshop`.
- **Referensi `final`** pada `daftarAcara`, `view`, dan `input` di `AcaraController`, serta `scanner` di `InputHelper`. `InputValidator` dan `FormatUtil` juga dibuat `final class` dengan constructor `private` karena hanya berisi method static.

Atribut seperti `nama`, `pemateri`, `tanggal`, dan `kuota` sengaja tidak dibuat `final` karena memang bisa diubah lewat menu Update. Begitu juga `nextId` yang terus bertambah.

### 4.2 Inheritance

```
          Acara (abstract)
         /               \
    Seminar             Workshop
```

`Seminar` dan `Workshop` memakai `extends Acara` dan memanggil `super(...)` untuk mengisi atribut umum (id, nama, pemateri, tanggal, kuota). Masing-masing hanya menambah atribut miliknya sendiri:

- `Seminar`: `bidang`, `biaya`
- `Workshop`: `durasiJam`, `alat`

Karena itu `ArrayList<Acara>` bisa menampung kedua jenis objek sekaligus.

---

## 5. Penerapan Polymorphism dan Abstraction

### 5.1 Abstraction

`Acara` adalah **abstract class** sehingga tidak bisa dibuat langsung dengan `new Acara(...)`. Acara hanya berfungsi sebagai kerangka umum. Class ini memiliki tiga **abstract method** yang wajib diisi oleh setiap subclass:

```java
public abstract String getJenis();
public abstract String getInfoTambahan();
public abstract double hitungBiayaPendaftaran();
```

Dengan cara ini, setiap jenis acara dipaksa menjelaskan sendiri jenisnya, informasi khususnya, dan cara menghitung biayanya.

### 5.2 Polymorphism: Overriding

| Method | `Seminar` | `Workshop` |
|---|---|---|
| `getJenis()` | `"Seminar"` | `"Workshop"` |
| `getInfoTambahan()` | `Bidang: ...` | `Durasi: ... jam \| Alat: ...` |
| `hitungBiayaPendaftaran()` | `biaya` (sesuai input) | `BIAYA_MATERI + (TARIF_PER_JAM × durasiJam)` |
| `toString()` | data umum + info bidang | data umum + info durasi dan alat |

Seluruh method di atas benar-benar dipanggil lewat referensi bertipe `Acara`, yaitu di:

- `MenuView.tampilkanDaftarAcara()`: loop `ArrayList<Acara>` yang memanggil `toString()` dan `hitungBiayaPendaftaran()`.
- `MenuView.tampilkanDetailAcara()`: menampilkan detail acara hasil pencarian.
- `MenuView.tampilkanStruk()`: memanggil `getJenis()` dan `hitungBiayaPendaftaran()` pada struk pendaftaran.

```java
for (Acara a : daftar) {
    System.out.println(a);  // toString() milik Seminar atau Workshop
    System.out.println("   Biaya Pendaftaran: "
            + FormatUtil.rupiah(a.hitungBiayaPendaftaran()));  // versi sesuai objek asli
}
```

Contoh hasilnya: Seminar "Cyber Security 2026" menampilkan Rp50.000 (dari input), sedangkan Workshop 6 jam menampilkan Rp145.000 (25.000 + 6 × 20.000).

### 5.3 Polymorphism: Overloading

| Class | Method | Perbedaan |
|---|---|---|
| `AcaraController` | `cariAcara(int id)` | Mengembalikan satu `Acara` berdasarkan ID |
| `AcaraController` | `cariAcara(String kataKunci)` | Mengembalikan daftar `Acara` yang nama atau pematerinya mengandung kata kunci |
| `Acara` | `tambahPeserta(String nama)` | Mendaftarkan peserta tanpa email |
| `Acara` | `tambahPeserta(String nama, String email)` | Mendaftarkan peserta beserta email |
| `MenuView` | `tampilkanDaftarAcara(List)` | Menampilkan daftar dengan judul bawaan |
| `MenuView` | `tampilkanDaftarAcara(String judul, List)` | Menampilkan daftar dengan judul sendiri (dipakai untuk hasil pencarian) |

Kedua versi `cariAcara` dipakai di menu Cari Acara. Kedua versi `tambahPeserta` dipakai di menu Daftar Peserta, tergantung apakah pengguna mengisi email atau tidak.

---

## 6. Penerapan Nilai Tambah

### 6.1 Interface

Interface `Pendaftaran` berada di `model/Pendaftaran.java` dan berisi kontrak pendaftaran peserta:

```java
public interface Pendaftaran {
    void tambahPeserta(String namaPeserta) throws KuotaPenuhException;
    boolean isKuotaPenuh();
    int getSisaKuota();
}
```

`Acara` mengimplementasikannya (`abstract class Acara implements Pendaftaran`) sehingga aturan pendaftaran terpisah dari data acara dan diwariskan ke `Seminar` dan `Workshop`.

### 6.2 Custom Exception

`KuotaPenuhException` (package `exception`) dilempar oleh `Acara.pastikanKuotaTersedia()` ketika kuota sudah penuh. Exception ditangkap di `AcaraController.daftarPeserta()` lalu pesannya ditampilkan lewat View.

### 6.3 Validasi dan Error Handling yang Diperketat

Penjelasannya ada di bagian 3.2. Semua aturan ada di `InputValidator`, dan pembacaan input berulang di `InputHelper`.
