package org.mephi.finance.manager.rest.handler;

import lombok.extern.slf4j.Slf4j;
import org.mephi.finance.manager.exception.ActionForbiddenException;
import org.mephi.finance.manager.exception.InsufficientFundsException;
import org.mephi.finance.manager.exception.ResourceNotFoundException;
import org.mephi.finance.manager.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Неизвестная ошибка: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Произошла непредвиденная ошибка.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String errorMessage = "Ошибка валидации: " + errors;

        ErrorResponse errorResponse = new ErrorResponse()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(errorMessage);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Ресурс не найден: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Недопустимое значение: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage());

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ActionForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleActionForbidden(ActionForbiddenException ex) {
        log.warn("Действие запрещено: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse()
                .timestamp(Instant.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Action forbidden")
                .message(ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        log.warn("Недостаточно средств: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Insufficient Funds")
                .message(ex.getMessage());

        return ResponseEntity.badRequest().body(errorResponse);
    }
}
