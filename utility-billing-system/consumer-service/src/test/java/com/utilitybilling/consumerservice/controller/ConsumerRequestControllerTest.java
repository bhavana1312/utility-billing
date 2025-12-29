package com.utilitybilling.consumerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilitybilling.consumerservice.dto.*;
import com.utilitybilling.consumerservice.model.ConsumerRequest;
import com.utilitybilling.consumerservice.service.ConsumerRequestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConsumerRequestController.class)
class ConsumerRequestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ConsumerRequestService service;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void submit_shouldCreateRequest() throws Exception {
		CreateConsumerRequest r = new CreateConsumerRequest();
		r.setFullName("John");
		r.setEmail("john@test.com");
		r.setPhone("123");
		r.setAddressLine1("Addr");
		r.setCity("City");
		r.setState("State");
		r.setPostalCode("11111");

		Mockito.when(service.submit(Mockito.any()))
				.thenReturn(ConsumerRequestResponse.builder().requestId("req1").status("PENDING").build());

		mockMvc.perform(post("/consumer-requests").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(r))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.requestId").value("req1"));
	}

	@Test
	void get_shouldReturnRequest() throws Exception {
		Mockito.when(service.getById("1")).thenReturn(new ConsumerRequest());

		mockMvc.perform(get("/consumer-requests/1")).andExpect(status().isOk());
	}

	@Test
	void reject_shouldReturn204() throws Exception {
		RejectRequest request = new RejectRequest();
		request.setReason("Invalid data");

		mockMvc.perform(put("/consumer-requests/1/reject").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent());

		Mockito.verify(service).reject("1", "Invalid data");
	}
}
