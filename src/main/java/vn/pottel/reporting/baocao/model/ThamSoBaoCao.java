package vn.pottel.reporting.baocao.model;

import java.time.LocalDate;

public record ThamSoBaoCao(
        LocalDate tuNgay,
        LocalDate denNgay,
        String msisdn,
        String trangThaiKit,
        String trangThaiDoiSoat,
        String tinhTrangGoiMoi,
        int trang,
        int kichThuoc
) {
    public ThamSoBaoCao {
        if (trang < 1) {
            trang = 1;
        }

        if (kichThuoc != 10 && kichThuoc != 20
                && kichThuoc != 50 && kichThuoc != 100) {
            kichThuoc = 20;
        }

        if (msisdn != null && msisdn.isBlank()) {
            msisdn = null;
        }

        if (trangThaiKit != null && trangThaiKit.isBlank()) {
            trangThaiKit = null;
        }

        if (trangThaiDoiSoat != null && trangThaiDoiSoat.isBlank()) {
            trangThaiDoiSoat = null;
        }

        if (tinhTrangGoiMoi != null && tinhTrangGoiMoi.isBlank()) {
            tinhTrangGoiMoi = null;
        }
    }

    public int tinhOffset() {
        return (trang - 1) * kichThuoc;
    }
}