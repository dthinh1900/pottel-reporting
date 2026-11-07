<<<<<<< HEAD
package vn.pottel.reporting.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
=======

package vn.pottel.reporting.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

<<<<<<< HEAD
import vn.pottel.reporting.baocao.model.KetQuaBaoCao;
import vn.pottel.reporting.baocao.model.ThamSoBaoCao;
import vn.pottel.reporting.baocao.runner.BaoCaoRunner;
import vn.pottel.reporting.data.BangKetQua;

/**
 * API báo cáo BC-DS-01 cho React.
 *
 * Lỗi đầu vào trả HTTP 400 kèm {"message": "..."} (xem GlobalExceptionHandler).
 */
=======
import vn.pottel.reporting.baocao.model.ThamSoBaoCao;
import vn.pottel.reporting.baocao.model.KetQuaBaoCao;
import vn.pottel.reporting.baocao.runner.BaoCaoRunner;
import vn.pottel.reporting.data.BangKetQua;

>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
@RestController
@RequestMapping("/api/reports")
public class ReportDataApiController {

<<<<<<< HEAD
    private static final Set<Integer> PAGE_SIZES = Set.of(10, 20, 50, 100);
    private static final Set<String> GOI_MOI_STATUSES = Set.of("HAS", "NONE");
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int MAX_MSISDN_LENGTH = 30;
    private static final int MAX_TEXT_LENGTH = 200;

=======
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
    private final BaoCaoRunner runner;

    public ReportDataApiController(BaoCaoRunner runner) {
        this.runner = runner;
    }

    @GetMapping("/bc-ds-01")
    public Map<String, Object> layBaoCao(
            @RequestParam(defaultValue = "1B") String bc,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false) String msisdn,
            @RequestParam(required = false) String kitStatus,
            @RequestParam(required = false) String reconciliationStatus,
            @RequestParam(required = false) String goiMoiStatus,

            @RequestParam(defaultValue = "1") int page,
<<<<<<< HEAD
            @RequestParam(defaultValue = "20") int pageSize,

            // false: chỉ lấy dữ liệu bảng, bỏ qua bảng 1A và danh sách lọc
            // (React chỉ cần tải các phần này một lần).
            @RequestParam(defaultValue = "true") boolean includeMeta
    ) {
        String selectedBc = chuanHoaBaoCao(bc);

        kiemTraThamSo(
                fromDate, toDate, msisdn, kitStatus,
                reconciliationStatus, goiMoiStatus, page, pageSize
        );
=======
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        if (fromDate != null && toDate != null
                && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ngày bắt đầu phải nhỏ hơn ngày kết thúc."
            );
        }

        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Thông số phân trang không hợp lệ."
            );
        }

        String selectedBc = "1C".equalsIgnoreCase(bc) ? "1C" : "1B";
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2

        ThamSoBaoCao thamSo = new ThamSoBaoCao(
                fromDate,
                toDate,
                msisdn,
                kitStatus,
                reconciliationStatus,
                goiMoiStatus,
                page,
                pageSize
        );

        Map<String, Object> response = new LinkedHashMap<>();

<<<<<<< HEAD
        if (includeMeta) {
            // Bảng 1A: Tổng quan và danh sách lựa chọn cho bộ lọc
            response.put("summary", runner.chayTongQuan());
            response.put("kitStatuses", runner.layDanhSachTrangThaiKit());
            response.put(
                    "reconciliationStatuses",
                    runner.layDanhSachTrangThaiDoiSoat()
            );
        }

        response.put("selectedBc", selectedBc);
        response.put("currentPage", thamSo.trang());
        response.put("pageSize", thamSo.kichThuoc());
=======
        // Bảng 1A: Tổng quan
        response.put("summary", runner.chayTongQuan());

        // Danh sách lựa chọn cho bộ lọc
        response.put("kitStatuses", runner.layDanhSachTrangThaiKit());
        response.put(
                "reconciliationStatuses",
                runner.layDanhSachTrangThaiDoiSoat()
        );

        // Thông tin bộ lọc và phân trang
        response.put("selectedBc", selectedBc);
        response.put("currentPage", page);
        response.put("pageSize", pageSize);

        // Chạy báo cáo
        KetQuaBaoCao ketQua = runner.chayBcDs01(thamSo);
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2

        int totalRows;

        if ("1B".equals(selectedBc)) {
<<<<<<< HEAD
            totalRows = runner.demSoNgayBang1B(thamSo);
            kiemTraTrang(thamSo, totalRows);

            KetQuaBaoCao ketQua = runner.chayBcDs01(thamSo, Set.of("1B"));
=======
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
            BangKetQua bang = ketQua.layBang("1B");

            if (bang == null) {
                throw new IllegalStateException(
                        "Không tìm thấy bảng kết quả 1B."
                );
            }

            response.put("dailySummary", bang.dong());
            response.put("subscriberDetails", null);

<<<<<<< HEAD
        } else {
            KetQuaBaoCao ketQua =
                    runner.chayBcDs01(thamSo, Set.of("1C", "1D"));

=======
            totalRows = runner.demSoNgayBang1B(thamSo);

        } else {
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
            BangKetQua bang = ketQua.layBang("1C");
            BangKetQua bangDem = ketQua.layBang("1D");

            if (bang == null) {
                throw new IllegalStateException(
                        "Không tìm thấy bảng kết quả 1C."
                );
            }

<<<<<<< HEAD
            totalRows = layTongSoDong(bangDem);
            kiemTraTrang(thamSo, totalRows);

            response.put("dailySummary", null);
            response.put("subscriberDetails", bang.dong());
        }

        response.put("totalRows", totalRows);
        response.put("totalPages", tinhSoTrang(totalRows, thamSo.kichThuoc()));

        return response;
    }

    // ==========================================
    // HÀM HỖ TRỢ
    // ==========================================

    private String chuanHoaBaoCao(String bc) {
        if ("1B".equalsIgnoreCase(bc)) {
            return "1B";
        }

        if ("1C".equalsIgnoreCase(bc)) {
            return "1C";
        }

        throw badRequest("Loại báo cáo không hợp lệ (chỉ hỗ trợ 1B hoặc 1C).");
    }

    private void kiemTraThamSo(
            LocalDate fromDate,
            LocalDate toDate,
            String msisdn,
            String kitStatus,
            String reconciliationStatus,
            String goiMoiStatus,
            int page,
            int pageSize) {

        if (fromDate != null && toDate != null
                && fromDate.isAfter(toDate)) {
            throw badRequest("Ngày bắt đầu không được lớn hơn ngày kết thúc.");
        }

        LocalDate homNay = LocalDate.now(VN_ZONE);

        if ((fromDate != null && fromDate.isAfter(homNay))
                || (toDate != null && toDate.isAfter(homNay))) {
            throw badRequest("Ngày lọc không được vượt quá ngày hiện tại.");
        }

        if (page < 1) {
            throw badRequest("Số trang không hợp lệ.");
        }

        if (!PAGE_SIZES.contains(pageSize)) {
            throw badRequest(
                    "Số dòng mỗi trang không hợp lệ (chỉ nhận 10, 20, 50 hoặc 100)."
            );
        }

        if (msisdn != null && msisdn.trim().length() > MAX_MSISDN_LENGTH) {
            throw badRequest("MSISDN quá dài.");
        }

        if (tooLong(kitStatus) || tooLong(reconciliationStatus)) {
            throw badRequest("Giá trị bộ lọc quá dài.");
        }

        if (goiMoiStatus != null && !goiMoiStatus.isBlank()
                && !GOI_MOI_STATUSES.contains(goiMoiStatus.trim())) {
            throw badRequest(
                    "Tình trạng GOI_MOI không hợp lệ (chỉ nhận HAS hoặc NONE)."
            );
        }
    }

    private boolean tooLong(String value) {
        return value != null && value.length() > MAX_TEXT_LENGTH;
    }

    private void kiemTraTrang(ThamSoBaoCao thamSo, int totalRows) {
        if (thamSo.trang() > tinhSoTrang(totalRows, thamSo.kichThuoc())) {
            throw badRequest("Số trang không hợp lệ.");
        }
    }

    private int tinhSoTrang(int totalRows, int pageSize) {
        return Math.max(1, (int) Math.ceil((double) totalRows / pageSize));
    }

    private int layTongSoDong(BangKetQua bangDem) {
        if (bangDem == null || bangDem.dong().isEmpty()) {
            return 0;
        }

        List<Map<String, Object>> dong = bangDem.dong();
        Object value = dong.get(0).get("total_rows");

        return value == null ? 0 : ((Number) value).intValue();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
=======
            response.put("dailySummary", null);
            response.put("subscriberDetails", bang.dong());

            if (bangDem == null || bangDem.dong().isEmpty()) {
                totalRows = 0;
            } else {
                Object value = bangDem.dong().get(0).get("total_rows");
                totalRows = value == null
                        ? 0
                        : ((Number) value).intValue();
            }
        }

        int totalPages = Math.max(
                1,
                (int) Math.ceil((double) totalRows / pageSize)
        );

        response.put("totalRows", totalRows);
        response.put("totalPages", totalPages);

        return response;
    }
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
}
