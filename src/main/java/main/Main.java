package main;

import config.Koneksi;
import controller.AcaraController;
import java.util.Scanner;
import view.InputHelper;
import view.MenuView;

public class Main {
    public static void main(String[] args) {
        InputHelper input = new InputHelper(new Scanner(System.in));
        MenuView view = new MenuView();

        view.tampilkanBanner();
        if (Koneksi.getConnection() == null) {
            view.pesanError("Tidak dapat terhubung ke database. Program dihentikan.");
            input.tutup();
            return;
        }
        AcaraController controller = new AcaraController(view, input);
        controller.jalankanMenu();
        input.tutup();
    }
}
