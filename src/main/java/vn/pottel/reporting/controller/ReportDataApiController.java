
package vn.pottel.reporting.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import vn.pottel.reporting.baocao.model.ThamSoBaoCao;
import vn.pottel.reporting.baocao.model.KetQuaBaoCao;
import vn.pottel.reporting.baocao.runner.BaoCaoRunner;
import vn.pottel.reporting.data.BangKetQua;

@RestController
@RequestMapping("/api/reports")
public class ReportDataApiController {

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

        int totalRows;

        if ("1B".equals(selectedBc)) {
            BangKetQua bang = ketQua.layBang("1B");

            if (bang == null) {
                throw new IllegalStateException(
                        "Không tìm thấy bảng kết quả 1B."
                );
            }

            response.put("dailySummary", bang.dong());
            response.put("subscriberDetails", null);

            totalRows = runner.demSoNgayBang1B(thamSo);

        } else {
            BangKetQua bang = ketQua.layBang("1C");
            BangKetQua bangDem = ketQua.layBang("1D");

            if (bang == null) {
                throw new IllegalStateException(
                        "Không tìm thấy bảng kết quả 1C."
                );
            }

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
}
