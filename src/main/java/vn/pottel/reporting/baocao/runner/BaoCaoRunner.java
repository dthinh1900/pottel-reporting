
package vn.pottel.reporting.baocao.runner;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import vn.pottel.reporting.baocao.catalog.DanhMucBaoCao;
import vn.pottel.reporting.baocao.catalog.DinhNghiaBaoCao;
import vn.pottel.reporting.baocao.model.KetQuaBaoCao;
import vn.pottel.reporting.baocao.model.ThamSoBaoCao;
import vn.pottel.reporting.baocao.service.BcDs01Service;
import vn.pottel.reporting.data.BangKetQua;
import vn.pottel.reporting.data.QueryRunner;
import vn.pottel.reporting.data.SqlStore;

@Component
public class BaoCaoRunner {

    private final BcDs01Service bcDs01Service;
    private final DanhMucBaoCao danhMucBaoCao;
    private final SqlStore sqlStore;
    private final QueryRunner queryRunner;

    public BaoCaoRunner(
            BcDs01Service bcDs01Service,
            DanhMucBaoCao danhMucBaoCao,
            SqlStore sqlStore,
            QueryRunner queryRunner
    ) {
        this.bcDs01Service = bcDs01Service;
        this.danhMucBaoCao = danhMucBaoCao;
        this.sqlStore = sqlStore;
        this.queryRunner = queryRunner;
    }

    // =====================================================
    // THỰC THI BÁO CÁO TỔNG QUÁT
    // =====================================================

    /**
     * Chạy những bảng được chỉ định.
     *
     * Phương thức này vẫn được giữ để tương thích với
     * các API và service cũ.
     */
    public KetQuaBaoCao chay(
            String maBaoCao,
            Map<String, Map<String, Object>> thamSoTheoBang
    ) {
        DinhNghiaBaoCao dinhNghia = layDinhNghia(maBaoCao);

        if (thamSoTheoBang == null || thamSoTheoBang.isEmpty()) {
            throw new IllegalArgumentException(
                    "Danh sách bảng cần chạy không được để trống."
            );
        }

        String noiDungSql = sqlStore.lay(dinhNghia.tepSql());

        Map<String, String> tatCaTruyVan =
                sqlStore.tachCacBang(noiDungSql);

        Map<String, String> cacTruyVan = new LinkedHashMap<>();

        for (String maBang : thamSoTheoBang.keySet()) {
            String sql = tatCaTruyVan.get(maBang);

            if (sql == null) {
                throw new IllegalArgumentException(
                        "Không tìm thấy bảng SQL: " + maBang
                );
            }

            cacTruyVan.put(maBang, sql);
        }

        Map<String, BangKetQua> cacBang =
                queryRunner.chayNhieuBangTheoTen(
                        cacTruyVan,
                        thamSoTheoBang
                );

        return new KetQuaBaoCao(
                dinhNghia.maBaoCao(),
                dinhNghia.tenBaoCao(),
                cacBang
        );
    }

    /**
     * Chạy báo cáo theo metadata trong DanhMucBaoCao.
     *
     * Tự động:
     * 1. Kiểm tra báo cáo tồn tại và đang hoạt động.
     * 2. Đọc file SQL đúng một lần.
     * 3. Tách các bảng @table.
     * 4. Bổ sung limit và offset theo metadata phân trang.
     * 5. Chạy các bảng SQL và trả kết quả.
     */
    public KetQuaBaoCao chayTheoMetadata(
            String maBaoCao,
            Map<String, Object> thamSo
    ) {
        DinhNghiaBaoCao dinhNghia = layDinhNghia(maBaoCao);

        Map<String, Object> thamSoChung = new LinkedHashMap<>();

        if (thamSo != null) {
            thamSoChung.putAll(thamSo);
        }

        // Bổ sung tham số phân trang theo metadata.
        if (dinhNghia.phanTrang()) {
            DinhNghiaBaoCao.PhanTrangDef phanTrang =
                    dinhNghia.cauHinhPhanTrang();

            int page = laySoNguyen(thamSoChung, "page", 1);
            int pageSize = laySoNguyen(
                    thamSoChung,
                    "pageSize",
                    phanTrang.kichThuocMacDinh()
            );

            if (page < 1) {
                throw new IllegalArgumentException(
                        "Số trang phải lớn hơn hoặc bằng 1."
                );
            }

            if (!Set.of(10, 20, 50, 100).contains(pageSize)) {
                throw new IllegalArgumentException(
                        "Page size chỉ nhận 10, 20, 50 hoặc 100."
                );
            }

            thamSoChung.put("limit", pageSize);
            thamSoChung.put("offset", (page - 1) * pageSize);
        }

        // Đọc và tách SQL đúng một lần.
        String noiDungSql = sqlStore.lay(dinhNghia.tepSql());

        Map<String, String> tatCaTruyVan =
                sqlStore.tachCacBang(noiDungSql);

        if (tatCaTruyVan.isEmpty()) {
            throw new IllegalStateException(
                    "File SQL không có bảng @table: "
                            + dinhNghia.tepSql()
            );
        }

        // Kiểm tra metadata có khai báo bảng không tồn tại trong SQL.
        for (String maBang : dinhNghia.cacBang().keySet()) {
            if (!tatCaTruyVan.containsKey(maBang)) {
                throw new IllegalStateException(
                        "Metadata báo cáo " + maBaoCao
                                + " khai báo bảng " + maBang
                                + " nhưng file SQL không có @table tương ứng."
                );
            }
        }

        // Chạy tất cả bảng SQL.
        // Bao gồm cả bảng ẩn dùng để đếm tổng số dòng.
        Map<String, String> cacTruyVan = new LinkedHashMap<>();
        Map<String, Map<String, Object>> thamSoTheoBang =
                new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : tatCaTruyVan.entrySet()) {
            String maBang = entry.getKey();

            cacTruyVan.put(maBang, entry.getValue());
            thamSoTheoBang.put(
                    maBang,
                    new LinkedHashMap<>(thamSoChung)
            );
        }

        Map<String, BangKetQua> cacBang =
                queryRunner.chayNhieuBangTheoTen(
                        cacTruyVan,
                        thamSoTheoBang
                );

        return new KetQuaBaoCao(
                dinhNghia.maBaoCao(),
                dinhNghia.tenBaoCao(),
                cacBang
        );
    }

    private DinhNghiaBaoCao layDinhNghia(String maBaoCao) {
        DinhNghiaBaoCao dinhNghia =
                danhMucBaoCao.lay(maBaoCao);

        if (dinhNghia == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy báo cáo: " + maBaoCao
            );
        }

        if (!dinhNghia.kichHoat()) {
            throw new IllegalStateException(
                    "Báo cáo đang bị tắt: " + maBaoCao
            );
        }

        return dinhNghia;
    }

    private int laySoNguyen(
            Map<String, Object> thamSo,
            String ten,
            int macDinh
    ) {
        Object giaTri = thamSo.get(ten);

        if (giaTri == null) {
            return macDinh;
        }

        try {
            return Integer.parseInt(giaTri.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Tham số " + ten + " phải là số nguyên."
            );
        }
    }

    // =====================================================
    // CÁC PHƯƠNG THỨC CŨ - CHƯA XÓA TRONG GIAI ĐOẠN MIGRATION
    // =====================================================

    // BC-DS-01: Chạy các bảng được yêu cầu.
    public KetQuaBaoCao chayBcDs01(
            ThamSoBaoCao t,
            Set<String> bangCanChay
    ) {
        if (t == null) {
            throw new IllegalArgumentException(
                    "Tham số báo cáo không được để trống."
            );
        }

        if (bangCanChay == null || bangCanChay.isEmpty()) {
            throw new IllegalArgumentException(
                    "Danh sách bảng cần chạy không được để trống."
            );
        }

        Map<String, Map<String, Object>> thamSoTheoBang =
                new LinkedHashMap<>();

        if (bangCanChay.contains("1A")) {
            thamSoTheoBang.put("1A", Map.of());
        }

        if (bangCanChay.contains("1B")) {
            Map<String, Object> thamSo1B = new LinkedHashMap<>();

            thamSo1B.put(
                    "tuNgay",
                    t.tuNgay() == null ? null : Date.valueOf(t.tuNgay())
            );

            thamSo1B.put(
                    "denNgay",
                    t.denNgay() == null ? null : Date.valueOf(t.denNgay())
            );

            thamSo1B.put("limit", t.kichThuoc());
            thamSo1B.put("offset", t.tinhOffset());

            thamSoTheoBang.put("1B", thamSo1B);
        }

        return chay("BC-DS-01", thamSoTheoBang);
    }

    public KetQuaBaoCao chayBcDs01(ThamSoBaoCao thamSo) {
        return chayBcDs01(thamSo, Set.of("1A", "1B"));
    }

    // BC-DS-02: Chạy các bảng được yêu cầu.
    public KetQuaBaoCao chayBcDs02(
            ThamSoBaoCao t,
            Set<String> bangCanChay
    ) {
        if (t == null) {
            throw new IllegalArgumentException(
                    "Tham số báo cáo không được để trống."
            );
        }

        if (bangCanChay == null || bangCanChay.isEmpty()) {
            throw new IllegalArgumentException(
                    "Danh sách bảng cần chạy không được để trống."
            );
        }

        Date tuNgay = t.tuNgay() == null
                ? null : Date.valueOf(t.tuNgay());

        Date denNgay = t.denNgay() == null
                ? null : Date.valueOf(t.denNgay());

        String msisdn = t.msisdn() == null || t.msisdn().isBlank()
                ? null : "%" + t.msisdn().trim() + "%";

        String trangThaiKit =
                t.trangThaiKit() == null || t.trangThaiKit().isBlank()
                        ? null : t.trangThaiKit().trim();

        String trangThaiDoiSoat =
                t.trangThaiDoiSoat() == null
                        || t.trangThaiDoiSoat().isBlank()
                        ? null : t.trangThaiDoiSoat().trim();

        String tinhTrangGoiMoi =
                t.tinhTrangGoiMoi() == null
                        || t.tinhTrangGoiMoi().isBlank()
                        ? null : t.tinhTrangGoiMoi().trim();

        Map<String, Object> thamSoLoc = new LinkedHashMap<>();

        thamSoLoc.put("tuNgay", tuNgay);
        thamSoLoc.put("denNgay", denNgay);
        thamSoLoc.put("msisdn", msisdn);
        thamSoLoc.put("trangThaiKit", trangThaiKit);
        thamSoLoc.put("trangThaiDoiSoat", trangThaiDoiSoat);
        thamSoLoc.put("tinhTrangGoiMoi", tinhTrangGoiMoi);

        Map<String, Map<String, Object>> thamSoTheoBang =
                new LinkedHashMap<>();

        if (bangCanChay.contains("1C")) {
            Map<String, Object> thamSo1C =
                    new LinkedHashMap<>(thamSoLoc);

            thamSo1C.put("limit", t.kichThuoc());
            thamSo1C.put("offset", t.tinhOffset());

            thamSoTheoBang.put("1C", thamSo1C);
        }

        if (bangCanChay.contains("1D")) {
            thamSoTheoBang.put(
                    "1D",
                    new LinkedHashMap<>(thamSoLoc)
            );
        }

        return chay("BC-DS-02", thamSoTheoBang);
    }

    public KetQuaBaoCao chayBcDs02(ThamSoBaoCao thamSo) {
        return chayBcDs02(thamSo, Set.of("1C", "1D"));
    }

    public int demDongBang1C(ThamSoBaoCao t) {
        KetQuaBaoCao ketQua = chayBcDs02(t, Set.of("1D"));

        BangKetQua bangDem = ketQua.layBang("1D");

        if (bangDem == null || bangDem.dong().isEmpty()) {
            return 0;
        }

        Object giaTri = bangDem.dong().get(0).get("total_rows");

        return giaTri == null
                ? 0
                : ((Number) giaTri).intValue();
    }

    // =====================================================
    // CÁC PHƯƠNG THỨC SERVICE BC-DS-01 CŨ
    // =====================================================

    public Map<String, Object> chayTongQuan() {
        return bcDs01Service.getSummary();
    }

    public List<Map<String, Object>> chayBang1B(ThamSoBaoCao t) {
        return bcDs01Service.getDailySummary(
                t.tuNgay(),
                t.denNgay(),
                t.kichThuoc(),
                t.tinhOffset()
        );
    }

    public int demSoNgayBang1B(ThamSoBaoCao t) {
        return bcDs01Service.countDailySummary(
                t.tuNgay(),
                t.denNgay()
        );
    }

    public List<Map<String, Object>> chayBang1C(ThamSoBaoCao t) {
        return bcDs01Service.getSubscriberDetails(
                t.tuNgay(),
                t.denNgay(),
                t.msisdn(),
                t.trangThaiKit(),
                t.trangThaiDoiSoat(),
                t.tinhTrangGoiMoi(),
                t.kichThuoc(),
                t.tinhOffset()
        );
    }

    public List<String> layDanhSachTrangThaiKit() {
        return bcDs01Service.getKitStatuses();
    }

    public List<String> layDanhSachTrangThaiDoiSoat() {
        return bcDs01Service.getReconciliationStatuses();
    }

    // =====================================================
    // BC-DT-01 - DOANH THU THEO NGÀY
    // =====================================================

    public KetQuaBaoCao chayBcDt01(
            LocalDate tuNgay,
            LocalDate denNgay
    ) {
        if (tuNgay == null || denNgay == null) {
            throw new IllegalArgumentException(
                    "Ngày bắt đầu và ngày kết thúc không được để trống."
            );
        }

        Map<String, Map<String, Object>> thamSoTheoBang =
                new LinkedHashMap<>();

        Map<String, Object> thamSo1A = new LinkedHashMap<>();
        thamSo1A.put("tuNgay", tuNgay);
        thamSo1A.put("denNgay", denNgay);

        thamSoTheoBang.put("1A", thamSo1A);

        return chay("BC-DT-01", thamSoTheoBang);
    }

    // =====================================================
    // BC-DT-02 - CHI TIẾT DOANH THU
    // =====================================================

    public KetQuaBaoCao chayBcDt02(LocalDate ngay) {
        if (ngay == null) {
            throw new IllegalArgumentException(
                    "Ngày xem không được để trống."
            );
        }

        Map<String, Map<String, Object>> thamSoTheoBang =
                new LinkedHashMap<>();

        Map<String, Object> thamSoChiTiet = new LinkedHashMap<>();
        thamSoChiTiet.put("ngay", ngay.toString());

        thamSoTheoBang.put("1A", thamSoChiTiet);

        return chay("BC-DT-02", thamSoTheoBang);
    }
}
