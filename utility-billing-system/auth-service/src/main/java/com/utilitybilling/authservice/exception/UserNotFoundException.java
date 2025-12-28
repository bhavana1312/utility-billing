package com.utilitybilling.authservice.exception;

public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException() {
		super("User not found");
	}
}