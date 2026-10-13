package model;

import exception.KuotaPenuhException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Acara implements Pendaftaran {
    private final int id;
    private String nama;
    private String pemateri;
    private String tanggal;
    private int kuota;
    private final ArrayList<String> daftarPeserta;

    public Acara(int id, String nama, String pemateri, String tanggal, int kuota) {
        this.id = id;
        this.nama = nama;
        this.pemateri = pemateri;
        this.tanggal = tanggal;
        this.kuota = kuota;
        this.daftarPeserta = new ArrayList<String>();
    }

    public int getId() { return id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getPemateri() { return pemateri; }
    public void setPemateri(String pemateri) { this.pemateri = pemateri; }

    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }

    public int getKuota() { return kuota; }

    public void setKuota(int kuota) {
        if (kuota < daftarPeserta.size()) {
            throw new IllegalArgumentException("Kuota tidak boleh kurang dari jumlah pendaftar.");
        }
        this.kuota = kuota;
    }

    public List<String> getDaftarPeserta() {
        return Collections.unmodifiableList(daftarPeserta);
    }

    // Dipakai DAO untuk mengisi peserta yang sudah tersimpan di database
    public void muatDaftarPeserta(List<String> peserta) {
        daftarPeserta.clear();
        daftarPeserta.addAll(peserta);
    }

    public int getJumlahPendaftar() {
        return daftarPeserta.size();
    }

    @Override
    public int getSisaKuota() {
        return kuota - daftarPeserta.size();
    }

    @Override
    public boolean isKuotaPenuh() {
        return daftarPeserta.size() >= kuota;
    }

    public void pastikanKuotaTersedia() throws KuotaPenuhException {
        if (isKuotaPenuh()) {
            throw new KuotaPenuhException(nama, daftarPeserta.size(), kuota);
        }
    }

    @Override
    public void tambahPeserta(String namaPeserta) throws KuotaPenuhException {
        pastikanKuotaTersedia();
        daftarPeserta.add(namaPeserta);
    }

    public void tambahPeserta(String namaPeserta, String email) throws KuotaPenuhException {
        tambahPeserta(namaPeserta + " (" + email + ")");
    }

    public boolean sudahTerdaftar(String namaPeserta) {
        for (String peserta : daftarPeserta) {
            int batas = peserta.indexOf(" (");
            String namaTersimpan = batas >= 0 ? peserta.substring(0, batas) : peserta;
            if (namaTersimpan.equalsIgnoreCase(namaPeserta)) {
                return true;
            }
        }
        return false;
    }

    public abstract String getJenis();

    public abstract String getInfoTambahan();

    public abstract double hitungBiayaPendaftaran();

    @Override
    public String toString() {
        return "ID: " + id
                + " | Jenis: " + getJenis()
                + " | Nama: " + nama
                + " | Pemateri: " + pemateri
                + " | Tanggal: " + tanggal
                + " | Kuota: " + getJumlahPendaftar() + "/" + kuota;
    }
}
