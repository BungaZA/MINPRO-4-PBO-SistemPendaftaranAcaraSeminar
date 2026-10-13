package controller;

import exception.KuotaPenuhException;
import java.util.List;
import model.Acara;
import model.Seminar;
import model.Workshop;
import model.dao.AcaraDAO;
import model.dao.PendaftaranDAO;
import util.InputValidator;
import view.InputHelper;
import view.MenuView;

public class AcaraController {
    private final AcaraDAO acaraDAO;
    private final PendaftaranDAO pendaftaranDAO;
    private final MenuView view;
    private final InputHelper input;

    public AcaraController(MenuView view, InputHelper input) {
        this.view = view;
        this.input = input;
        this.acaraDAO = new AcaraDAO();
        this.pendaftaranDAO = new PendaftaranDAO();
    }

    public void jalankanMenu() {
        int pilihan;
        do {
            view.tampilkanMenu();
            pilihan = input.bacaPilihan("Pilih menu (0-7): ", 0, 7);

            switch (pilihan) {
                case 1:
                    tambahSeminar();
                    break;
                case 2:
                    tambahWorkshop();
                    break;
                case 3:
                    view.tampilkanDaftarAcara(acaraDAO.ambilSemuaAcara());
                    break;
                case 4:
                    menuCariAcara();
                    break;
                case 5:
                    updateAcara();
                    break;
                case 6:
                    hapusAcara();
                    break;
                case 7:
                    daftarPeserta();
                    break;
                default:
                    view.tampilkanPenutup();
            }
        } while (pilihan != 0);
    }

    public void tambahSeminar() {
        view.tampilkanJudul("TAMBAH SEMINAR");
        String nama = input.bacaNamaAcara("Nama Seminar: ", false);
        String pemateri = input.bacaNamaOrang("Pemateri: ", false);
        String tanggal = input.bacaTanggal("Tanggal (YYYY-MM-DD): ", false);
        int kuota = input.bacaKuota("Kuota (" + InputValidator.KUOTA_MINIMAL + "-"
                + InputValidator.KUOTA_MAKSIMAL + "): ", false);
        String bidang = input.bacaTeksUmum("Bidang: ", false);
        double biaya = input.bacaBiaya("Biaya (0 = gratis): ", false);

        int idBaru = acaraDAO.tambahAcara(new Seminar(0, nama, pemateri, tanggal, kuota, bidang, biaya));
        if (idBaru > 0) {
            view.pesanSukses("Seminar berhasil ditambahkan dengan ID " + idBaru);
        } else {
            view.pesanError("Seminar gagal disimpan ke database.");
        }
    }

    public void tambahWorkshop() {
        view.tampilkanJudul("TAMBAH WORKSHOP");
        String nama = input.bacaNamaAcara("Nama Workshop: ", false);
        String pemateri = input.bacaNamaOrang("Pemateri: ", false);
        String tanggal = input.bacaTanggal("Tanggal (YYYY-MM-DD): ", false);
        int kuota = input.bacaKuota("Kuota (" + InputValidator.KUOTA_MINIMAL + "-"
                + InputValidator.KUOTA_MAKSIMAL + "): ", false);
        int durasi = input.bacaDurasi("Durasi (jam, " + InputValidator.DURASI_MINIMAL + "-"
                + InputValidator.DURASI_MAKSIMAL + "): ", false);
        String alat = input.bacaTeksUmum("Alat: ", false);

        int idBaru = acaraDAO.tambahAcara(new Workshop(0, nama, pemateri, tanggal, kuota, durasi, alat));
        if (idBaru > 0) {
            view.pesanSukses("Workshop berhasil ditambahkan dengan ID " + idBaru);
        } else {
            view.pesanError("Workshop gagal disimpan ke database.");
        }
    }


    public Acara cariAcara(int id) {
        return acaraDAO.cariById(id);
    }

    public List<Acara> cariAcara(String kataKunci) {
        return acaraDAO.cariByKataKunci(kataKunci);
    }

    public void menuCariAcara() {
        view.tampilkanJudul("CARI ACARA");
        view.tampilkanSubmenuCari();
        int pilihan = input.bacaPilihan("Pilih (1-2): ", 1, 2);

        if (pilihan == 1) {
            int id = input.bacaId("Masukkan ID Acara: ");
            Acara acara = cariAcara(id);
            if (acara == null) {
                view.pesanError("Acara dengan ID " + id + " tidak ditemukan.");
                return;
            }
            view.tampilkanDetailAcara(acara);
        } else {
            String kataKunci = input.bacaKataKunci("Masukkan kata kunci: ");
            List<Acara> hasil = cariAcara(kataKunci);
            if (hasil.isEmpty()) {
                view.pesanError("Tidak ada acara dengan kata kunci '" + kataKunci + "'.");
                return;
            }
            view.tampilkanDaftarAcara("HASIL PENCARIAN '" + kataKunci + "'", hasil);
        }
    }

    public void updateAcara() {
        view.tampilkanJudul("UPDATE ACARA");
        if (!adaData()) {
            return;
        }
        view.tampilkanDaftarAcara(acaraDAO.ambilSemuaAcara());
        int id = input.bacaId("\nMasukkan ID Acara yang akan diupdate: ");
        Acara acara = cariAcara(id);
        if (acara == null) {
            view.pesanError("Acara dengan ID " + id + " tidak ditemukan.");
            return;
        }

        view.pesanInfo("\nData saat ini: " + acara);
        view.pesanInfo("Kosongkan (tekan Enter) jika tidak ingin mengubah.\n");

        String nama = input.bacaNamaAcara("Nama [" + acara.getNama() + "]: ", true);
        if (nama != null) {
            acara.setNama(nama);
        }

        String pemateri = input.bacaNamaOrang("Pemateri [" + acara.getPemateri() + "]: ", true);
        if (pemateri != null) {
            acara.setPemateri(pemateri);
        }

        String tanggal = input.bacaTanggal("Tanggal [" + acara.getTanggal() + "]: ", true);
        if (tanggal != null) {
            acara.setTanggal(tanggal);
        }

        Integer kuota;
        while (true) {
            kuota = input.bacaKuota("Kuota [" + acara.getKuota() + "]: ", true);
            if (kuota == null || kuota >= acara.getJumlahPendaftar()) {
                break;
            }
            view.pesanError("Kuota tidak boleh kurang dari jumlah pendaftar ("
                    + acara.getJumlahPendaftar() + ").");
        }
        if (kuota != null) {
            acara.setKuota(kuota);
        }

        updateDataKhusus(acara);
        if (acaraDAO.updateAcara(acara)) {
            view.pesanSukses("Update selesai.");
        } else {
            view.pesanError("Update gagal disimpan ke database.");
        }
    }

    private void updateDataKhusus(Acara acara) {
        if (acara instanceof Seminar) {
            Seminar seminar = (Seminar) acara;
            String bidang = input.bacaTeksUmum("Bidang [" + seminar.getBidang() + "]: ", true);
            if (bidang != null) {
                seminar.setBidang(bidang);
            }
            Double biaya = input.bacaBiaya("Biaya [" + seminar.getBiaya() + "]: ", true);
            if (biaya != null) {
                seminar.setBiaya(biaya);
            }
        } else if (acara instanceof Workshop) {
            Workshop workshop = (Workshop) acara;
            Integer durasi = input.bacaDurasi("Durasi jam [" + workshop.getDurasiJam() + "]: ", true);
            if (durasi != null) {
                workshop.setDurasiJam(durasi);
            }
            String alat = input.bacaTeksUmum("Alat [" + workshop.getAlat() + "]: ", true);
            if (alat != null) {
                workshop.setAlat(alat);
            }
        }
    }

    public void hapusAcara() {
        view.tampilkanJudul("HAPUS ACARA");
        if (!adaData()) {
            return;
        }
        view.tampilkanDaftarAcara(acaraDAO.ambilSemuaAcara());
        int id = input.bacaId("\nMasukkan ID Acara yang akan dihapus: ");
        Acara acara = cariAcara(id);
        if (acara == null) {
            view.pesanError("Acara dengan ID " + id + " tidak ditemukan.");
            return;
        }

        if (acara.getJumlahPendaftar() > 0) {
            view.pesanInfo("Perhatian: acara ini sudah memiliki "
                    + acara.getJumlahPendaftar() + " peserta.");
        }
        if (input.bacaKonfirmasi("Yakin ingin menghapus '" + acara.getNama() + "'? (y/n): ")) {
            if (acaraDAO.hapusAcara(acara.getId())) {
                view.pesanSukses("Acara berhasil dihapus.");
            } else {
                view.pesanError("Acara gagal dihapus dari database.");
            }
        } else {
            view.pesanInfo("Penghapusan dibatalkan.");
        }
    }

    public void daftarPeserta() {
        view.tampilkanJudul("DAFTAR PESERTA");
        if (!adaData()) {
            return;
        }
        view.tampilkanDaftarAcara(acaraDAO.ambilSemuaAcara());
        int id = input.bacaId("\nMasukkan ID Acara yang ingin diikuti: ");
        Acara acara = cariAcara(id);
        if (acara == null) {
            view.pesanError("Acara dengan ID " + id + " tidak ditemukan.");
            return;
        }

        try {
            acara.pastikanKuotaTersedia();

            String nama;
            while (true) {
                nama = input.bacaNamaOrang("Masukkan Nama Peserta: ", false);
                if (!acara.sudahTerdaftar(nama)) {
                    break;
                }
                view.pesanError("Peserta '" + nama + "' sudah terdaftar di acara ini.");
            }
            String email = input.bacaEmail("Email (opsional, Enter untuk lewati): ", true);

            // tambahPeserta memeriksa kuota di model, lalu disimpan ke database lewat DAO
            if (email == null) {
                acara.tambahPeserta(nama);
            } else {
                acara.tambahPeserta(nama, email);
            }
            if (pendaftaranDAO.tambahPendaftaran(acara.getId(), nama, email)) {
                view.tampilkanStruk(acara, nama);
            } else {
                view.pesanError("Pendaftaran gagal disimpan ke database.");
            }
        } catch (KuotaPenuhException e) {
            view.pesanError(e.getMessage());
        }
    }

    private boolean adaData() {
        if (acaraDAO.ambilSemuaAcara().isEmpty()) {
            view.pesanError("Belum ada acara yang terdaftar.");
            return false;
        }
        return true;
    }
}
