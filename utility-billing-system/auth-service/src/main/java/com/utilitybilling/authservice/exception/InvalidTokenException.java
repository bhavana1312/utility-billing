package com.utilitybilling.authservice.exception;

public class InvalidTokenException extends RuntimeException {
	public InvalidTokenException(String message) {
		super(message);
	}
}
