
package vn.pottel.reporting.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.pottel.reporting.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
}
