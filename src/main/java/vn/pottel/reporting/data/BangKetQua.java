
package vn.pottel.reporting.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BangKetQua {

    private final List<String> cot;
    private final List<Map<String, Object>> dong;

    public BangKetQua(
            List<String> cot,
            List<Map<String, Object>> dong) {

        this.cot = cot == null
                ? List.of()
                : List.copyOf(cot);

        if (dong == null) {
            this.dong = List.of();
        } else {
            List<Map<String, Object>> banSao = new ArrayList<>();

            for (Map<String, Object> item : dong) {
                banSao.add(
                        Collections.unmodifiableMap(
                                new LinkedHashMap<>(item)
                        )
                );
            }

            this.dong = Collections.unmodifiableList(banSao);
        }
    }

    // Lấy danh sách tên cột
    public List<String> cot() {
        return cot;
    }

    // Lấy danh sách các dòng dữ liệu
    public List<Map<String, Object>> dong() {
        return dong;
    }

    // Lấy tổng số dòng
    public int soLuongDong() {
        return dong.size();
    }

    // Kiểm tra bảng có dữ liệu hay không
    public boolean coDuLieu() {
        return !dong.isEmpty();
    }
}
