package vn.pottel.reporting.baocao.runner;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.sql.Date;
import java.util.LinkedHashMap;
import java.time.LocalDate;

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
            Map<String, Object[]> thamSoTheoBang
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
                queryRunner.chayNhieuBang(
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
     * Chạy BC-DS-01 với các tham số lấy từ bộ lọc.
     *
     * bangCanChay: các mã bảng cần chạy (1A, 1B, 1C, 1D).
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

        Map<String, Object[]> thamSoTheoBang = new LinkedHashMap<>();

        // Bảng 1A: Không cần tham số.
        if (bangCanChay.contains("1A")) {
            thamSoTheoBang.put("1A", new Object[]{});
        }

        // Bảng 1B: 4 tham số.
        if (bangCanChay.contains("1B")) {
            thamSoTheoBang.put("1B", new Object[]{
                    tuNgay,
                    denNgay,
                    t.kichThuoc(),
                    t.tinhOffset()
            });
        }

        // 13 tham số lọc dùng chung cho bảng 1C và 1D.
        Object[] thamSoLoc = new Object[]{

                // 1-4: Lọc theo ngày đăng ký KIT (registed_date).
                tuNgay, tuNgay, denNgay, denNgay,

                // 5-6: Lọc MSISDN.
                msisdn, msisdn,

                // 7-8: Lọc trạng thái KIT.
                trangThaiKit, trangThaiKit,

                // 9-10: Lọc trạng thái đối soát.
                trangThaiDoiSoat, trangThaiDoiSoat,

                // 11-13: Lọc tình trạng GOI_MOI.
                tinhTrangGoiMoi,
                tinhTrangGoiMoi,
                tinhTrangGoiMoi
        };

        // Bảng 1C: 13 tham số lọc + 2 tham số phân trang.
        if (bangCanChay.contains("1C")) {
            Object[] thamSo1C = Arrays.copyOf(
                    thamSoLoc,
                    thamSoLoc.length + 2
            );

            thamSo1C[13] = t.kichThuoc();
            thamSo1C[14] = t.tinhOffset();

            thamSoTheoBang.put("1C", thamSo1C);
        }

        // Bảng 1D: 13 tham số để đếm tổng số dòng của 1C.
        if (bangCanChay.contains("1D")) {
            thamSoTheoBang.put("1D", thamSoLoc);
        }

        return chay("BC-DS-01", thamSoTheoBang);
    }

    /**
     * Lấy tổng số dòng của bảng 1C từ bảng đếm 1D.
     */
    public int demDongBang1C(ThamSoBaoCao t) {

        KetQuaBaoCao ketQua = chayBcDs01(t, Set.of("1D"));

        BangKetQua bangDem = ketQua.layBang("1D");

        if (bangDem == null || bangDem.dong().isEmpty()) {
            return 0;
        }

        Object giaTri = bangDem.dong().get(0).get("total_rows");

        return giaTri == null
                ? 0
                : ((Number) giaTri).intValue();
    }

    // =========================
    // BẢNG 1A - TỔNG QUAN
    // =========================

    public Map<String, Object> chayTongQuan() {
        return bcDs01Service.getSummary();
    }

    // =========================
    // BẢNG 1B - TỔNG HỢP THEO NGÀY
    // =========================

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

    // =========================
    // BẢNG 1C - CHI TIẾT THUÊ BAO
    // =========================

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

        // Chạy toàn bộ các bảng báo cáo BC-DS-01
        public KetQuaBaoCao chayBcDs01(ThamSoBaoCao thamSo) {
        return chayBcDs01(
                thamSo,
                Set.of("1A", "1B", "1C", "1D")
        );
        }
        // =========================
        // DANH SÁCH TRẠNG THÁI BỘ LỌC
        // =========================

        public List<String> layDanhSachTrangThaiKit() {
                return bcDs01Service.getKitStatuses();
        }

        public List<String> layDanhSachTrangThaiDoiSoat() {
                return bcDs01Service.getReconciliationStatuses();
        }

        
        // =========================
        // BC-DT-01 - DOANH THU THEO NGÀY
        // =========================

        public KetQuaBaoCao chayBcDt01(
                LocalDate tuNgay,
                LocalDate denNgay
        ) {
                if (tuNgay == null || denNgay == null) {
                throw new IllegalArgumentException(
                        "Ngày bắt đầu và ngày kết thúc không được để trống."
                );
                }

                Map<String, Object[]> thamSoTheoBang =
                        new LinkedHashMap<>();

                thamSoTheoBang.put("1A", new Object[]{
                        tuNgay.toString(),
                        denNgay.toString()
                });

                return chay("BC-DT-01", thamSoTheoBang);
        }
        // =========================
        // BC-DT-02 - CHI TIẾT DOANH THU
        // =========================

        public KetQuaBaoCao chayBcDt02(LocalDate ngay) {

        if (ngay == null) {
                throw new IllegalArgumentException(
                        "Ngày xem báo cáo không được để trống."
                );
        }

        Map<String, Object[]> thamSoTheoBang = new LinkedHashMap<>();

        thamSoTheoBang.put("1A", new Object[]{
                ngay.toString()
        });

        return chay("BC-DT-02", thamSoTheoBang);
        }

}