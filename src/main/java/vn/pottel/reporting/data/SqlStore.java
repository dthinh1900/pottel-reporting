
package vn.pottel.reporting.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collections;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class SqlStore {

    private static final String SQL_ROOT = "classpath:sql/";

    private static final Pattern INCLUDE_PATTERN =
            Pattern.compile(
                    "(?m)^\\s*--\\s*@include\\s+([^\\r\\n]+)\\s*$"
            );

    private static final Pattern TABLE_PATTERN =
            Pattern.compile(
                    "(?m)^\\s*--\\s*@table\\s+([A-Za-z0-9_-]+)\\s*$"
            );

    private final ResourceLoader resourceLoader;

    private final Map<String, String> cache =
            new ConcurrentHashMap<>();

    public SqlStore(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Đọc một file SQL và xử lý các lệnh @include.
     */
    public String lay(String tepSql) {
        String duongDan = chuanHoaDuongDan(tepSql);

        return docVaXuLyInclude(
                duongDan,
                new HashSet<>()
        );
    }

    /**
     * Đọc file SQL và xử lý các file được include.
     */
    private String docVaXuLyInclude(
            String duongDan,
            Set<String> dangDoc) {

        if (!dangDoc.add(duongDan)) {
            throw new IllegalStateException(
                    "Phát hiện vòng lặp @include tại: " + duongDan
            );
        }

        try {
            String noiDung = cache.computeIfAbsent(
                    duongDan,
                    this::docFile
            );

            Matcher matcher = INCLUDE_PATTERN.matcher(noiDung);
            StringBuffer ketQua = new StringBuffer();

            while (matcher.find()) {
                String fileInclude = matcher.group(1).trim();

                String duongDanInclude = chuanHoaDuongDan(
                        Paths.get(duongDan)
                                .getParent()
                                .resolve(fileInclude)
                                .toString()
                );

                String noiDungInclude = docVaXuLyInclude(
                        duongDanInclude,
                        dangDoc
                );

                matcher.appendReplacement(
                        ketQua,
                        Matcher.quoteReplacement(noiDungInclude)
                );
            }

            matcher.appendTail(ketQua);

            return ketQua.toString();

        } finally {
            dangDoc.remove(duongDan);
        }
    }

    /**
     * Tách một file SQL thành nhiều truy vấn theo mã @table.
     *
     * Ví dụ:
     * -- @table 1A
     * SELECT ...
     *
     * -- @table 1B
     * SELECT ...
     *
     * Kết quả: Map gồm 1A và 1B.
     */
    public Map<String, String> tachCacBang(String noiDungSql) {

        if (noiDungSql == null || noiDungSql.isBlank()) {
            throw new IllegalArgumentException(
                    "Nội dung SQL không được để trống."
            );
        }

        Matcher matcher = TABLE_PATTERN.matcher(noiDungSql);

        Map<String, String> cacBang = new LinkedHashMap<>();

        int viTriBatDau = -1;
        String maBangHienTai = null;

        while (matcher.find()) {

            // Kiểm tra phần trước marker đầu tiên.
            // Chỉ cho phép chú thích SQL, không cho phép câu lệnh SQL.
            if (viTriBatDau == -1) {
                String phanTruoc = noiDungSql
                        .substring(0, matcher.start())
                        .trim();

                if (!phanTruoc.isEmpty()
                        && !laPhanChuThichSql(phanTruoc)) {
                    throw new IllegalArgumentException(
                            "Có nội dung SQL nằm trước @table đầu tiên."
                    );
                }

            } else {
                String sqlBang = noiDungSql
                        .substring(viTriBatDau, matcher.start())
                        .trim();

                if (sqlBang.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Truy vấn của bảng " + maBangHienTai
                                    + " không được để trống."
                    );
                }

                if (cacBang.putIfAbsent(maBangHienTai, sqlBang) != null) {
                    throw new IllegalArgumentException(
                            "Mã bảng bị trùng: " + maBangHienTai
                    );
                }
            }

            maBangHienTai = matcher.group(1).trim();
            viTriBatDau = matcher.end();
        }

        if (viTriBatDau == -1) {
            throw new IllegalArgumentException(
                    "File SQL không có marker @table."
            );
        }

        // Lấy truy vấn cuối cùng.
        String sqlCuoi = noiDungSql
                .substring(viTriBatDau)
                .trim();

        if (sqlCuoi.isEmpty()) {
            throw new IllegalArgumentException(
                    "Truy vấn của bảng " + maBangHienTai
                            + " không được để trống."
            );
        }

        if (cacBang.putIfAbsent(maBangHienTai, sqlCuoi) != null) {
            throw new IllegalArgumentException(
                    "Mã bảng bị trùng: " + maBangHienTai
            );
        }

        // Trả về Map không thể chỉnh sửa nhưng vẫn giữ thứ tự bảng.
        return Collections.unmodifiableMap(
                new LinkedHashMap<>(cacBang)
        );
    }


    /**
     * Kiểm tra phần nội dung có chỉ gồm chú thích SQL hay không.
     */
    private boolean laPhanChuThichSql(String noiDung) {

        String noiDungDaXoaChuThich = noiDung
                .replaceAll("(?s)/\\*.*?\\*/", "")
                .replaceAll("(?m)--.*$", "")
                .trim();

        return noiDungDaXoaChuThich.isEmpty();
    }

    /**
     * Đọc nội dung một file SQL từ resources.
     */
    private String docFile(String duongDan) {

        Resource resource = resourceLoader.getResource(
                SQL_ROOT + duongDan
        );

        try (InputStream inputStream = resource.getInputStream()) {

            return new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Không thể đọc file SQL: " + duongDan,
                    e
            );
        }
    }

    /**
     * Chuẩn hóa đường dẫn và ngăn truy cập ra ngoài thư mục sql.
     */
    private String chuanHoaDuongDan(String duongDan) {

        if (duongDan == null || duongDan.isBlank()) {
            throw new IllegalArgumentException(
                    "Đường dẫn file SQL không được để trống."
            );
        }

        String duongDanHopLe = duongDan
                .trim()
                .replace("\\", "/");

        Path path = Paths.get(duongDanHopLe).normalize();

        if (path.isAbsolute()
                || path.startsWith("..")
                || duongDanHopLe.startsWith("/")) {

            throw new IllegalArgumentException(
                    "Đường dẫn SQL không hợp lệ: " + duongDan
            );
        }

        return path.toString().replace("\\", "/");
    }

    /**
     * Xóa cache để lần đọc tiếp theo lấy nội dung mới.
     */
    public void napLai() {
        cache.clear();
    }
}
