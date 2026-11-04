
package vn.pottel.reporting.baocao.catalog;

public record DinhNghiaBaoCao(
        String maBaoCao,
        String tenBaoCao,
        String moTa,
        String tepSql,
        boolean kichHoat
) {

    public DinhNghiaBaoCao {
        if (maBaoCao == null || maBaoCao.isBlank()) {
            throw new IllegalArgumentException(
                    "Mã báo cáo không được để trống."
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
        tenBaoCao = tenBaoCao.trim();
        moTa = moTa == null ? "" : moTa.trim();
        tepSql = tepSql.trim();
    }
}
