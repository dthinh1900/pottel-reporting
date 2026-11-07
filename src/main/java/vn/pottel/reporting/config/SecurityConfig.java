package vn.pottel.reporting.config;

<<<<<<< HEAD
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
=======
import java.util.List;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
<<<<<<< HEAD
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import vn.pottel.reporting.security.JwtAuthenticationFilter;

/**
 * Cấu hình bảo mật: Backend chỉ cung cấp REST API, xác thực bằng JWT.
 * Không còn đăng nhập bằng biểu mẫu, phiên (session) hay CSRF của Thymeleaf.
 */
=======
import org.springframework.security.web.AuthenticationEntryPoint;

import vn.pottel.reporting.security.JwtAuthenticationFilter;

>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
<<<<<<< HEAD
    private final List<String> allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            // Danh sách địa chỉ React được phép gọi API, cách nhau bằng dấu phẩy.
            @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
            String allowedOrigins) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
=======

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
<<<<<<< HEAD
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // API dùng JWT trong header Authorization, không dùng cookie
            // nên không cần CSRF và không cần session.
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/error").permitAll()
=======
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
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2

                // API đăng nhập không cần JWT
                .requestMatchers("/api/auth/**").permitAll()

<<<<<<< HEAD
                // Chỉ ADMIN được dùng API quản trị
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Mọi API và đường dẫn còn lại yêu cầu JWT hợp lệ
                .anyRequest().authenticated()
            )

            // Trả JSON {"message": "..."} thay vì trang lỗi hoặc chuyển hướng
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, ex) ->
                    ghiLoi(response, HttpStatus.UNAUTHORIZED,
                        "Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn.")
                )
                .accessDeniedHandler((request, response, ex) ->
                    ghiLoi(response, HttpStatus.FORBIDDEN,
                        "Bạn không có quyền thực hiện thao tác này.")
                )
            )

=======
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
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

<<<<<<< HEAD
    private static void ghiLoi(
            HttpServletResponse response,
            HttpStatus status,
            String message) throws java.io.IOException {

        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

=======
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
<<<<<<< HEAD

=======
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
<<<<<<< HEAD

=======
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

<<<<<<< HEAD
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );
        configuration.setAllowedHeaders(
            List.of("Authorization", "Content-Type")
        );
=======
        configuration.setAllowedOrigins(
            List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
            List.of("Authorization", "Content-Type")
        );

>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
