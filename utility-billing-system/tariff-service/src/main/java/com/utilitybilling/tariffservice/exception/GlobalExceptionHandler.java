package com.utilitybilling.tariffservice.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<String> handleBusiness(BusinessException e) {
		return ResponseEntity.badRequest().body(e.getMessage());
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<String> handleNotFound(NotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	}
}
