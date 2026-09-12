package io.lin.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionController {

    private final MessageSource messageSource;

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleException(CustomException e, HttpServletRequest request) {

        String message = messageSource.getMessage(
                e.getMessageCode(),
                e.getArgs(),
                e.getMessageCode(),
                LocaleContextHolder.getLocale()
        );

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now(ZoneOffset.UTC));
        errorResponse.put("status", e.getStatus().value());
        errorResponse.put("error", e.getStatus().name());
        errorResponse.put("message", message);
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity.status(e.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fields = new HashMap<>();
        Locale locale = LocaleContextHolder.getLocale();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {

            String msg = messageSource.getMessage(
                    error.getDefaultMessage(),
                    error.getArguments(),
                    error.getDefaultMessage(),
                    locale
            );

            fields.put(error.getField(), msg);
        }

        String message = messageSource.getMessage(
                "valid.failed",
                null,
                "Validation failed",
                LocaleContextHolder.getLocale()
        );

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now(ZoneOffset.UTC));
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.name());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        body.put("fields", fields);

        return ResponseEntity.badRequest().body(body);
    }
}
