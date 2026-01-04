package com.utilitybilling.consumerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilitybilling.consumerservice.dto.*;
import com.utilitybilling.consumerservice.service.ConsumerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConsumerController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConsumerControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ConsumerService service;

	@Autowired
	private ObjectMapper mapper;

	@Test
	void approve_ok() throws Exception {
		when(service.approve("1")).thenReturn(ConsumerResponse.builder().id("1").build());

		mockMvc.perform(post("/consumers/from-request/1")).andExpect(status().isCreated());
	}

	@Test
	void get_ok() throws Exception {
		when(service.getById("1")).thenReturn(ConsumerResponse.builder().id("1").build());

		mockMvc.perform(get("/consumers/1")).andExpect(status().isOk());
	}

	@Test
	void getAll_paginated() throws Exception {
		Page<ConsumerResponse> page = new PageImpl<>(List.of(ConsumerResponse.builder().id("1").build()));

		when(service.getAll(0, 10)).thenReturn(page);

		mockMvc.perform(get("/consumers?page=0&size=10")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());
	}

	@Test
	void update_ok() throws Exception {
		when(service.update(any(), any())).thenReturn(ConsumerResponse.builder().id("1").build());

		mockMvc.perform(put("/consumers/1").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "fullName":"A",
				  "phone":"1",
				  "addressLine1":"x",
				  "city":"c",
				  "state":"s",
				  "postalCode":"p"
				}
				""")).andExpect(status().isOk());
	}

	@Test
	void deactivate_ok() throws Exception {
		mockMvc.perform(delete("/consumers/1")).andExpect(status().isNoContent());
	}

	@Test
	void exists_ok() throws Exception {
		when(service.exists("1")).thenReturn(true);

		mockMvc.perform(get("/consumers/1/exists")).andExpect(status().isOk())
				.andExpect(jsonPath("$.exists").value(true));
	}

	@Test
	void getByUsername_ok() throws Exception {
		when(service.getByUsername("u")).thenReturn(ConsumerResponse.builder().id("1").build());

		mockMvc.perform(get("/consumers/username/u")).andExpect(status().isOk());
	}
}
