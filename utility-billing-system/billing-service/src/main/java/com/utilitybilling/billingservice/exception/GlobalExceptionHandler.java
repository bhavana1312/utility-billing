package com.utilitybilling.billingservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import feign.FeignException;

@Data
@AllArgsConstructor
class ApiError {
	private String message;
}

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> badRequest(IllegalArgumentException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(ex.getMessage()));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiError> conflict(IllegalStateException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(ex.getMessage()));
	}

	@ExceptionHandler(FeignException.NotFound.class)
	public ResponseEntity<String> handleFeign404(FeignException.NotFound ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dependent service resource not found");
	}

	@ExceptionHandler(FeignException.class)
	public ResponseEntity<String> handleFeign(FeignException ex) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Dependent service unavailable");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleGeneric(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error while generating bill");
	}
}
