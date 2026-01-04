package com.utilitybilling.authservice.feign;

import org.springframework.stereotype.Component;

@Component
public class NotificationClientFallback implements NotificationClient {

	@Override
	public void send(NotificationRequest request) {
		// intentionally silent fallback
		// auth flow should NOT fail if notification fails
	}
}
