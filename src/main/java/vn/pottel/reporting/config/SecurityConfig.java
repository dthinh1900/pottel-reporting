package vn.pottel.reporting.config;

import java.util.List;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;

import vn.pottel.reporting.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // Cho phép React gọi API từ localhost:5173
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // API dùng JWT nên không cần CSRF token.
            // Các trang Thymeleaf vẫn giữ cơ chế CSRF mặc định.
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )

            .authorizeHttpRequests(auth -> auth

                // Các trang và tài nguyên công khai
                .requestMatchers(
                    "/login",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                // API đăng nhập không cần JWT
                .requestMatchers("/api/auth/**").permitAll()

                // Chỉ ADMIN được truy cập khu vực quản trị
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Các API còn lại yêu cầu đăng nhập
                .requestMatchers("/api/**").authenticated()

                // Các trang còn lại yêu cầu đăng nhập
                .anyRequest().authenticated()
            )

            // Giữ đăng nhập bằng biểu mẫu cho Thymeleaf
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
            )

            // Giữ đăng xuất cũ
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // API không đăng nhập sẽ nhận HTTP 401 thay vì chuyển hướng
            .exceptionHandling(exception -> exception
                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) ->
                        response.sendError(HttpStatus.UNAUTHORIZED.value()),
                    request -> request.getServletPath().startsWith("/api/")
                )
            )

            // Thêm bộ lọc JWT trước bộ lọc đăng nhập mặc định
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
            List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
            List.of("Authorization", "Content-Type")
        );

        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}