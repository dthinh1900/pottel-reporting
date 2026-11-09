
package vn.pottel.reporting.baocao.catalog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class DanhMucBaoCao {

    private final Map<String, DinhNghiaBaoCao> danhSach =
            new LinkedHashMap<>();

    public DanhMucBaoCao() {
        dangKy(new DinhNghiaBaoCao(
                "BC-DS-01",
                "Báo cáo đối soát KIT và GOI_MOI",
                "Báo cáo gồm tổng quan, thống kê theo ngày và chi tiết thuê bao",
                "reports/bc-ds-01.sql",
                true
        ));
        dangKy(new DinhNghiaBaoCao(
                "BC-DT-01",
                "Tổng hợp doanh thu theo ngày",
                "Báo cáo số lượng thuê bao, doanh thu chưa VAT, tiền VAT và tổng doanh thu theo ngày",
                "reports/bc-dt-01.sql",
                true
        ));
        dangKy(new DinhNghiaBaoCao(
                "BC-DT-02",
                "Chi tiết doanh thu theo thuê bao",
                "Chi tiết doanh thu phân bổ của từng thuê bao theo ngày",
                "reports/bc-dt-02.sql",
                true
        ));
    }

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
