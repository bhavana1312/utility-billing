package com.utilitybilling.consumerservice.util;

import java.security.SecureRandom;

public class PasswordGenerator {

	private PasswordGenerator() {
		throw new AssertionError("Utility class");
	}

	private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#";

	public static String generate() {
		SecureRandom r = new SecureRandom();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < 10; i++)
			sb.append(CHARS.charAt(r.nextInt(CHARS.length())));

		return sb.toString();
	}
}
