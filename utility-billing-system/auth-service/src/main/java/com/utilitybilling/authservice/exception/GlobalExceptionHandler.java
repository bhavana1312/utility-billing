package com.utilitybilling.authservice.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import feign.FeignException;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<String> handleUserExists(UserAlreadyExistsException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
	}

	@ExceptionHandler(CallNotPermittedException.class)
	public ResponseEntity<String> handleCircuitOpen(CallNotPermittedException ex) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body("Consumer service unavailable (circuit open)");
	}

	@ExceptionHandler(FeignException.class)
	public ResponseEntity<String> handleFeign(FeignException ex) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Consumer service unavailable");
	}

	@ExceptionHandler(DownstreamServiceException.class)
	public ResponseEntity<String> handleDownstream(DownstreamServiceException ex) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
	}

	@ExceptionHandler({ UserNotFoundException.class, InvalidCredentialsException.class, InvalidTokenException.class,
			IllegalArgumentException.class, IllegalStateException.class })
	public ResponseEntity<String> bad(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleGeneric(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
	}

}
