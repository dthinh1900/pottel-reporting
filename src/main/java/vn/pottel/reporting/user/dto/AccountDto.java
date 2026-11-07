package vn.pottel.reporting.user.dto;

import vn.pottel.reporting.user.entity.AppUser;

/**
 * Thông tin tài khoản trả về cho React.
 * Cố ý KHÔNG có passwordHash.
 */
public record AccountDto(
        Long id,
        String tenDangNhap,
        String vaiTro,
        boolean hoatDong,
        String ngayTao
) {
    public static AccountDto from(AppUser user) {
        return new AccountDto(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt() == null
                        ? null
                        : user.getCreatedAt().toString()
        );
    }
}
