package vn.pottel.reporting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;

import vn.pottel.reporting.user.entity.AppUser;
import vn.pottel.reporting.user.repository.AppUserRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {

        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Hiển thị danh sách tài khoản
    @GetMapping("/accounts")
    public String accounts(
            Model model,
            Authentication authentication) {

        model.addAttribute("accounts", appUserRepository.findAll());
        model.addAttribute("currentUsername", authentication.getName());

        // Xác định trang hiện tại để navbar ẩn nút tương ứng
        model.addAttribute("currentPage", "admin");

        return "admin/accounts";
    }

    // Tạo tài khoản mới
    @PostMapping("/accounts/create")
    public String createAccount(
            @RequestParam String username,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {

        username = username.trim();

        if (username.isBlank() || password.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Tên đăng nhập và mật khẩu không được để trống."
            );
            return "redirect:/admin/accounts";
        }

        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Mật khẩu phải có ít nhất 8 ký tự."
            );
            return "redirect:/admin/accounts";
        }

        

        if (appUserRepository.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Tên đăng nhập đã tồn tại."
            );
            return "redirect:/admin/accounts";
        }

        AppUser account = new AppUser(
        username,
        passwordEncoder.encode(password),
        "USER",
        true
        );      

        appUserRepository.save(account);

        redirectAttributes.addFlashAttribute(
                "success",
                "Tạo tài khoản thành công."
        );

        return "redirect:/admin/accounts";
    }

    // Khóa hoặc mở khóa tài khoản
    @PostMapping("/accounts/toggle")
    public String toggleAccount(
            @RequestParam Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        AppUser account = appUserRepository.findById(id).orElse(null);

        if (account == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Không tìm thấy tài khoản."
            );
            return "redirect:/admin/accounts";
        }

        // Không cho tự khóa tài khoản đang đăng nhập
        if (account.getUsername().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Bạn không thể khóa tài khoản đang đăng nhập."
            );
            return "redirect:/admin/accounts";
        }

        // Không cho khóa ADMIN cuối cùng đang hoạt động
        if (account.getRole().equals("ADMIN") && account.isEnabled()) {

            long activeAdmins = appUserRepository.findAll().stream()
                    .filter(user ->
                            user.getRole().equals("ADMIN")
                                    && user.isEnabled())
                    .count();

            if (activeAdmins <= 1) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Không thể khóa ADMIN cuối cùng đang hoạt động."
                );
                return "redirect:/admin/accounts";
            }
        }

        account.setEnabled(!account.isEnabled());
        appUserRepository.save(account);

        redirectAttributes.addFlashAttribute(
                "success",
                account.isEnabled()
                        ? "Đã mở khóa tài khoản."
                        : "Đã khóa tài khoản."
        );

        return "redirect:/admin/accounts";
    }
}