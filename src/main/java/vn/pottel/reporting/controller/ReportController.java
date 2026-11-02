package vn.pottel.reporting.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.pottel.reporting.model.ThamSoBaoCao;
import vn.pottel.reporting.runner.BaoCaoRunner;

@Controller
@RequestMapping("/reports")
public class ReportController {

        private final BaoCaoRunner runner;

        public ReportController(BaoCaoRunner runner) {
                this.runner = runner;
        }

        // ==========================================
        // HIỂN THỊ TOÀN BỘ TRANG BC-DS-01
        // ==========================================

        @GetMapping("/bc-ds-01")
        public String hienThiBaoCao(
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
                @RequestParam(defaultValue = "20") int pageSize,

                Model model
        ) {
                String selectedBc = chuanHoaBaoCao(bc);

                model.addAttribute("currentPage", "report");

                ThamSoBaoCao thamSo = taoThamSo(
                        fromDate,
                        toDate,
                        msisdn,
                        kitStatus,
                        reconciliationStatus,
                        goiMoiStatus,
                        page,
                        pageSize
                );

                // Bảng 1A
                model.addAttribute("summary", runner.chayTongQuan());

                // Thông tin bộ lọc
                themThongTinBoLoc(model, selectedBc, thamSo);

                // Kiểm tra khoảng ngày trước khi truy vấn
                if (!khoangNgayHopLe(fromDate, toDate)) {
                model.addAttribute(
                        "dateValidationError",
                        "Ngày bắt đầu phải nhỏ hơn ngày kết thúc."
                );

                themThongTinTrangRong(model, thamSo);
                } else {
                napDuLieuBang(model, selectedBc, thamSo);
                }

                return "reports/bc-ds-01";
        }

        // ==========================================
        // ENDPOINT AJAX - CHỈ TRẢ VỀ PHẦN BẢNG
        // ==========================================

        @GetMapping("/bc-ds-01/tables")
        public String taiBangBangAjax(
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
                @RequestParam(defaultValue = "20") int pageSize,

                Model model
        ) {
                String selectedBc = chuanHoaBaoCao(bc);

                ThamSoBaoCao thamSo = taoThamSo(
                        fromDate,
                        toDate,
                        msisdn,
                        kitStatus,
                        reconciliationStatus,
                        goiMoiStatus,
                        page,
                        pageSize
                );

                if (!khoangNgayHopLe(fromDate, toDate)) {
                model.addAttribute(
                        "dateValidationError",
                        "Ngày bắt đầu phải nhỏ hơn ngày kết thúc."
                );

                themThongTinTrangRong(model, thamSo);
                } else {
                napDuLieuBang(model, selectedBc, thamSo);
                }

                return "fragments/bc-ds-01-tables :: tables";
        }

        // ==========================================
        // CHUẨN HÓA MÃ BÁO CÁO
        // ==========================================

        private String chuanHoaBaoCao(String bc) {
                return "1C".equalsIgnoreCase(bc) ? "1C" : "1B";
        }

        // ==========================================
        // KIỂM TRA KHOẢNG NGÀY
        // ==========================================

        private boolean khoangNgayHopLe(
                LocalDate fromDate,
                LocalDate toDate) {

        if (fromDate == null || toDate == null) {
                return true;
        }

        return !fromDate.isAfter(toDate);
        }

        // ==========================================
        // TẠO THAM SỐ BÁO CÁO
        // ==========================================

        private ThamSoBaoCao taoThamSo(
                LocalDate fromDate,
                LocalDate toDate,
                String msisdn,
                String kitStatus,
                String reconciliationStatus,
                String goiMoiStatus,
                int page,
                int pageSize
        ) {
                return new ThamSoBaoCao(
                        fromDate,
                        toDate,
                        msisdn,
                        kitStatus,
                        reconciliationStatus,
                        goiMoiStatus,
                        page,
                        pageSize
                );
        }

        // ==========================================
        // THÔNG TIN BỘ LỌC
        // ==========================================

        private void themThongTinBoLoc(
                Model model,
                String selectedBc,
                ThamSoBaoCao thamSo
        ) {
                model.addAttribute("selectedBc", selectedBc);
                model.addAttribute("fromDate", thamSo.tuNgay());
                model.addAttribute("toDate", thamSo.denNgay());
                model.addAttribute("msisdn", thamSo.msisdn());
                model.addAttribute("kitStatus", thamSo.trangThaiKit());

                model.addAttribute(
                        "reconciliationStatus",
                        thamSo.trangThaiDoiSoat()
                );

                model.addAttribute(
                        "goiMoiStatus",
                        thamSo.tinhTrangGoiMoi()
                );

                model.addAttribute(
                        "kitStatuses",
                        runner.layDanhSachTrangThaiKit()
                );

                model.addAttribute(
                        "reconciliationStatuses",
                        runner.layDanhSachTrangThaiDoiSoat()
                );
        }

        // ==========================================
        // HIỂN THỊ THÔNG TIN PHÂN TRANG KHI CÓ LỖI
        // ==========================================

        private void themThongTinTrangRong(
                Model model,
                ThamSoBaoCao thamSo
        ) {
                model.addAttribute("dailySummary", List.of());
                model.addAttribute("subscriberDetails", List.of());
                model.addAttribute("currentPage", thamSo.trang());
                model.addAttribute("pageSize", thamSo.kichThuoc());
                model.addAttribute("totalRows", 0);
                model.addAttribute("totalPages", 1);
        }

        // ==========================================
        // NẠP DỮ LIỆU BẢNG VÀ PHÂN TRANG
        // ==========================================

        private void napDuLieuBang(
                Model model,
                String selectedBc,
                ThamSoBaoCao thamSo
        ) {
                int totalRows;

                model.addAttribute("selectedBc", selectedBc);

                if ("1B".equals(selectedBc)) {
                List<Map<String, Object>> dailySummary =
                        runner.chayBang1B(thamSo);

                totalRows = runner.demSoNgayBang1B(thamSo);

                model.addAttribute("dailySummary", dailySummary);

                } else {
                List<Map<String, Object>> subscriberDetails =
                        runner.chayBang1C(thamSo);

                totalRows = runner.demDongBang1C(thamSo);

                model.addAttribute("subscriberDetails", subscriberDetails);
                }

                int totalPages = (int) Math.ceil(
                        (double) totalRows / thamSo.kichThuoc()
                );

                if (totalPages < 1) {
                totalPages = 1;
                }

                model.addAttribute("currentPage", thamSo.trang());
                model.addAttribute("pageSize", thamSo.kichThuoc());
                model.addAttribute("totalRows", totalRows);
                model.addAttribute("totalPages", totalPages);
        }
}