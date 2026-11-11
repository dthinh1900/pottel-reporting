
package vn.pottel.reporting.baocao.catalog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import vn.pottel.reporting.baocao.catalog.DinhNghiaBaoCao.BangDef;
import vn.pottel.reporting.baocao.catalog.DinhNghiaBaoCao.CotDef;
import vn.pottel.reporting.baocao.catalog.DinhNghiaBaoCao.PhanTrangDef;
import vn.pottel.reporting.baocao.catalog.DinhNghiaBaoCao.TheDef;

import static vn.pottel.reporting.baocao.catalog.DinhNghiaBaoCao.*;

@Component
public class DanhMucBaoCao {

    private final Map<String, DinhNghiaBaoCao> danhSach =
            new LinkedHashMap<>();

    public DanhMucBaoCao() {

        // =====================================================
        // NHÓM ĐỐI SOÁT
        // =====================================================

        // BC-DS-01: Báo cáo đối soát KIT và GOI_MOI
        dangKy(new DinhNghiaBaoCao(
                "BC-DS-01",
                "Đối soát",
                "Báo cáo đối soát KIT và GOI_MOI",
                "Báo cáo gồm tổng quan và thống kê GOI_MOI theo ngày",
                "reports/bc-ds-01.sql",
                true,
                null,
                List.of(
                        ThamSoDef.ngay("tuNgay", "Từ ngày"),
                        ThamSoDef.ngay("denNgay", "Đến ngày")
                ),
                true,
                Map.of(
                        "1A", BangDef.hienThi(
                                "Tổng quan",
                                cot(
                                        "total_kit", CotDef.so("Tổng số KIT"),
                                        "unique_kit_msisdn", CotDef.so("Số MSISDN KIT duy nhất"),
                                        "total_goi_moi", CotDef.so("Tổng số GOI_MOI"),
                                        "unique_goi_moi_msisdn", CotDef.so("Số MSISDN GOI_MOI duy nhất"),
                                        "matched_msisdn", CotDef.so("Số thuê bao khớp"),
                                        "orphan_msisdn", CotDef.so("Số thuê bao không có KIT"),
                                        "missing_registed_date", CotDef.so("Thiếu ngày đăng ký"),
                                        "repeated_goi_moi_msisdn", CotDef.so("Thuê bao có GOI_MOI lặp")
                                )
                        ),
                        // Chưa khai báo cột 1B vì cần đối chiếu chính xác
                        // tên cột trả về từ function fn_bc_ds_01_1b.
                        "1B", BangDef.hienThi(
                                "Tổng hợp GOI_MOI theo ngày",
                                cot(
                                        "event_date", CotDef.ngay("Ngày phát sinh"),
                                        "event_count", CotDef.so("Số sự kiện GOI_MOI"),
                                        "unique_msisdn", CotDef.so("Số thuê bao duy nhất"),
                                        "matched_events", CotDef.so("Số sự kiện khớp"),
                                        "orphan_events", CotDef.so("Số sự kiện không có KIT"),
                                        "missing_registed_date_events",
                                        CotDef.so("Số sự kiện thiếu ngày đăng ký")
                                )
                        ),
                        "1D", BangDef.an("count")
                ),
                List.of(
                        the("totalKit", "Tổng số KIT", "1A", "total_kit"),
                        the("totalGoiMoi", "Tổng số GOI_MOI", "1A", "total_goi_moi"),
                        the("matched", "Thuê bao khớp", "1A", "matched_msisdn"),
                        the("orphan", "Thuê bao không có KIT", "1A", "orphan_msisdn"),
                        the("missingDate", "Thiếu ngày đăng ký", "1A", "missing_registed_date"),
                        the("repeated", "GOI_MOI lặp", "1A", "repeated_goi_moi_msisdn")
                ),
                new PhanTrangDef("1B", "1D", "total_rows", 20)
        ));

        // BC-DS-02: Chi tiết thuê bao KIT và GOI_MOI
        dangKy(new DinhNghiaBaoCao(
                "BC-DS-02",
                "Đối soát",
                "Báo cáo đối soát KIT và GOI_MOI - Phần 2",
                "Báo cáo chi tiết tình trạng đối soát của từng thuê bao",
                "reports/bc-ds-02.sql",
                true,
                null,
                List.of(
                        ThamSoDef.ngay("tuNgay", "Từ ngày"),
                        ThamSoDef.ngay("denNgay", "Đến ngày"),
                        ThamSoDef.chuoi("msisdn", "Số thuê bao"),
                        ThamSoDef.chuoi("trangThaiKit", "Trạng thái KIT"),
                        ThamSoDef.chuoi("trangThaiDoiSoat", "Trạng thái đối soát"),
                        ThamSoDef.chuoi("tinhTrangGoiMoi", "Tình trạng gói mới")
                ),
                true,
                Map.of(
                        "1C", BangDef.hienThi(
                                "Chi tiết thuê bao",
                                cot(
                                        "msisdn", CotDef.macDinh("Số thuê bao"),
                                        "kit_status", CotDef.macDinh("Trạng thái KIT"),
                                        "registed_date", CotDef.ngay("Ngày đăng ký"),
                                        "kit_row_count", CotDef.so("Số dòng KIT"),
                                        "event_count", CotDef.so("Số lần GOI_MOI"),
                                        "first_event_date", CotDef.ngay("Ngày GOI_MOI đầu tiên"),
                                        "last_event_date", CotDef.ngay("Ngày GOI_MOI gần nhất"),
                                        "reconciliation_status", CotDef.macDinh("Trạng thái đối soát")
                                )
                        ),
                        "1D", BangDef.an("count")
                ),
                List.of(),
                new PhanTrangDef("1C", "1D", "total_rows", 20)
        ));

        // =====================================================
        // NHÓM DOANH THU
        // =====================================================

        // BC-DT-01: Tổng hợp doanh thu theo ngày
        
        // BC-DT-01: Tổng hợp doanh thu theo ngày
        dangKy(new DinhNghiaBaoCao(
                "BC-DT-01",
                "Doanh thu",
                "Tổng hợp doanh thu theo ngày",
                "Báo cáo số lượng thuê bao và doanh thu theo ngày",
                "reports/bc-dt-01.sql",
                true,
                null,
                List.of(
                        ThamSoDef.ngay("tuNgay", "Từ ngày"),
                        ThamSoDef.ngay("denNgay", "Đến ngày")
                ),
                true,
                Map.of(
                        "1A", BangDef.hienThi(
                                "Tổng hợp doanh thu theo ngày",
                                cot(
                                        "ngay_bao_cao", CotDef.ngay("Ngày báo cáo"),
                                        "so_thue_bao", CotDef.so("Số thuê bao"),
                                        "doanh_thu_chua_vat", CotDef.tien("Doanh thu chưa VAT"),
                                        "tien_vat", CotDef.tien("Tiền VAT"),
                                        "tong_doanh_thu", CotDef.tien("Tổng doanh thu")
                                )
                        ),
                        "1B", BangDef.an("summary"),
                        "1D", BangDef.an("count")
                ),
                List.of(
                        the("subscriberCount", "Tổng lượt thuê bao",
                                "1B", "so_thue_bao", "SUM", "number"),

                        the("revenueNet", "Doanh thu chưa VAT",
                                "1B", "doanh_thu_chua_vat", "SUM", "currency"),

                        the("vat", "Tiền VAT",
                                "1B", "tien_vat", "SUM", "currency"),

                        the("revenueGross", "Tổng doanh thu",
                                "1B", "tong_doanh_thu", "SUM", "currency")
                ),
                new PhanTrangDef("1A", "1D", "total_rows", 20)
        ));


        // BC-DT-02: Chi tiết doanh thu theo thuê bao
        
        // BC-DT-02: Chi tiết doanh thu theo thuê bao
        dangKy(new DinhNghiaBaoCao(
                "BC-DT-02",
                "Doanh thu",
                "Chi tiết doanh thu theo thuê bao",
                "Chi tiết doanh thu phân bổ của từng thuê bao theo ngày",
                "reports/bc-dt-02.sql",
                true,
                null,
                List.of(
                        ThamSoDef.ngay("ngay", "Ngày báo cáo")
                ),
                true,
                Map.of(
                        "1A", BangDef.hienThi(
                                "Chi tiết doanh thu thuê bao",
                                cot(
                                        "ngay_bao_cao", CotDef.ngay("Ngày báo cáo"),
                                        "kit_id", CotDef.so("Mã KIT"),
                                        "msisdn", CotDef.macDinh("Số thuê bao"),
                                        "ma_goi", CotDef.macDinh("Mã gói"),
                                        "ngay_kich_hoat", CotDef.ngay("Ngày kích hoạt"),
                                        "ngay_d1", CotDef.ngay("Ngày D1"),
                                        "ngay_ket_thuc", CotDef.ngay("Ngày kết thúc"),
                                        "chu_ky", CotDef.so("Chu kỳ (ngày)"),
                                        "ngay_thu", CotDef.so("Ngày thứ"),
                                        "doanh_thu_chua_vat", CotDef.tien("Doanh thu chưa VAT"),
                                        "tien_vat", CotDef.tien("Tiền VAT"),
                                        "tong_doanh_thu", CotDef.tien("Tổng doanh thu")
                                )
                        ),
                        "1B", BangDef.an("summary"),
                        "1D", BangDef.an("count")
                ),
                List.of(
                        the("reportDate", "Ngày báo cáo",
                                "1B", "ngay_bao_cao", "FIRST", "date"),

                        the("subscriberCount", "Số thuê bao",
                                "1B", "so_thue_bao", "FIRST", "number"),

                        the("revenueNet", "Doanh thu chưa VAT",
                                "1B", "doanh_thu_chua_vat", "FIRST", "currency"),

                        the("vat", "Tiền VAT",
                                "1B", "tien_vat", "FIRST", "currency"),

                        the("revenueGross", "Tổng doanh thu",
                                "1B", "tong_doanh_thu", "FIRST", "currency")
                ),
                new PhanTrangDef("1A", "1D", "total_rows", 20)
        ));
    }

    // =====================================================
    // HÀM TẠO METADATA CỘT
    // =====================================================

    private static Map<String, CotDef> cot(Object... duLieu) {
        if (duLieu.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Dữ liệu cột phải gồm từng cặp mã cột và metadata."
            );
        }

        Map<String, CotDef> ketQua = new LinkedHashMap<>();

        for (int i = 0; i < duLieu.length; i += 2) {
            String maCot = (String) duLieu[i];
            CotDef dinhNghia = (CotDef) duLieu[i + 1];

            ketQua.put(maCot, dinhNghia);
        }

        return ketQua;
    }

    private static TheDef the(
            String ma,
            String nhan,
            String bang,
            String cot
    ) {
        return the(ma, nhan, bang, cot, "FIRST", "number");
    }

    private static TheDef the(
            String ma,
            String nhan,
            String bang,
            String cot,
            String phepTinh,
            String dinhDang
    ) {
        return new TheDef(
                ma,
                nhan,
                bang,
                cot,
                phepTinh,
                dinhDang
        );
    }

    // =====================================================
    // ĐĂNG KÝ VÀ TRA CỨU BÁO CÁO
    // =====================================================

    private void dangKy(DinhNghiaBaoCao baoCao) {
        if (danhSach.containsKey(baoCao.maBaoCao())) {
            throw new IllegalArgumentException(
                    "Mã báo cáo đã tồn tại: " + baoCao.maBaoCao()
            );
        }

        danhSach.put(baoCao.maBaoCao(), baoCao);
    }

    public DinhNghiaBaoCao lay(String maBaoCao) {
        return danhSach.get(maBaoCao);
    }

    public List<DinhNghiaBaoCao> layDanhSachHoatDong() {
        return danhSach.values()
                .stream()
                .filter(DinhNghiaBaoCao::kichHoat)
                .toList();
    }

    public List<DinhNghiaBaoCao> layTatCa() {
        return List.copyOf(danhSach.values());
    }

    public boolean dangHoatDong(String maBaoCao) {
        DinhNghiaBaoCao baoCao = lay(maBaoCao);
        return baoCao != null && baoCao.kichHoat();
    }
}
