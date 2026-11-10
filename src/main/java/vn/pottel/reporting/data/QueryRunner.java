
package vn.pottel.reporting.data;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Component
public class QueryRunner {

    private static final int QUERY_TIMEOUT_SECONDS = 30;

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public QueryRunner(JdbcTemplate jdbcTemplate,
        NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    /**
     * Thực thi SELECT và trả về danh sách các dòng dữ liệu.
     */
    @Transactional(readOnly = true, timeout = 30)
    public List<Map<String, Object>> chayBang(
            String sql,
            Object... thamSo) {

        kiemTraSql(sql);

        return jdbcTemplate.query(
                connection -> {
                    PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                ResultSet.TYPE_FORWARD_ONLY,
                                ResultSet.CONCUR_READ_ONLY
                        );

                    statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

                    for (int i = 0; i < thamSo.length; i++) {
                        statement.setObject(i + 1, thamSo[i]);
                    }

                    return statement;
                },
                resultSet -> {
                    List<Map<String, Object>> ketQua = new ArrayList<>();

                    ResultSetMetaData metadata = resultSet.getMetaData();
                    int soCot = metadata.getColumnCount();

                    while (resultSet.next()) {
                        Map<String, Object> dong = new LinkedHashMap<>();

                        for (int i = 1; i <= soCot; i++) {
                            dong.put(
                                    metadata.getColumnLabel(i),
                                    resultSet.getObject(i)
                            );
                        }

                        ketQua.add(dong);
                    }

                    return ketQua;
                }
        );
    }

    /**
     * Thực thi SELECT chỉ trả về một giá trị.
     */
    @Transactional(readOnly = true, timeout = 30)
    public Object chayGiaTri(
            String sql,
            Object... thamSo) {

        kiemTraSql(sql);

        return jdbcTemplate.query(
                connection -> {
                    PreparedStatement statement =
                            connection.prepareStatement(sql);

                    statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

                    for (int i = 0; i < thamSo.length; i++) {
                        statement.setObject(i + 1, thamSo[i]);
                    }

                    return statement;
                },
                resultSet -> {
                    if (!resultSet.next()) {
                        return null;
                    }

                    return resultSet.getObject(1);
                }
        );
    }

    /**
 * Thực thi SELECT và trả về BangKetQua.
 * Giữ lại tên cột kể cả khi không có dòng dữ liệu.
 */
@Transactional(readOnly = true, timeout = 30)
public BangKetQua chayBangKetQua(String sql, Object... thamSo) {
    kiemTraSql(sql);

    return jdbcTemplate.execute(
            connection -> {
                PreparedStatement statement =
                        connection.prepareStatement(sql);

                statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

                for (int i = 0; i < thamSo.length; i++) {
                    statement.setObject(i + 1, thamSo[i]);
                }

                return statement;
            },
            (org.springframework.jdbc.core.PreparedStatementCallback<BangKetQua>) statement -> {

                // Dùng execute() thay cho executeQuery().
                boolean coKetQua = statement.execute();

                if (!coKetQua) {
                    throw new IllegalStateException(
                            "SQL không trả về bảng kết quả."
                    );
                }

                try (ResultSet resultSet = statement.getResultSet()) {

                    ResultSetMetaData metadata =
                            resultSet.getMetaData();

                    int soCot = metadata.getColumnCount();

                    List<String> danhSachCot = new ArrayList<>();

                    for (int i = 1; i <= soCot; i++) {
                        danhSachCot.add(metadata.getColumnLabel(i));
                    }

                    List<Map<String, Object>> danhSachDong =
                            new ArrayList<>();

                    while (resultSet.next()) {
                        Map<String, Object> dong =
                                new LinkedHashMap<>();

                        for (int i = 1; i <= soCot; i++) {
                            dong.put(
                                    danhSachCot.get(i - 1),
                                    resultSet.getObject(i)
                            );
                        }

                        danhSachDong.add(dong);
                    }

                    return new BangKetQua(
                            danhSachCot,
                            danhSachDong
                    );
                }
            }
    );
}
/**
 * Thực thi SELECT bằng tham số có tên.
 * Giữ lại tên cột kể cả khi không có dòng dữ liệu.
 */
/**
 * Thực thi SELECT bằng tham số có tên.
 * Giữ lại tên cột kể cả khi không có dòng dữ liệu.
 */
@Transactional(readOnly = true, timeout = 30)
public BangKetQua chayBangKetQuaTheoTen(
        String sql,
        Map<String, ?> thamSo
) {
    kiemTraSql(sql);

    if (thamSo == null) {
        thamSo = Collections.emptyMap();
    }

    return namedParameterJdbcTemplate.query(
            sql,
            thamSo,
            resultSet -> {
                ResultSetMetaData metadata = resultSet.getMetaData();
                int soCot = metadata.getColumnCount();

                List<String> danhSachCot = new ArrayList<>();

                for (int i = 1; i <= soCot; i++) {
                    danhSachCot.add(metadata.getColumnLabel(i));
                }

                List<Map<String, Object>> danhSachDong =
                        new ArrayList<>();

                while (resultSet.next()) {
                    Map<String, Object> dong = new LinkedHashMap<>();

                    for (int i = 1; i <= soCot; i++) {
                        dong.put(
                                danhSachCot.get(i - 1),
                                resultSet.getObject(i)
                        );
                    }

                    danhSachDong.add(dong);
                }

                return new BangKetQua(danhSachCot, danhSachDong);
            }
    );
}

/**
 * Chạy nhiều câu SELECT bằng tham số có tên.
 *
 * cacTruyVan:
 * Mã bảng -> câu SQL.
 *
 * thamSoTheoBang:
 * Mã bảng -> các tham số có tên riêng của bảng đó.
 */
@Transactional(readOnly = true, timeout = 30)
public Map<String, BangKetQua> chayNhieuBangTheoTen(
        Map<String, String> cacTruyVan,
        Map<String, Map<String, Object>> thamSoTheoBang
) {
    if (cacTruyVan == null || cacTruyVan.isEmpty()) {
        throw new IllegalArgumentException(
                "Danh sách câu SQL không được để trống."
        );
    }

    if (thamSoTheoBang == null) {
        thamSoTheoBang = Collections.emptyMap();
    }

    for (String maBang : thamSoTheoBang.keySet()) {
        if (!cacTruyVan.containsKey(maBang)) {
            throw new IllegalArgumentException(
                    "Không tìm thấy bảng SQL tương ứng với tham số: "
                            + maBang
            );
        }
    }

    Map<String, BangKetQua> ketQua = new LinkedHashMap<>();

    for (Map.Entry<String, String> muc : cacTruyVan.entrySet()) {
        String maBang = muc.getKey();
        String sql = muc.getValue();

        Map<String, Object> thamSo = thamSoTheoBang.get(maBang);

        BangKetQua bang = chayBangKetQuaTheoTen(sql, thamSo);

        ketQua.put(maBang, bang);
    }

    return Collections.unmodifiableMap(
            new LinkedHashMap<>(ketQua)
    );
}
    /**
     * Chạy nhiều câu SELECT và gom kết quả thành nhiều bảng.
     *
     * cacTruyVan:
     * Mã bảng -> câu SQL.
     *
     * thamSoTheoBang:
     * Mã bảng -> danh sách tham số riêng của bảng đó.
     */
    @Transactional(readOnly = true, timeout = 30)
    public Map<String, BangKetQua> chayNhieuBang(
            Map<String, String> cacTruyVan,
            Map<String, Object[]> thamSoTheoBang
    ) {
        if (cacTruyVan == null || cacTruyVan.isEmpty()) {
            throw new IllegalArgumentException(
                    "Danh sách câu SQL không được để trống."
            );
        }

        if (thamSoTheoBang == null) {
            thamSoTheoBang = Collections.emptyMap();
        }

        for (String maBang : thamSoTheoBang.keySet()) {
            if (!cacTruyVan.containsKey(maBang)) {
                throw new IllegalArgumentException(
                        "Không tìm thấy bảng SQL tương ứng với tham số: "
                                + maBang
                );
            }
        }

        Map<String, BangKetQua> ketQua = new LinkedHashMap<>();

        for (Map.Entry<String, String> muc : cacTruyVan.entrySet()) {
            String maBang = muc.getKey();
            String sql = muc.getValue();

            Object[] thamSo = thamSoTheoBang.get(maBang);

            if (thamSo == null) {
                thamSo = new Object[0];
            }

            BangKetQua bang = chayBangKetQua(sql, thamSo);
            ketQua.put(maBang, bang);
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(ketQua)
        );
    }

    /**
     * Kiểm tra câu SQL đầu vào.
     */
    private void kiemTraSql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException(
                    "Câu lệnh SQL không được để trống."
            );
        }
    }
}
