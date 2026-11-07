package vn.pottel.reporting.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Chuyển mọi lỗi của REST API thành JSON dạng {"message": "..."}
 * để React hiển thị được thông báo rõ ràng.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> xuLyLoi(Exception e)
            throws Exception {

        // Lỗi xác thực/phân quyền do Spring Security xử lý.
        if (e instanceof AccessDeniedException
                || e instanceof AuthenticationException) {
            throw e;
        }

        if (e instanceof ResponseStatusException rse) {
            String reason = rse.getReason();

            return phanHoi(
                    HttpStatus.valueOf(rse.getStatusCode().value()),
                    reason != null ? reason : "Yêu cầu không hợp lệ."
            );
        }

        if (e instanceof MethodArgumentTypeMismatchException m) {
            return phanHoi(
                    HttpStatus.BAD_REQUEST,
                    "Giá trị của tham số '" + m.getName()
                            + "' không hợp lệ."
            );
        }

        if (e instanceof MissingServletRequestParameterException m) {
            return phanHoi(
                    HttpStatus.BAD_REQUEST,
                    "Thiếu tham số '" + m.getParameterName() + "'."
            );
        }

        if (e instanceof HttpMessageNotReadableException) {
            return phanHoi(
                    HttpStatus.BAD_REQUEST,
                    "Dữ liệu gửi lên không hợp lệ."
            );
        }

        // Các lỗi chuẩn của Spring MVC (404, 405, ...).
        if (e instanceof ErrorResponse er) {
            return phanHoi(
                    HttpStatus.valueOf(er.getStatusCode().value()),
                    "Yêu cầu không hợp lệ."
            );
        }

        log.error("Lỗi không mong muốn khi xử lý API", e);

        return phanHoi(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Hệ thống gặp lỗi. Vui lòng thử lại sau."
        );
    }

    private ResponseEntity<Map<String, String>> phanHoi(
            HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
