package com.pokevault.common.error;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.pokevault.card.CardNotFoundException;
import com.pokevault.card.DuplicateCardException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		for (FieldError error : exception.getBindingResult().getFieldErrors()) {
			fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed",
				request.getRequestURI(), fieldErrors);
	}

	@ExceptionHandler({HandlerMethodValidationException.class, MethodArgumentTypeMismatchException.class})
	ResponseEntity<ApiError> handleInvalidRequest(Exception exception, HttpServletRequest request) {
		String message = "Request contains an invalid value";
		if (exception instanceof HandlerMethodValidationException validationException) {
			message = validationException.getAllErrors().stream()
					.map(MessageSourceResolvable::getDefaultMessage)
					.findFirst()
					.orElse(message);
		}
		return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message, request.getRequestURI(), Map.of());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException exception,
			HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Request body is missing or malformed",
				request.getRequestURI(), Map.of());
	}

	@ExceptionHandler(CardNotFoundException.class)
	ResponseEntity<ApiError> handleNotFound(CardNotFoundException exception, HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, "CARD_NOT_FOUND", exception.getMessage(), request.getRequestURI(), Map.of());
	}

	@ExceptionHandler(DuplicateCardException.class)
	ResponseEntity<ApiError> handleDuplicate(DuplicateCardException exception, HttpServletRequest request) {
		return error(HttpStatus.CONFLICT, "DUPLICATE_CARD", exception.getMessage(), request.getRequestURI(), Map.of());
	}

	private static ResponseEntity<ApiError> error(HttpStatus status, String code, String message,
			String path, Map<String, String> fieldErrors) {
		ApiError body = new ApiError(Instant.now(), status.value(), code, message, path, fieldErrors);
		return ResponseEntity.status(status).body(body);
	}
}
