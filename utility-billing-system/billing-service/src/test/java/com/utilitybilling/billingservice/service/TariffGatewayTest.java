package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.feign.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TariffGatewayTest {

	@Test
	void getActive_success() {
		TariffClient client = mock(TariffClient.class);
		TariffGateway gateway = new TariffGateway(client);

		TariffResponse r = new TariffResponse();
		when(client.getActive("E", "P")).thenReturn(r);

		assertEquals(r, gateway.getActive("E", "P"));
	}

	@Test
	void fallback_throws() {
		TariffGateway gateway = new TariffGateway(mock(TariffClient.class));
		RuntimeException ex = new RuntimeException();
		assertThrows(IllegalStateException.class, () -> gateway.getActiveFallback("E", "P", ex));
	}
}
