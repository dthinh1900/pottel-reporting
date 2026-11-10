
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

    /**
     * Chạy một báo cáo có nhiều bảng kết quả.
     * Chỉ các bảng có tên trong thamSoTheoBang mới được chạy.
     */
    public KetQuaBaoCao chay(
            String maBaoCao,
            Map<String, Map<String, Object>> thamSoTheoBang
    ) {
        DinhNghiaBaoCao dinhNghia = danhMucBaoCao.lay(maBaoCao);

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

        String noiDungSql = sqlStore.lay(dinhNghia.tepSql());

        Map<String, String> tatCaTruyVan =
                sqlStore.tachCacBang(noiDungSql);
        
        

        // Chỉ chạy những bảng được yêu cầu.
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

    // =====================================================
    // BC-DS-01: TỔNG QUAN VÀ TỔNG HỢP THEO NGÀY
    // Gồm bảng 1A và 1B.
    // =====================================================

    /**
     * Chạy các bảng được yêu cầu của BC-DS-01.
     *
     * bangCanChay: 1A, 1B.
     */
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

        // Bảng 1A: Không cần tham số.
        if (bangCanChay.contains("1A")) {
            thamSoTheoBang.put("1A", Map.of());
        }

        // Bảng 1B: Tham số ngày và phân trang.
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

    /**
     * Chạy toàn bộ BC-DS-01: bảng 1A và 1B.
     */
    public KetQuaBaoCao chayBcDs01(ThamSoBaoCao thamSo) {
        return chayBcDs01(
                thamSo,
                Set.of("1A", "1B")
        );
    }

    // =====================================================
    // BC-DS-02: CHI TIẾT THUÊ BAO
    // Gồm bảng 1C và 1D.
    // =====================================================

    /**
     * Chạy các bảng được yêu cầu của BC-DS-02.
     *
     * bangCanChay: 1C, 1D.
     */
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

        // Sáu tham số lọc dùng chung cho bảng 1C và 1D.
        Map<String, Object> thamSoLoc = new LinkedHashMap<>();

        thamSoLoc.put("tuNgay", tuNgay);
        thamSoLoc.put("denNgay", denNgay);
        thamSoLoc.put("msisdn", msisdn);
        thamSoLoc.put("trangThaiKit", trangThaiKit);
        thamSoLoc.put("trangThaiDoiSoat", trangThaiDoiSoat);
        thamSoLoc.put("tinhTrangGoiMoi", tinhTrangGoiMoi);

        Map<String, Map<String, Object>> thamSoTheoBang =
                new LinkedHashMap<>();

        // Bảng 1C: Bộ lọc và phân trang.
        if (bangCanChay.contains("1C")) {
            Map<String, Object> thamSo1C =
                    new LinkedHashMap<>(thamSoLoc);

            thamSo1C.put("limit", t.kichThuoc());
            thamSo1C.put("offset", t.tinhOffset());

            thamSoTheoBang.put("1C", thamSo1C);
        }

        // Bảng 1D: Chỉ có bộ lọc, không phân trang.
        if (bangCanChay.contains("1D")) {
            thamSoTheoBang.put(
                    "1D",
                    new LinkedHashMap<>(thamSoLoc)
            );
        }

        return chay("BC-DS-02", thamSoTheoBang);
    }

    /**
     * Chạy toàn bộ BC-DS-02: bảng 1C và 1D.
     */
    public KetQuaBaoCao chayBcDs02(ThamSoBaoCao thamSo) {
        return chayBcDs02(
                thamSo,
                Set.of("1C", "1D")
        );
    }

    /**
     * Lấy tổng số dòng của bảng 1C
     * thông qua bảng đếm 1D thuộc BC-DS-02.
     */
    public int demDongBang1C(ThamSoBaoCao t) {
        KetQuaBaoCao ketQua = chayBcDs02(
                t,
                Set.of("1D")
        );

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

    // Bảng 1A - Tổng quan.
    public Map<String, Object> chayTongQuan() {
        return bcDs01Service.getSummary();
    }

    // Bảng 1B - Tổng hợp theo ngày.
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

    // Bảng 1C - Chi tiết thuê bao (service cũ).
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

    // =====================================================
    // DANH SÁCH TRẠNG THÁI BỘ LỌC
    // =====================================================

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
