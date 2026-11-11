package vn.pottel.reporting.baocao.catalog;

import java.util.List;

public record ThamSoDef(
        String ten,
        Kieu kieu,
        String nhan,
        boolean batBuoc,
        List<String> giaTri
) {

    public enum Kieu {
        NGAY,
        CHUOI,
        SO,
        CHON
    }

    public ThamSoDef {
        if (ten == null || ten.isBlank()) {
            throw new IllegalArgumentException(
                    "Tên tham số không được để trống."
            );
        }

        if (kieu == null) {
            throw new IllegalArgumentException(
                    "Kiểu tham số không được để trống."
            );
        }

        if (nhan == null || nhan.isBlank()) {
            throw new IllegalArgumentException(
                    "Nhãn tham số không được để trống."
            );
        }

        ten = ten.trim();
        nhan = nhan.trim();

        giaTri = giaTri == null
                ? List.of()
                : List.copyOf(giaTri);

        if (kieu == Kieu.CHON && giaTri.isEmpty()) {
            throw new IllegalArgumentException(
                    "Tham số kiểu CHON phải có danh sách giá trị."
            );
        }

        if (kieu != Kieu.CHON && !giaTri.isEmpty()) {
            throw new IllegalArgumentException(
                    "Chỉ tham số kiểu CHON mới được có danh sách giá trị."
            );
        }
    }

    public static ThamSoDef ngay(String ten, String nhan) {
        return new ThamSoDef(
                ten, Kieu.NGAY, nhan, false, List.of()
        );
    }

    public static ThamSoDef chuoi(String ten, String nhan) {
        return new ThamSoDef(
                ten, Kieu.CHUOI, nhan, false, List.of()
        );
    }

    public static ThamSoDef so(String ten, String nhan) {
        return new ThamSoDef(
                ten, Kieu.SO, nhan, false, List.of()
        );
    }

    public static ThamSoDef chon(
            String ten,
            String nhan,
            List<String> giaTri
    ) {
        return new ThamSoDef(
                ten, Kieu.CHON, nhan, false, giaTri
        );
    }

    public static ThamSoDef batBuoc(
            String ten,
            Kieu kieu,
            String nhan
    ) {
        return new ThamSoDef(
                ten, kieu, nhan, true, List.of()
        );
    }
}