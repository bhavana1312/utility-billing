package com.utilitybilling.billingservice.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.utilitybilling.billingservice.dto.OutstandingBalanceResponse;
import com.utilitybilling.billingservice.service.InternalBillingService;

class InternalBillingControllerTest {

	private MockMvc mockMvc;
	private InternalBillingService service;

	@BeforeEach
	void setup() {
		service = mock(InternalBillingService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new InternalBillingController(service)).build();
	}

	@Test
	void markPaid_ok() throws Exception {
		mockMvc.perform(put("/billing/internal/B1/mark-paid")).andExpect(status().isNoContent());

		verify(service).markPaid("B1");
	}

	@Test
	void outstanding_ok() throws Exception {
		OutstandingBalanceResponse r = new OutstandingBalanceResponse();
		r.setOutstandingAmount(BigDecimal.TEN);

		when(service.outstanding("C1")).thenReturn(r);

		mockMvc.perform(get("/billing/internal/consumer/C1/outstanding")).andExpect(status().isOk());
	}
}
