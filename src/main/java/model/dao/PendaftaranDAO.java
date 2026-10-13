package model.dao;

import config.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PendaftaranDAO {

    private final Connection conn;

    public PendaftaranDAO() {
        this.conn = Koneksi.getConnection();
    }

    public boolean tambahPendaftaran(int acaraId, String namaPeserta, String email) {
        String sql = "INSERT INTO pendaftaran (acara_id, nama_peserta, email_peserta) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, acaraId);
            pstmt.setString(2, namaPeserta);
            pstmt.setString(3, email); // null jika email tidak diisi

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Gagal menambah pendaftaran - " + e.getMessage());
            return false;
        }
    }

    public List<String> ambilPesertaByAcara(int acaraId) {
        List<String> daftarPeserta = new ArrayList<>();
        String sql = "SELECT nama_peserta, email_peserta FROM pendaftaran WHERE acara_id=? ORDER BY id";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, acaraId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String nama = rs.getString("nama_peserta");
                    String email = rs.getString("email_peserta");
                    daftarPeserta.add(email == null || email.isEmpty() ? nama : nama + " (" + email + ")");
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengambil data peserta - " + e.getMessage());
        }
        return daftarPeserta;
    }

    public boolean hapusPendaftaran(int acaraId, String namaPeserta) {
        String sql = "DELETE FROM pendaftaran WHERE acara_id=? AND nama_peserta=?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, acaraId);
            pstmt.setString(2, namaPeserta);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Gagal menghapus pendaftaran - " + e.getMessage());
            return false;
        }
    }
}
