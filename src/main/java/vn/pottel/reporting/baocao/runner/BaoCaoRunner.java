
package vn.pottel.reporting.baocao.runner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import vn.pottel.reporting.baocao.catalog.DanhMucBaoCao;
import vn.pottel.reporting.baocao.catalog.DinhNghiaBaoCao;
import vn.pottel.reporting.baocao.model.KetQuaBaoCao;
import vn.pottel.reporting.data.BangKetQua;
import vn.pottel.reporting.data.QueryRunner;
import vn.pottel.reporting.data.SqlStore;

@Component
public class BaoCaoRunner {

    private final DanhMucBaoCao danhMucBaoCao;
    private final SqlStore sqlStore;
    private final QueryRunner queryRunner;

    public BaoCaoRunner(
            DanhMucBaoCao danhMucBaoCao,
            SqlStore sqlStore,
            QueryRunner queryRunner
    ) {
        this.danhMucBaoCao = danhMucBaoCao;
        this.sqlStore = sqlStore;
        this.queryRunner = queryRunner;
    }

    // =====================================================
    // THỰC THI BÁO CÁO THEO METADATA
    // =====================================================

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

        // Chạy tất cả bảng SQL, bao gồm bảng ẩn dùng để đếm dữ liệu.
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

    // =====================================================
    // KIỂM TRA ĐỊNH NGHĨA BÁO CÁO
    // =====================================================

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

    // =====================================================
    // ĐỌC THAM SỐ SỐ NGUYÊN
    // =====================================================

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
}
