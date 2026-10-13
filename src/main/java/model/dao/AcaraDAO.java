package model.dao;

import config.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Acara;
import model.Seminar;
import model.Workshop;

public class AcaraDAO {

    private static final String SELECT_ACARA =
            "SELECT a.id, a.nama_acara, a.pemateri, a.tanggal, a.kuota, a.tipe_acara, "
            + "s.bidang, s.biaya, w.durasi_jam, w.alat "
            + "FROM acara a "
            + "LEFT JOIN seminar s ON s.acara_id = a.id "
            + "LEFT JOIN workshop w ON w.acara_id = a.id ";

    private final Connection conn;
    private final PendaftaranDAO pendaftaranDAO;

    public AcaraDAO() {
        this.conn = Koneksi.getConnection();
        this.pendaftaranDAO = new PendaftaranDAO();
    }

    public int tambahAcara(Acara acara) {
        String sqlAcara = "INSERT INTO acara (nama_acara, pemateri, tanggal, kuota, tipe_acara) VALUES (?, ?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false);
            int idBaru;

            try (PreparedStatement pstmt = conn.prepareStatement(sqlAcara, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, acara.getNama());
                pstmt.setString(2, acara.getPemateri());
                pstmt.setDate(3, java.sql.Date.valueOf(acara.getTanggal()));
                pstmt.setInt(4, acara.getKuota());
                pstmt.setString(5, acara.getJenis());
                pstmt.executeUpdate();

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("ID acara baru tidak didapatkan.");
                    }
                    idBaru = keys.getInt(1);
                }
            }

            simpanDetail(acara, idBaru);
            conn.commit();
            return idBaru;
        } catch (SQLException e) {
            batalkanTransaksi();
            System.out.println("Gagal menambah acara - " + e.getMessage());
            return -1;
        } finally {
            kembalikanAutoCommit();
        }
    }

    public List<Acara> ambilSemuaAcara() {
        return jalankanQuery(SELECT_ACARA + "ORDER BY a.id", null);
    }


    public Acara cariById(int id) {
        List<Acara> hasil = jalankanQuery(SELECT_ACARA + "WHERE a.id = ?", Integer.valueOf(id));
        return hasil.isEmpty() ? null : hasil.get(0);
    }

    public List<Acara> cariByKataKunci(String kataKunci) {
        String sql = SELECT_ACARA
                + "WHERE LOWER(a.nama_acara) LIKE ? OR LOWER(a.pemateri) LIKE ? ORDER BY a.id";
        List<Acara> hasil = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pola = "%" + kataKunci.toLowerCase() + "%";
            pstmt.setString(1, pola);
            pstmt.setString(2, pola);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    hasil.add(petakanKeAcara(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mencari acara - " + e.getMessage());
        }

        isiPeserta(hasil);
        return hasil;
    }

    public boolean updateAcara(Acara acara) {
        String sqlAcara = "UPDATE acara SET nama_acara=?, pemateri=?, tanggal=?, kuota=? WHERE id=?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlAcara)) {
                pstmt.setString(1, acara.getNama());
                pstmt.setString(2, acara.getPemateri());
                pstmt.setDate(3, java.sql.Date.valueOf(acara.getTanggal()));
                pstmt.setInt(4, acara.getKuota());
                pstmt.setInt(5, acara.getId());

                if (pstmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            updateDetail(acara);
            conn.commit();
            return true;
        } catch (SQLException e) {
            batalkanTransaksi();
            System.out.println("Gagal mengubah acara - " + e.getMessage());
            return false;
        } finally {
            kembalikanAutoCommit();
        }
    }

    public boolean hapusAcara(int id) {
        String sql = "DELETE FROM acara WHERE id=?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Gagal menghapus acara - " + e.getMessage());
            return false;
        }
    }


    private List<Acara> jalankanQuery(String sql, Integer parameterId) {
        List<Acara> hasil = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (parameterId != null) {
                pstmt.setInt(1, parameterId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    hasil.add(petakanKeAcara(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil data acara - " + e.getMessage());
        }

        isiPeserta(hasil);
        return hasil;
    }

    private Acara petakanKeAcara(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nama = rs.getString("nama_acara");
        String pemateri = rs.getString("pemateri");
        String tanggal = rs.getDate("tanggal").toString();
        int kuota = rs.getInt("kuota");

        if ("Workshop".equals(rs.getString("tipe_acara"))) {
            return new Workshop(id, nama, pemateri, tanggal, kuota,
                    rs.getInt("durasi_jam"), rs.getString("alat"));
        }
        return new Seminar(id, nama, pemateri, tanggal, kuota,
                rs.getString("bidang"), rs.getDouble("biaya"));
    }

    private void isiPeserta(List<Acara> daftarAcara) {
        for (Acara acara : daftarAcara) {
            acara.muatDaftarPeserta(pendaftaranDAO.ambilPesertaByAcara(acara.getId()));
        }
    }

    private void simpanDetail(Acara acara, int acaraId) throws SQLException {
        if (acara instanceof Seminar) {
            Seminar seminar = (Seminar) acara;
            String sql = "INSERT INTO seminar (acara_id, bidang, biaya) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, acaraId);
                pstmt.setString(2, seminar.getBidang());
                pstmt.setDouble(3, seminar.getBiaya());
                pstmt.executeUpdate();
            }
        } else if (acara instanceof Workshop) {
            Workshop workshop = (Workshop) acara;
            String sql = "INSERT INTO workshop (acara_id, durasi_jam, alat) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, acaraId);
                pstmt.setInt(2, workshop.getDurasiJam());
                pstmt.setString(3, workshop.getAlat());
                pstmt.executeUpdate();
            }
        }
    }

    private void updateDetail(Acara acara) throws SQLException {
        if (acara instanceof Seminar) {
            Seminar seminar = (Seminar) acara;
            String sql = "UPDATE seminar SET bidang=?, biaya=? WHERE acara_id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, seminar.getBidang());
                pstmt.setDouble(2, seminar.getBiaya());
                pstmt.setInt(3, acara.getId());
                pstmt.executeUpdate();
            }
        } else if (acara instanceof Workshop) {
            Workshop workshop = (Workshop) acara;
            String sql = "UPDATE workshop SET durasi_jam=?, alat=? WHERE acara_id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, workshop.getDurasiJam());
                pstmt.setString(2, workshop.getAlat());
                pstmt.setInt(3, acara.getId());
                pstmt.executeUpdate();
            }
        }
    }

    private void batalkanTransaksi() {
        try {
            conn.rollback();
        } catch (SQLException e) {
            System.out.println("Gagal rollback - " + e.getMessage());
        }
    }

    private void kembalikanAutoCommit() {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println("Gagal mengembalikan auto-commit - " + e.getMessage());
        }
    }
}
