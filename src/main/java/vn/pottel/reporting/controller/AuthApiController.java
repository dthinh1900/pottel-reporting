package vn.pottel.reporting.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.pottel.reporting.security.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthApiController(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/dang-nhap")
    public ResponseEntity<?> dangNhap(
            @RequestBody LoginRequest request) {

        if (request.tenDangNhap() == null
                || request.tenDangNhap().isBlank()
                || request.matKhau() == null
                || request.matKhau().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Vui lòng nhập tên đăng nhập và mật khẩu.")
            );
        }

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.tenDangNhap(),
                                    request.matKhau()
                            )
                    );

            UserDetails userDetails =
                    (UserDetails) authentication.getPrincipal();

            String token = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(
                    Map.of(
                            "token", token,
                            "loai", "Bearer",
                            "hetHan", 86400,
                            "nguoiDung", Map.of(
                                    "tenDangNhap", userDetails.getUsername(),
                                    "vaiTro", userDetails.getAuthorities()
                                            .stream()
                                            .map(authority -> authority.getAuthority())
                                            .toList()
                            )
                    )
            );

        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Tên đăng nhập hoặc mật khẩu không chính xác.")
            );
        }
    }

    public record LoginRequest(
            String tenDangNhap,
            String matKhau
    ) {
    }
}