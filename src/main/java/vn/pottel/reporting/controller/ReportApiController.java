package vn.pottel.reporting.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReportApiController {

    @GetMapping("/me")
    public Map<String, Object> thongTinTaiKhoan(
            Authentication authentication) {

        return Map.of(
                "tenDangNhap", authentication.getName(),
                "daDangNhap", authentication.isAuthenticated(),
                "vaiTro", authentication.getAuthorities()
                        .stream()
                        .map(authority -> authority.getAuthority())
                        .toList()
        );
    }
}