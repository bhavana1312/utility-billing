package com.utilitybilling.paymentservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilitybilling.paymentservice.dto.ConfirmPaymentRequest;
import com.utilitybilling.paymentservice.dto.InitiatePaymentRequest;
import com.utilitybilling.paymentservice.dto.OfflinePaymentRequest;
import com.utilitybilling.paymentservice.service.PaymentService;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private PaymentService service;

	@Autowired
	private ObjectMapper mapper;

	@Test
	void shouldInitiatePayment() throws Exception {
		InitiatePaymentRequest req = new InitiatePaymentRequest();
		req.setBillId("B1");

		when(service.initiate(any())).thenReturn(new Object());

		mockMvc.perform(post("/payments/initiate").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(req))).andExpect(status().isOk());
	}

	@Test
	void shouldConfirmPayment() throws Exception {
		ConfirmPaymentRequest req = new ConfirmPaymentRequest();

		when(service.confirm(any())).thenReturn(new Object());

		mockMvc.perform(post("/payments/confirm").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(req))).andExpect(status().isOk());
	}

	@Test
	void shouldHandleOfflinePayment() throws Exception {
		OfflinePaymentRequest req = new OfflinePaymentRequest();

		mockMvc.perform(post("/payments/offline").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(req))).andExpect(status().isOk());

		verify(service).offlinePay(any());
	}

	@Test
	void shouldReturnPaymentHistory() throws Exception {
		when(service.history("C1")).thenReturn(List.of());

		mockMvc.perform(get("/payments/history/C1")).andExpect(status().isOk());
	}

	@Test
	void shouldReturnInvoices() throws Exception {
		when(service.invoices("C1")).thenReturn(List.of());

		mockMvc.perform(get("/payments/invoices/C1")).andExpect(status().isOk());
	}

	@Test
	void shouldReturnOutstanding() throws Exception {
		when(service.outstanding("C1")).thenReturn(new Object());

		mockMvc.perform(get("/payments/outstanding/C1")).andExpect(status().isOk());
	}
}
