package vn.pottel.reporting.baocao.module;

import org.springframework.stereotype.Component;

@Component
public class BaoCaoModule {

    public String getTenModule() {
        return "Hệ thống báo cáo Pottel";
    }

    public String[] getDanhSachBaoCao() {
        return new String[] {
            "BC-DS-01"
        };
    }

    public boolean hoTroBaoCao(String maBaoCao) {
        for (String ma : getDanhSachBaoCao()) {
            if (ma.equalsIgnoreCase(maBaoCao)) {
                return true;
            }
        }

        return false;
    }
}