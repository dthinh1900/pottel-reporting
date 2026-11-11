
package vn.pottel.reporting.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import vn.pottel.reporting.baocao.catalog.DanhMucBaoCao;
import vn.pottel.reporting.baocao.catalog.DinhNghiaBaoCao;
import vn.pottel.reporting.baocao.catalog.ThamSoDef;
import vn.pottel.reporting.baocao.model.KetQuaBaoCao;
import vn.pottel.reporting.baocao.runner.BaoCaoRunner;
import vn.pottel.reporting.data.BangKetQua;

@RestController
@RequestMapping("/api/reports")
public class ReportCatalogApiController {

    private static final Set<Integer> PAGE_SIZES =
            Set.of(10, 20, 50, 100);

    private final DanhMucBaoCao danhMucBaoCao;
    private final BaoCaoRunner baoCaoRunner;

    public ReportCatalogApiController(
            DanhMucBaoCao danhMucBaoCao,
            BaoCaoRunner baoCaoRunner
    ) {
        this.danhMucBaoCao = danhMucBaoCao;
        this.baoCaoRunner = baoCaoRunner;
    }

    // =====================================================
    // API 1: LẤY DANH SÁCH BÁO CÁO VÀ METADATA
    // GET /api/reports
    // =====================================================

    @GetMapping
    public List<Map<String, Object>> layDanhSachBaoCao(
            Authentication authentication
    ) {
        return danhMucBaoCao.layDanhSachHoatDong()
                .stream()
                .filter(baoCao -> coQuyen(authentication, baoCao))
                .map(this::chuyenThanhMap)
                .toList();
    }

    // =====================================================
    // API 2: CHẠY BÁO CÁO THEO MÃ
    // GET /api/reports/{ma}
    // =====================================================

    @GetMapping("/{ma}")
    public Map<String, Object> chayBaoCao(
            @PathVariable String ma,
            @RequestParam Map<String, String> query,
            Authentication authentication
    ) {
        DinhNghiaBaoCao dinhNghia = danhMucBaoCao.lay(ma);

        if (dinhNghia == null || !dinhNghia.kichHoat()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy báo cáo hoặc báo cáo đã bị tắt."
            );
        }

        if (!coQuyen(authentication, dinhNghia)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không có quyền xem báo cáo này."
            );
        }

        Map<String, Object> thamSo = chuanHoaThamSo(
                dinhNghia,
                query
        );

        try {
            KetQuaBaoCao ketQua =
                    baoCaoRunner.chayTheoMetadata(ma, thamSo);

            Map<String, Object> response = new LinkedHashMap<>();

            response.put("maBaoCao", ketQua.maBaoCao());
            response.put("tenBaoCao", ketQua.tenBaoCao());

            // Chỉ trả những bảng được metadata đánh dấu hiển thị.
            response.put(
                    "cacBang",
                    chuyenCacBang(ketQua, dinhNghia)
            );

            // Trả metadata thẻ tổng quan và giá trị đã tính.
            response.put(
                    "cacThe",
                    tinhCacThe(ketQua, dinhNghia)
            );

            // Trả metadata bảng và cột để frontend hiển thị.
            response.put(
                    "metadataBang",
                    chuyenMetadataBang(dinhNghia)
            );

            if (dinhNghia.phanTrang()) {
                DinhNghiaBaoCao.PhanTrangDef cauHinh =
                        dinhNghia.cauHinhPhanTrang();

                int currentPage = (Integer) thamSo.get("page");
                int pageSize = (Integer) thamSo.get("pageSize");

                BangKetQua bangDem =
                        ketQua.layBang(cauHinh.bangDem());

                int totalRows = docTongSoDong(
                        bangDem,
                        cauHinh.cotDem()
                );

                int totalPages = totalRows == 0
                        ? 0
                        : (int) Math.ceil(
                                (double) totalRows / pageSize
                        );

                response.put("currentPage", currentPage);
                response.put("pageSize", pageSize);
                response.put("totalRows", totalRows);
                response.put("totalPages", totalPages);
            }

            return response;

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage(),
                    e
            );
        }
    }

    // =====================================================
    // CHUYỂN ĐỊNH NGHĨA BÁO CÁO THÀNH JSON
    // =====================================================

    private Map<String, Object> chuyenThanhMap(
            DinhNghiaBaoCao baoCao
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("maBaoCao", baoCao.maBaoCao());
        result.put("nhom", baoCao.nhom());
        result.put("tenBaoCao", baoCao.tenBaoCao());
        result.put("moTa", baoCao.moTa());
        result.put("kichHoat", baoCao.kichHoat());
        result.put("vaiTro", baoCao.vaiTro());
        result.put("phanTrang", baoCao.phanTrang());

        result.put(
                "thamSo",
                baoCao.thamSo()
                        .stream()
                        .map(this::chuyenThamSoThanhMap)
                        .toList()
        );

        result.put(
                "metadataBang",
                chuyenMetadataBang(baoCao)
        );

        result.put(
                "cacThe",
                baoCao.cacThe()
                        .stream()
                        .map(this::chuyenTheThanhMap)
                        .toList()
        );

        if (baoCao.phanTrang()) {
            result.put(
                    "phanTrang",
                    chuyenPhanTrangThanhMap(
                            baoCao.cauHinhPhanTrang()
                    )
            );
        } else {
            result.put("phanTrang", null);
        }

        return result;
    }

    private Map<String, Object> chuyenThamSoThanhMap(
            ThamSoDef thamSo
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("ten", thamSo.ten());
        result.put("kieu", thamSo.kieu().name());
        result.put("nhan", thamSo.nhan());
        result.put("batBuoc", thamSo.batBuoc());
        result.put("giaTri", thamSo.giaTri());

        return result;
    }

    // =====================================================
    // METADATA BẢNG VÀ CỘT
    // =====================================================

    private Map<String, Object> chuyenMetadataBang(
            DinhNghiaBaoCao baoCao
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        baoCao.cacBang().forEach((maBang, bang) -> {
            Map<String, Object> thongTinBang =
                    new LinkedHashMap<>();

            thongTinBang.put("nhan", bang.nhan());
            thongTinBang.put("hienThi", bang.hienThi());
            thongTinBang.put("vaiTro", bang.vaiTro());

            Map<String, Object> cacCot = new LinkedHashMap<>();

            bang.cacCot().forEach((maCot, cot) -> {
                Map<String, Object> thongTinCot =
                        new LinkedHashMap<>();

                thongTinCot.put("nhan", cot.nhan());
                thongTinCot.put("hienThi", cot.hienThi());
                thongTinCot.put("dinhDang", cot.dinhDang());

                cacCot.put(maCot, thongTinCot);
            });

            thongTinBang.put("cacCot", cacCot);
            result.put(maBang, thongTinBang);
        });

        return result;
    }

    // =====================================================
    // METADATA THẺ TỔNG QUAN
    // =====================================================

    private Map<String, Object> chuyenTheThanhMap(
            DinhNghiaBaoCao.TheDef the
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("ma", the.ma());
        result.put("nhan", the.nhan());
        result.put("bang", the.bang());
        result.put("cot", the.cot());
        result.put("phepTinh", the.phepTinh());
        result.put("dinhDang", the.dinhDang());

        return result;
    }

    private List<Map<String, Object>> tinhCacThe(
            KetQuaBaoCao ketQua,
            DinhNghiaBaoCao baoCao
    ) {
        return baoCao.cacThe()
                .stream()
                .map(the -> {
                    Map<String, Object> result =
                            chuyenTheThanhMap(the);

                    BangKetQua bang = ketQua.layBang(the.bang());

                    Object giaTri = tinhGiaTriThe(
                            bang,
                            the.cot(),
                            the.phepTinh()
                    );

                    result.put("giaTri", giaTri);

                    return result;
                })
                .toList();
    }

    private Object tinhGiaTriThe(
            BangKetQua bang,
            String tenCot,
            String phepTinh
    ) {
        if (bang == null || bang.dong().isEmpty()) {
            return "COUNT".equals(phepTinh) ? 0 : null;
        }

        return switch (phepTinh) {
            case "FIRST" -> bang.dong().get(0).get(tenCot);

            case "SUM" -> tinhTong(bang, tenCot);

            case "COUNT" -> demGiaTri(bang, tenCot);

            default -> throw new IllegalStateException(
                    "Phép tính thẻ tổng quan không được hỗ trợ: "
                            + phepTinh
            );
        };
    }

    private Number tinhTong(
            BangKetQua bang,
            String tenCot
    ) {
        java.math.BigDecimal tong = java.math.BigDecimal.ZERO;

        for (Map<String, Object> dong : bang.dong()) {
            Object giaTri = dong.get(tenCot);

            if (giaTri instanceof Number number) {
                tong = tong.add(
                        new java.math.BigDecimal(number.toString())
                );
            }
        }

        return tong;
    }

    private int demGiaTri(
            BangKetQua bang,
            String tenCot
    ) {
        int soLuong = 0;

        for (Map<String, Object> dong : bang.dong()) {
            if (dong.get(tenCot) != null) {
                soLuong++;
            }
        }

        return soLuong;
    }

    // =====================================================
    // METADATA PHÂN TRANG
    // =====================================================

    private Map<String, Object> chuyenPhanTrangThanhMap(
            DinhNghiaBaoCao.PhanTrangDef phanTrang
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("bangDuLieu", phanTrang.bangDuLieu());
        result.put("bangDem", phanTrang.bangDem());
        result.put("cotDem", phanTrang.cotDem());
        result.put(
                "kichThuocMacDinh",
                phanTrang.kichThuocMacDinh()
        );
        result.put("kichThuocChoPhep", PAGE_SIZES);

        return result;
    }

    private int docTongSoDong(
            BangKetQua bangDem,
            String cotDem
    ) {
        if (bangDem == null || bangDem.dong().isEmpty()) {
            return 0;
        }

        Object total = bangDem.dong()
                .get(0)
                .get(cotDem);

        if (total == null) {
            return 0;
        }

        if (total instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(total.toString());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Giá trị cột đếm " + cotDem
                            + " không phải số nguyên.",
                    e
            );
        }
    }

    // =====================================================
    // CHUẨN HÓA VÀ KIỂM TRA THAM SỐ
    // =====================================================

    private Map<String, Object> chuanHoaThamSo(
            DinhNghiaBaoCao baoCao,
            Map<String, String> query
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        Set<String> thamSoChoPhep = new HashSet<>();

        for (ThamSoDef dinhNghia : baoCao.thamSo()) {
            thamSoChoPhep.add(dinhNghia.ten());

            String giaTri = query.get(dinhNghia.ten());

            if (giaTri == null || giaTri.isBlank()) {
                if (dinhNghia.batBuoc()) {
                    throw badRequest(
                            "Tham số " + dinhNghia.nhan()
                                    + " không được để trống."
                    );
                }

                result.put(dinhNghia.ten(), null);
                continue;
            }

            result.put(
                    dinhNghia.ten(),
                    chuyenKieu(dinhNghia, giaTri.trim())
            );
        }

        thamSoChoPhep.add("page");
        thamSoChoPhep.add("pageSize");

        for (String ten : query.keySet()) {
            if (!thamSoChoPhep.contains(ten)) {
                throw badRequest(
                        "Tham số không được hỗ trợ: " + ten
                );
            }
        }

        if (baoCao.phanTrang()) {
            int page = docSoNguyen(
                    query.get("page"),
                    1,
                    "page"
            );

            int pageSizeMacDinh =
                    baoCao.cauHinhPhanTrang().kichThuocMacDinh();

            int pageSize = docSoNguyen(
                    query.get("pageSize"),
                    pageSizeMacDinh,
                    "pageSize"
            );

            if (page < 1) {
                throw badRequest(
                        "Page phải lớn hơn hoặc bằng 1."
                );
            }

            if (!PAGE_SIZES.contains(pageSize)) {
                throw badRequest(
                        "Page size chỉ nhận 10, 20, 50 hoặc 100."
                );
            }

            result.put("page", page);
            result.put("pageSize", pageSize);
        } else {
            if (query.containsKey("page")
                    || query.containsKey("pageSize")) {
                throw badRequest(
                        "Báo cáo này không hỗ trợ phân trang."
                );
            }
        }

        return result;
    }

    // =====================================================
    // CHUYỂN KIỂU DỮ LIỆU THEO METADATA
    // =====================================================

    private Object chuyenKieu(
            ThamSoDef dinhNghia,
            String giaTri
    ) {
        try {
            return switch (dinhNghia.kieu()) {

                case NGAY -> LocalDate.parse(giaTri);

                case CHUOI -> {
                    if (giaTri.length() > 200) {
                        throw badRequest(
                                dinhNghia.nhan() + " quá dài."
                        );
                    }

                    yield giaTri;
                }

                case SO -> Integer.parseInt(giaTri);

                case CHON -> {
                    if (!dinhNghia.giaTri().contains(giaTri)) {
                        throw badRequest(
                                "Giá trị " + dinhNghia.nhan()
                                        + " không hợp lệ."
                        );
                    }

                    yield giaTri;
                }
            };

        } catch (DateTimeParseException e) {
            throw badRequest(
                    dinhNghia.nhan()
                            + " phải có định dạng yyyy-MM-dd."
            );

        } catch (NumberFormatException e) {
            throw badRequest(
                    dinhNghia.nhan() + " phải là số nguyên."
            );
        }
    }

    private int docSoNguyen(
            String giaTri,
            int macDinh,
            String ten
    ) {
        if (giaTri == null || giaTri.isBlank()) {
            return macDinh;
        }

        try {
            return Integer.parseInt(giaTri);
        } catch (NumberFormatException e) {
            throw badRequest(
                    "Tham số " + ten + " phải là số nguyên."
            );
        }
    }

    // =====================================================
    // CHUYỂN KẾT QUẢ CÁC BẢNG THÀNH JSON
    // =====================================================

    private Map<String, Object> chuyenCacBang(
            KetQuaBaoCao ketQua,
            DinhNghiaBaoCao baoCao
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        ketQua.cacBang().forEach((maBang, bang) -> {
            DinhNghiaBaoCao.BangDef metadata =
                    baoCao.cacBang().get(maBang);

            // Nếu metadata có khai báo bảng thì chỉ hiện bảng
            // được đánh dấu hienThi = true.
            if (metadata != null && !metadata.hienThi()) {
                return;
            }

            Map<String, Object> duLieuBang =
                    new LinkedHashMap<>();

            duLieuBang.put("cot", bang.cot());
            duLieuBang.put("dong", bang.dong());
            duLieuBang.put("soLuongDong", bang.soLuongDong());

            result.put(maBang, duLieuBang);
        });

        return result;
    }

    // =====================================================
    // KIỂM TRA QUYỀN
    // =====================================================

    private boolean coQuyen(
            Authentication authentication,
            DinhNghiaBaoCao baoCao
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            return false;
        }

        if (baoCao.vaiTro() == null
                || baoCao.vaiTro().isBlank()) {
            return true;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals(
                                baoCao.vaiTro()
                        )
                );
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }
}
