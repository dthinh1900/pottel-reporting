
package vn.pottel.reporting.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import vn.pottel.reporting.user.entity.AppUser;
import vn.pottel.reporting.user.repository.AppUserRepository;

@Configuration
public class AdminAccountInitializer {

    @Bean
    public CommandLineRunner createAdminAccount(
            AppUserRepository repository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            String username = System.getenv("APP_ADMIN_USERNAME");
            String password = System.getenv("APP_ADMIN_PASSWORD");

            if (username == null || username.isBlank()
                    || password == null || password.isBlank()) {
                return;
            }

            if (repository.findByUsername(username).isPresent()) {
                System.out.println("Tai khoan ADMIN da ton tai.");
                return;
            }

            AppUser admin = new AppUser(
                username,
                passwordEncoder.encode(password),
                "ADMIN",
                true
            );

            repository.save(admin);

            System.out.println("Da tao tai khoan ADMIN thanh cong.");
        };
    }
}
