package com.utilitybilling.consumerservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilitybilling.consumerservice.dto.ConsumerRequestResponse;
import com.utilitybilling.consumerservice.model.ConsumerRequest;
import com.utilitybilling.consumerservice.service.ConsumerRequestService;

@WebMvcTest(ConsumerRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConsumerRequestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ConsumerRequestService service;

	@Autowired
	private ObjectMapper mapper;

	@Test
	void submit_ok() throws Exception {
		when(service.submit(any()))
				.thenReturn(ConsumerRequestResponse.builder().requestId("1").status("PENDING").build());

		mockMvc.perform(post("/consumer-requests").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "fullName":"A",
				  "email":"e",
				  "phone":"p",
				  "addressLine1":"x",
				  "city":"c",
				  "state":"s",
				  "postalCode":"p"
				}
				""")).andExpect(status().isCreated());
	}

	@Test
	void getAll_without_status() throws Exception {
		Page<ConsumerRequest> page = new PageImpl<>(List.of(new ConsumerRequest()));

		when(service.getAll(null, 0, 10)).thenReturn(page);

		mockMvc.perform(get("/consumer-requests?page=0&size=10")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());
	}

	@Test
	void getAll_with_status() throws Exception {
		Page<ConsumerRequest> page = new PageImpl<>(List.of(new ConsumerRequest()));

		when(service.getAll("PENDING", 0, 10)).thenReturn(page);

		mockMvc.perform(get("/consumer-requests?status=PENDING&page=0&size=10")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());
	}

	@Test
	void getById_ok() throws Exception {
		when(service.getById("1")).thenReturn(new ConsumerRequest());

		mockMvc.perform(get("/consumer-requests/1")).andExpect(status().isOk());
	}

	@Test
	void reject_ok() throws Exception {
		mockMvc.perform(put("/consumer-requests/1/reject").contentType(MediaType.APPLICATION_JSON)
				.content("{\"reason\":\"x\"}")).andExpect(status().isNoContent());
	}
}
