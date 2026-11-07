package vn.pottel.reporting.user.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vn.pottel.reporting.user.dto.AccountDto;
import vn.pottel.reporting.user.entity.AppUser;
import vn.pottel.reporting.user.repository.AppUserRepository;

/**
 * Nghiệp vụ quản lý tài khoản (chuyển từ AdminController cũ của Thymeleaf,
 * giữ nguyên các quy tắc và thông báo).
 */
@Service
public class AccountService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_USERNAME_LENGTH = 100;

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AccountDto> layDanhSach() {
        return appUserRepository.findAll(Sort.by("id"))
                .stream()
                .map(AccountDto::from)
                .toList();
    }

    @Transactional
    public AccountDto taoTaiKhoan(String tenDangNhap, String matKhau) {

        String username = tenDangNhap == null ? "" : tenDangNhap.trim();
        String password = matKhau == null ? "" : matKhau;

        if (username.isBlank() || password.isBlank()) {
            throw loi(HttpStatus.BAD_REQUEST,
                    "Tên đăng nhập và mật khẩu không được để trống.");
        }

        if (username.length() > MAX_USERNAME_LENGTH) {
            throw loi(HttpStatus.BAD_REQUEST,
                    "Tên đăng nhập tối đa " + MAX_USERNAME_LENGTH + " ký tự.");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw loi(HttpStatus.BAD_REQUEST,
                    "Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH
                            + " ký tự.");
        }

        if (appUserRepository.findByUsername(username).isPresent()) {
            throw loi(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại.");
        }

        // Tài khoản mới luôn có vai trò USER.
        AppUser account = new AppUser(
                username,
                passwordEncoder.encode(password),
                ROLE_USER,
                true
        );

        try {
            return AccountDto.from(appUserRepository.save(account));
        } catch (DataIntegrityViolationException e) {
            // Hai người cùng tạo một tên đăng nhập cùng lúc.
            throw loi(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại.");
        }
    }

    @Transactional
    public AccountDto doiTrangThai(Long id, String tenNguoiDangDangNhap) {

        AppUser account = appUserRepository.findById(id)
                .orElseThrow(() -> loi(HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài khoản."));

        // Không cho tự khóa tài khoản đang đăng nhập.
        if (account.getUsername().equals(tenNguoiDangDangNhap)) {
            throw loi(HttpStatus.BAD_REQUEST,
                    "Bạn không thể khóa tài khoản đang đăng nhập.");
        }

        // Không cho khóa ADMIN cuối cùng đang hoạt động.
        if (ROLE_ADMIN.equals(account.getRole()) && account.isEnabled()) {
            long adminDangHoatDong =
                    appUserRepository.countByRoleAndEnabled(ROLE_ADMIN, true);

            if (adminDangHoatDong <= 1) {
                throw loi(HttpStatus.BAD_REQUEST,
                        "Không thể khóa ADMIN cuối cùng đang hoạt động.");
            }
        }

        account.setEnabled(!account.isEnabled());

        return AccountDto.from(appUserRepository.save(account));
    }

    private ResponseStatusException loi(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }
}
