package com.prasad.banking.exception;

import com.prasad.banking.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler — intercepts ALL exceptions thrown anywhere
 * in the application and converts them into consistent JSON error responses.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * It applies to ALL @RestController classes across the application.
 *
 * Why this matters:
 *   Without this, Spring would return its default HTML error page or
 *   an inconsistent JSON structure. This ensures every error returns
 *   the same ErrorResponse format — predictable for clients.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -----------------------------------------------------------------------
    // 404 NOT FOUND
    // -----------------------------------------------------------------------

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(
            CustomerNotFoundException ex, HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.NOT_FOUND, "Customer Not Found",
                ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(
            AccountNotFoundException ex, HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.NOT_FOUND, "Account Not Found",
                ex.getMessage(), request.getRequestURI());
    }

    // -----------------------------------------------------------------------
    // 409 CONFLICT
    // -----------------------------------------------------------------------

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.CONFLICT, "Duplicate Resource",
                ex.getMessage(), request.getRequestURI());
    }

    // -----------------------------------------------------------------------
    // 400 BAD REQUEST — Business rule violations
    // -----------------------------------------------------------------------

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(
            InsufficientBalanceException ex, HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Insufficient Balance",
                ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransaction(
            InvalidTransactionException ex, HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Transaction",
                ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotActive(
            AccountNotActiveException ex, HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Account Not Active",
                ex.getMessage(), request.getRequestURI());
    }

    // -----------------------------------------------------------------------
    // 400 BAD REQUEST — Jakarta Bean Validation failures
    // -----------------------------------------------------------------------

    /**
     * Handles @Valid / @Validated annotation failures.
     * When a request body fails validation (e.g., @NotBlank, @Email),
     * Spring throws MethodArgumentNotValidException.
     *
     * We collect ALL field errors and return them in the validationErrors map.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        // Collect each field error into a Map<fieldName, errorMessage>
        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request contains invalid fields. Check 'validationErrors' for details.")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // -----------------------------------------------------------------------
    // 500 INTERNAL SERVER ERROR — Catch-all for unexpected exceptions
    // -----------------------------------------------------------------------

    /**
     * Last-resort handler. Catches any exception not handled above.
     * Returns a generic 500 response without exposing internal details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI());
    }

    // -----------------------------------------------------------------------
    // Helper method — builds the standard ErrorResponse
    // -----------------------------------------------------------------------

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status, String error, String message, String path) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}
