package com.utilitybilling.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilitybilling.authservice.dto.InternalCreateUserRequest;
import com.utilitybilling.authservice.service.InternalUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalUserControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	InternalUserService service;

	@Autowired
	ObjectMapper mapper;

	@Test
	void create_returnsCreated() throws Exception {
		InternalCreateUserRequest r = new InternalCreateUserRequest();
		r.setUsername("u");
		r.setEmail("e");
		r.setPassword("p");

		mockMvc.perform(
				post("/internal/users").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(r)))
				.andExpect(status().isCreated());
	}
}
