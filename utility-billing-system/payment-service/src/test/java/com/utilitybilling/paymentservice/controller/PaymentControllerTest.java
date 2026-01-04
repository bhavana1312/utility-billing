package com.utilitybilling.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.utilitybilling.paymentservice.model.Payment;
import com.utilitybilling.paymentservice.service.PaymentService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;

class PaymentControllerTest {

	private MockMvc mockMvc;
	private PaymentService service;

	@BeforeEach
	void setup() {
		service = Mockito.mock(PaymentService.class);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.registerModule(new SpringDataJacksonConfiguration.PageModule());

		mockMvc = MockMvcBuilders.standaloneSetup(new PaymentController(service))
				.setMessageConverters(new MappingJackson2HttpMessageConverter(mapper),
						new ByteArrayHttpMessageConverter())
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();
	}

	@Test
	void initiate_ok() throws Exception {
		when(service.initiate(any())).thenReturn(new Payment());

		mockMvc.perform(
				post("/payments/initiate").contentType(MediaType.APPLICATION_JSON).content("{\"billId\":\"B1\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void confirm_ok() throws Exception {
		when(service.confirm(any())).thenReturn(new Payment());

		mockMvc.perform(post("/payments/confirm").contentType(MediaType.APPLICATION_JSON)
				.content("{\"paymentId\":\"P1\",\"otp\":\"123456\"}")).andExpect(status().isOk());
	}

	@Test
	void offline_ok() throws Exception {
		mockMvc.perform(post("/payments/offline").contentType(MediaType.APPLICATION_JSON)
				.content("{\"billId\":\"B1\",\"mode\":\"CASH\"}")).andExpect(status().isOk());
	}

	@Test
	void history_ok() throws Exception {
		Payment p = new Payment();
		p.setId("P1");

		Page<Payment> page = new PageImpl<>(List.of(p));

		when(service.history(eq("C1"), eq(0), eq(10), Mockito.nullable(String.class))).thenReturn(page);

		mockMvc.perform(
				get("/payments/history/C1").param("page", "0").param("size", "10").param("utilityType", "ELECTRICITY"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
	}

	@Test
	void invoices_ok() throws Exception {
		when(service.invoices("C1")).thenReturn(List.of());

		mockMvc.perform(get("/payments/invoices/C1")).andExpect(status().isOk());
	}

	@Test
	void outstanding_ok() throws Exception {
		when(service.outstanding("C1")).thenReturn(List.of());

		mockMvc.perform(get("/payments/outstanding/C1")).andExpect(status().isOk());
	}

	@Test
	void getPayments_ok() throws Exception {
		Payment p = new Payment();
		p.setId("P1");

		Page<Payment> page = new PageImpl<>(List.of(p));

		when(service.getPayments(anyInt(), anyInt(), Mockito.nullable(String.class), Mockito.nullable(String.class)))
				.thenReturn(page);

		mockMvc.perform(get("/payments").param("page", "0").param("size", "10")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());
	}

	@Test
	void downloadInvoice_ok() throws Exception {
		when(service.downloadInvoicePdf("P1")).thenReturn(new byte[] { 1, 2, 3 });

		mockMvc.perform(get("/payments/P1/invoice")).andExpect(status().isOk())
				.andExpect(header().string("Content-Type", "application/pdf"));
	}
}
