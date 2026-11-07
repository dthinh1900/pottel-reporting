package vn.pottel.reporting.baocao.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import vn.pottel.reporting.data.BangKetQua;

public record KetQuaBaoCao(
        String maBaoCao,
        String tenBaoCao,
        Map<String, BangKetQua> cacBang
) {
    public KetQuaBaoCao {
        cacBang = Collections.unmodifiableMap(
                new LinkedHashMap<>(cacBang)
        );
    }

    public BangKetQua layBang(String maBang) {
        return cacBang.get(maBang);
    }
}