package vn.pottel.reporting.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import vn.pottel.reporting.user.dto.AccountDto;
import vn.pottel.reporting.user.service.AccountService;

/**
 * API quản lý tài khoản. Chỉ ADMIN được gọi
 * (cấu hình trong SecurityConfig: /api/admin/** → ROLE_ADMIN).
 */
@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountApiController {

    private final AccountService accountService;

    public AdminAccountApiController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Danh sách tài khoản
    @GetMapping
    public List<AccountDto> layDanhSach() {
        return accountService.layDanhSach();
    }

    // Tạo tài khoản mới (vai trò mặc định: USER)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDto taoTaiKhoan(@RequestBody TaoTaiKhoanRequest request) {
        return accountService.taoTaiKhoan(
                request.tenDangNhap(),
                request.matKhau()
        );
    }

    // Khóa hoặc mở khóa tài khoản
    @PostMapping("/{id}/toggle")
    public AccountDto doiTrangThai(
            @PathVariable Long id,
            Authentication authentication) {
        return accountService.doiTrangThai(id, authentication.getName());
    }

    public record TaoTaiKhoanRequest(
            String tenDangNhap,
            String matKhau
    ) {
    }
}
