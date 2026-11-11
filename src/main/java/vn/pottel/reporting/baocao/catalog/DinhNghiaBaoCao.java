
package vn.pottel.reporting.baocao.catalog;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record DinhNghiaBaoCao(
        String maBaoCao,
        String nhom,
        String tenBaoCao,
        String moTa,
        String tepSql,
        boolean kichHoat,
        String vaiTro,
        List<ThamSoDef> thamSo,
        boolean phanTrang,

        // Metadata mô tả các bảng kết quả
        Map<String, BangDef> cacBang,

        // Metadata mô tả các thẻ tổng quan
        List<TheDef> cacThe,

        // Metadata mô tả cấu hình phân trang
        PhanTrangDef cauHinhPhanTrang
) {

    public DinhNghiaBaoCao {
        if (maBaoCao == null || maBaoCao.isBlank()) {
            throw new IllegalArgumentException(
                    "Mã báo cáo không được để trống."
            );
        }

        if (nhom == null || nhom.isBlank()) {
            throw new IllegalArgumentException(
                    "Nhóm báo cáo không được để trống."
            );
        }

        if (tenBaoCao == null || tenBaoCao.isBlank()) {
            throw new IllegalArgumentException(
                    "Tên báo cáo không được để trống."
            );
        }

        if (tepSql == null || tepSql.isBlank()) {
            throw new IllegalArgumentException(
                    "Đường dẫn SQL không được để trống."
            );
        }

        maBaoCao = maBaoCao.trim();
        nhom = nhom.trim();
        tenBaoCao = tenBaoCao.trim();
        moTa = moTa == null ? "" : moTa.trim();
        tepSql = tepSql.trim();

        vaiTro = vaiTro == null || vaiTro.isBlank()
                ? null
                : vaiTro.trim();

        thamSo = thamSo == null
                ? List.of()
                : List.copyOf(thamSo);

        cacBang = cacBang == null
                ? Map.of()
                : Collections.unmodifiableMap(
                        new LinkedHashMap<>(cacBang)
                );

        cacThe = cacThe == null
                ? List.of()
                : List.copyOf(cacThe);

        if (phanTrang && cauHinhPhanTrang == null) {
            // Giá trị mặc định để giữ tương thích với cấu hình cũ.
            cauHinhPhanTrang = new PhanTrangDef(
                    "1B",
                    "1D",
                    "total_rows",
                    20
            );
        }

        if (!phanTrang) {
            cauHinhPhanTrang = null;
        }
    }

    // Constructor cũ 9 tham số.
    // Giữ lại để không làm hỏng code đang sử dụng.
    public DinhNghiaBaoCao(
            String maBaoCao,
            String nhom,
            String tenBaoCao,
            String moTa,
            String tepSql,
            boolean kichHoat,
            String vaiTro,
            List<ThamSoDef> thamSo,
            boolean phanTrang
    ) {
        this(
                maBaoCao,
                nhom,
                tenBaoCao,
                moTa,
                tepSql,
                kichHoat,
                vaiTro,
                thamSo,
                phanTrang,
                Map.of(),
                List.of(),
                null
        );
    }

    // Constructor cũ 5 tham số.
    public DinhNghiaBaoCao(
            String maBaoCao,
            String tenBaoCao,
            String moTa,
            String tepSql,
            boolean kichHoat
    ) {
        this(
                maBaoCao,
                "Khác",
                tenBaoCao,
                moTa,
                tepSql,
                kichHoat,
                null,
                List.of(),
                false,
                Map.of(),
                List.of(),
                null
        );
    }

    // =====================================================
    // METADATA BẢNG
    // =====================================================

    public record BangDef(
            String nhan,
            boolean hienThi,
            String vaiTro,
            Map<String, CotDef> cacCot
    ) {
        public BangDef {
            nhan = nhan == null ? "" : nhan.trim();

            vaiTro = vaiTro == null || vaiTro.isBlank()
                    ? null
                    : vaiTro.trim();

            cacCot = cacCot == null
                    ? Map.of()
                    : Collections.unmodifiableMap(
                            new LinkedHashMap<>(cacCot)
                    );
        }

        public static BangDef hienThi(
                String nhan,
                Map<String, CotDef> cacCot
        ) {
            return new BangDef(nhan, true, null, cacCot);
        }

        public static BangDef an(String vaiTro) {
            return new BangDef("", false, vaiTro, Map.of());
        }
    }

    // =====================================================
    // METADATA CỘT
    // =====================================================

    public record CotDef(
            String nhan,
            boolean hienThi,
            String dinhDang
    ) {
        public CotDef {
            nhan = nhan == null || nhan.isBlank()
                    ? ""
                    : nhan.trim();

            dinhDang = dinhDang == null || dinhDang.isBlank()
                    ? "text"
                    : dinhDang.trim();
        }

        public static CotDef macDinh(String nhan) {
            return new CotDef(nhan, true, "text");
        }

        public static CotDef so(String nhan) {
            return new CotDef(nhan, true, "number");
        }

        public static CotDef tien(String nhan) {
            return new CotDef(nhan, true, "currency");
        }

        public static CotDef ngay(String nhan) {
            return new CotDef(nhan, true, "date");
        }

        public static CotDef an() {
            return new CotDef("", false, "text");
        }
    }

    // =====================================================
    // METADATA THẺ TỔNG QUAN
    // =====================================================

    public record TheDef(
            String ma,
            String nhan,
            String bang,
            String cot,
            String phepTinh,
            String dinhDang
    ) {
        public TheDef {
            if (ma == null || ma.isBlank()) {
                throw new IllegalArgumentException(
                        "Mã thẻ tổng quan không được để trống."
                );
            }

            if (nhan == null || nhan.isBlank()) {
                throw new IllegalArgumentException(
                        "Nhãn thẻ tổng quan không được để trống."
                );
            }

            if (bang == null || bang.isBlank()) {
                throw new IllegalArgumentException(
                        "Bảng dữ liệu của thẻ không được để trống."
                );
            }

            if (cot == null || cot.isBlank()) {
                throw new IllegalArgumentException(
                        "Cột dữ liệu của thẻ không được để trống."
                );
            }

            ma = ma.trim();
            nhan = nhan.trim();
            bang = bang.trim();
            cot = cot.trim();

            phepTinh = phepTinh == null || phepTinh.isBlank()
                    ? "FIRST"
                    : phepTinh.trim().toUpperCase();

            dinhDang = dinhDang == null || dinhDang.isBlank()
                    ? "number"
                    : dinhDang.trim();
        }
    }

    // =====================================================
    // METADATA PHÂN TRANG
    // =====================================================

    public record PhanTrangDef(
            String bangDuLieu,
            String bangDem,
            String cotDem,
            int kichThuocMacDinh
    ) {
        public PhanTrangDef {
            if (bangDuLieu == null || bangDuLieu.isBlank()) {
                throw new IllegalArgumentException(
                        "Bảng dữ liệu phân trang không được để trống."
                );
            }

            if (bangDem == null || bangDem.isBlank()) {
                throw new IllegalArgumentException(
                        "Bảng đếm phân trang không được để trống."
                );
            }

            if (cotDem == null || cotDem.isBlank()) {
                throw new IllegalArgumentException(
                        "Cột đếm phân trang không được để trống."
                );
            }

            if (kichThuocMacDinh <= 0) {
                throw new IllegalArgumentException(
                        "Kích thước trang phải lớn hơn 0."
                );
            }

            bangDuLieu = bangDuLieu.trim();
            bangDem = bangDem.trim();
            cotDem = cotDem.trim();
        }
    }
}
