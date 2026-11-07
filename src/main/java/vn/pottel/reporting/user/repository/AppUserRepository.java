
package vn.pottel.reporting.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.pottel.reporting.user.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
<<<<<<< HEAD

    // Đếm số tài khoản theo vai trò và trạng thái (dùng để bảo vệ ADMIN cuối cùng).
    long countByRoleAndEnabled(String role, boolean enabled);
=======
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
}
